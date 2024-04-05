package com.lothus.engines.bedwars.entity.golem;

import com.lothus.engines.bedwars.Bedwars;
import com.lothus.engines.bedwars.entity.silverfish.Silverfish;
import com.lothus.engines.bedwars.team.Team;
import com.lothus.engines.bedwars.team.color.TeamColor;
import com.lothus.engines.bedwars.user.User;
import net.minecraft.server.v1_8_R3.*;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftIronGolem;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftSilverfish;
import org.bukkit.craftbukkit.v1_8_R3.util.UnsafeList;
import org.bukkit.entity.Entity;
import org.bukkit.entity.IronGolem;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityTargetEvent;

import java.lang.reflect.Field;

@SuppressWarnings("ALL")
public class IGolem extends EntityIronGolem implements Listener {

    private Team team;

    public IGolem() {
        super(((CraftWorld) Bukkit.getWorld("world")).getHandle());
    }

    private IGolem(World world, Team team) {
        super(world);
        this.team = team;

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
        this.setSize(1.4F, 2.9F);
        ((Navigation) this.getNavigation()).a(true);
        this.goalSelector.a(1, new PathfinderGoalFloat(this));
        this.goalSelector.a(2, new PathfinderGoalMeleeAttack(this, 1.5D, false));
        this.targetSelector.a(1, new PathfinderGoalHurtByTarget(this, true));
        this.goalSelector.a(3, new PathfinderGoalRandomStroll(this, 1D));
        this.goalSelector.a(4, new PathfinderGoalRandomLookaround(this));
        this.targetSelector.a(2, new PathfinderGoalNearestAttackableTarget(this, EntityHuman.class, 20, true, false, player -> {
            if (player == null) return false;
            return ((EntityHuman) player).isAlive() && !team.isMember(((EntityHuman) player).getName());
        }));
        this.targetSelector.a(3, new PathfinderGoalNearestAttackableTarget(this, IGolem.class, 20, true, false, golem -> {
            if (golem == null) return false;
            return ((IGolem) golem).getTeam() != team;
        }));
        this.targetSelector.a(4, new PathfinderGoalNearestAttackableTarget(this, Silverfish.class, 20, true, false, sf -> {
            if (sf == null) return false;
            return ((Silverfish) sf).getTeam() != team;
        }));
    }

    public Team getTeam() {
        return team;
    }

    public static LivingEntity spawn(Location loc, Team bedWarsTeam, double speed, double health, int despawn) {
        WorldServer mcWorld = ((CraftWorld) loc.getWorld()).getHandle();
        IronGolem golem = mcWorld.getWorld().spawn(loc, IronGolem.class, CreatureSpawnEvent.SpawnReason.CUSTOM);
        EntityIronGolem entityIronGolem = ((CraftIronGolem) golem).getHandle();

        int percentuale = (int) ((golem.getHealth() * 100) / health / 10);

        golem.setRemoveWhenFarAway(false);
        entityIronGolem.getAttributeInstance(GenericAttributes.maxHealth).setValue(health);
        entityIronGolem.getAttributeInstance(GenericAttributes.MOVEMENT_SPEED).setValue(speed);
        entityIronGolem.setLocation(loc.getX(), loc.getY(), loc.getZ(), loc.getYaw(), loc.getPitch());
        entityIronGolem.setCustomNameVisible(true);
        entityIronGolem.setHealth((float) health);
        entityIronGolem.setInvisible(false);
        entityIronGolem.removeAllEffects();
        entityIronGolem.targetSelector.a(2, new PathfinderGoalNearestAttackableTarget(entityIronGolem, EntityHuman.class, 20, true, false, player -> {
            if (player == null) return false;
            return ((EntityHuman) player).isAlive() && !bedWarsTeam.isMember(((EntityHuman) player).getName());
        }));
        entityIronGolem.targetSelector.a(3, new PathfinderGoalNearestAttackableTarget(entityIronGolem, IGolem.class, 20, true, false, g -> {
            if (golem == null) return false;
            return ((IGolem) golem).getTeam() != bedWarsTeam;
        }));
        entityIronGolem.targetSelector.a(4, new PathfinderGoalNearestAttackableTarget(entityIronGolem, Silverfish.class, 20, true, false, sf -> {
            if (sf == null) return false;
            return ((Silverfish) sf).getTeam() != bedWarsTeam;
        }));
        return golem;
    }

    @EventHandler
    public void onEntityTarget(EntityTargetEvent event) {
        org.bukkit.entity.Entity entity = event.getEntity();

        if (!(entity instanceof CraftIronGolem)) return;

        Entity target = event.getTarget();

        if (!(target instanceof CraftPlayer)) {
            if (target instanceof CraftSilverfish) {
                EntitySilverfish entitySilverfish = ((CraftSilverfish) target).getHandle();
                TeamColor silverTeam = TeamColor.valueOf(entitySilverfish.getCustomName().split(" : ")[0]);
                if (silverTeam == null || team.getTeamColor() == silverTeam) {
                    event.setCancelled(true);
                }
            }
            event.setCancelled(true);
            return;
        }

        Player player = (Player) target;
        EntityIronGolem entitySilverfish = ((CraftIronGolem) entity).getHandle();
        User u = Bedwars.getInstance().getGameManager().getUserService().get(player.getUniqueId());
        Team playerTeam = u.getTeam();

        TeamColor team = TeamColor.valueOf(entitySilverfish.getCustomName().split(" : ")[0]);

        if (team == null || team == playerTeam.getTeamColor()) {
            event.setCancelled(true);
        }
    }

    @Override
    protected void dropDeathLoot(boolean flag, int i) {

    }

    @Override
    public void die() {
        super.die();
        team = null;
        Bedwars.getInstance().getGameManager().getDespawnables().remove(this.getUniqueID());
    }

    @Override
    public void die(DamageSource damagesource) {
        super.die(damagesource);
        team = null;
        Bedwars.getInstance().getGameManager().getDespawnables().remove(this.getUniqueID());
    }
}