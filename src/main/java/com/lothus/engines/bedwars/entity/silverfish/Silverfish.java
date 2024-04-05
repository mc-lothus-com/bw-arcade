package com.lothus.engines.bedwars.entity.silverfish;

import com.lothus.engines.bedwars.Bedwars;
import com.lothus.engines.bedwars.team.Team;
import com.lothus.engines.bedwars.team.color.TeamColor;
import com.lothus.engines.bedwars.user.User;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftSilverfish;
import org.bukkit.craftbukkit.v1_8_R3.util.UnsafeList;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityTargetEvent;
import org.bukkit.event.entity.EntityTargetLivingEntityEvent;

import java.lang.reflect.Field;

@SuppressWarnings("ALL")
public class Silverfish extends EntitySilverfish implements Listener {

    private Team team;

    public Silverfish() {
        super(((CraftWorld) Bukkit.getWorld("world")).getHandle());
    }

    public Silverfish(World world, Team bedWarsTeam) {
        super(world);
        if (bedWarsTeam == null) return;
        try {
            Field bField = PathfinderGoalSelector.class.getDeclaredField("b");
            bField.setAccessible(true);
            Field cField = PathfinderGoalSelector.class.getDeclaredField("c");
            cField.setAccessible(true);
            bField.set(this.goalSelector, new UnsafeList());
            bField.set(this.targetSelector, new UnsafeList());
            cField.set(this.goalSelector, new UnsafeList());
            cField.set(this.targetSelector, new UnsafeList());
        } catch (IllegalAccessException | NoSuchFieldException e1) {
            e1.printStackTrace();
        }
        this.team = bedWarsTeam;
        this.goalSelector.a(1, new PathfinderGoalFloat(this));
        this.goalSelector.a(2, new PathfinderGoalMeleeAttack(this, 1.9D, false));
        this.targetSelector.a(1, new PathfinderGoalHurtByTarget(this, true));
        this.goalSelector.a(3, new PathfinderGoalRandomStroll(this, 2D));
        this.targetSelector.a(2, new PathfinderGoalNearestAttackableTarget(this, EntityHuman.class, 20, true, false, player -> {
            if (player == null) return false;
            return ((EntityHuman) player).isAlive() && !team.isMember(((EntityHuman) player).getName());
        }));
        this.targetSelector.a(4, new PathfinderGoalNearestAttackableTarget(this, Silverfish.class, 20, true, false, sf -> {
            if (sf == null) return false;
            return ((Silverfish) sf).getTeam() != team;
        }));
    }

    public Team getTeam() {
        return team;
    }

    public static LivingEntity spawn(Location loc, Team bedWarsTeam, double speed, double health, int despawn, double damage) {
        WorldServer mcWorld = ((CraftWorld) loc.getWorld()).getHandle();
        org.bukkit.entity.Silverfish silverfish = mcWorld.getWorld().spawn(loc, org.bukkit.entity.Silverfish.class, CreatureSpawnEvent.SpawnReason.CUSTOM);
        EntitySilverfish entitySilverfish = ((CraftSilverfish) silverfish).getHandle();

        int percentuale = (int) ((silverfish.getHealth() * 100) / health);

        silverfish.setRemoveWhenFarAway(false);
        silverfish.setTarget((LivingEntity) silverfish.getLocation().getWorld().getNearbyEntities(silverfish.getLocation(), 10, 10, 10).stream().
                filter(entity -> entity instanceof Player && (!bedWarsTeam.isMember(((Player) entity).getName()))).findFirst().orElse(null));
        entitySilverfish.getAttributeInstance(GenericAttributes.maxHealth).setValue(health);
        entitySilverfish.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(speed);
        entitySilverfish.setLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
        entitySilverfish.setCustomNameVisible(false);
        entitySilverfish.setHealth((float) health);
        entitySilverfish.setInvisible(false);
        entitySilverfish.removeAllEffects();
        entitySilverfish.targetSelector.a(2, new PathfinderGoalNearestAttackableTarget(entitySilverfish, EntityHuman.class, 20, true, false, player -> {
            if (player == null) return false;
            return ((EntityHuman) player).isAlive() && !bedWarsTeam.isMember(((EntityHuman) player).getName());
        }));
        entitySilverfish.targetSelector.a(3, new PathfinderGoalNearestAttackableTarget(entitySilverfish, Silverfish.class, 20, true, false, g -> {
            if (silverfish == null) return false;
            return ((Silverfish) silverfish).getTeam() != bedWarsTeam;
        }));
        entitySilverfish.targetSelector.a(4, new PathfinderGoalNearestAttackableTarget(entitySilverfish, Silverfish.class, 20, true, false, sf -> {
            if (sf == null) return false;
            return ((Silverfish) sf).getTeam() != bedWarsTeam;
        }));
        return silverfish;
    }

    @EventHandler
    public void onEntityTarget(EntityTargetEvent event) {
        Entity entity = event.getEntity();

        if (!(entity instanceof CraftSilverfish)) return;

        Entity target = event.getTarget();

        if (!(target instanceof CraftPlayer)) {
            event.setCancelled(true);
            return;
        }

        Player player = (Player) target;
        EntitySilverfish entitySilverfish = ((CraftSilverfish) entity).getHandle();
        User u = Bedwars.getInstance().getGameManager().getUserService().get(player.getUniqueId());
        Team playerTeam = u.getTeam();

        TeamColor team = TeamColor.valueOf(entitySilverfish.getCustomName().split(" : ")[0]);

        if (team == null || team == playerTeam.getTeamColor()) {
            event.setCancelled(true);
        }
    }

    @Override
    public void die(DamageSource damagesource) {
        super.die(damagesource);
        team = null;
        Bedwars.getInstance().getGameManager().getDespawnables().remove(this.getUniqueID());
    }

    @Override
    public void die() {
        super.die();
        team = null;
        Bedwars.getInstance().getGameManager().getDespawnables().remove(this.getUniqueID());
    }

}
