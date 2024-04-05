package com.lothus.engines.bedwars.listener.server.fireball;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import com.lothus.engines.bedwars.Bedwars;
import com.lothus.engines.bedwars.arena.Arena;
import net.minecraft.server.v1_8_R3.EntityFireball;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.craftbukkit.v1_8_R3.CraftWorld;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftEntity;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftFireball;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Fireball;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.ExplosionPrimeEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.projectiles.ProjectileSource;
import org.bukkit.util.Vector;

import java.util.Collection;
import java.util.concurrent.TimeUnit;

public class FireballListener implements Listener {

    private final double fireballExplosionSize;
    private final double fireballHorizontal;
    private final double fireballVertical;

    private final double damageSelf;
    private final double damageTeammates;
    private final Arena arena;

    private static final Cache<Player, Boolean> cache = CacheBuilder.newBuilder().expireAfterWrite(3, TimeUnit.SECONDS).build();

    public FireballListener() {
        this.arena = Bedwars.getInstance().getGameManager().getArena();
        this.fireballExplosionSize = Bedwars.getInstance().getConfig().getInt("fireball.explosionSize");
        this.fireballHorizontal = Bedwars.getInstance().getConfig().getDouble("fireball.horizontal") * -1;
        this.fireballVertical = Bedwars.getInstance().getConfig().getDouble("fireball.vertical");
        this.damageSelf = Bedwars.getInstance().getConfig().getDouble("fireball.damageSelf");
        this.damageTeammates = 0.0;
    }

    @EventHandler
    public void fireballLaunch(PlayerInteractEvent e) {
        Player player = e.getPlayer();
        ItemStack inHand = e.getItem();

        if (e.getAction() == Action.RIGHT_CLICK_BLOCK || e.getAction() == Action.RIGHT_CLICK_AIR) {
            if (inHand == null) return;
            if (inHand.getType() == Material.FIREBALL) {
                e.setCancelled(true);

                /*if (fireballCooldown.getOrDefault(player, 0L) <= System.currentTimeMillis()) {
                    fireballCooldown.put(player, (System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(2)));*/
                Fireball fb = player.launchProjectile(Fireball.class);
                Vector direction = player.getEyeLocation().getDirection();
                fb = setFireballDirection(fb, direction);
                fb.setVelocity(fb.getDirection().multiply(10));
                fb.setIsIncendiary(true); // apparently this on <12 makes the fireball not explode on hit. wtf bukkit?
                fb.setYield((float) fireballExplosionSize);

                if (inHand.getAmount() > 1) {
                    inHand.setAmount((inHand.getAmount() - 1));
                } else {
                    player.getInventory().remove(inHand);
                    player.updateInventory();
                }
            }
//            }
        }
    }

    @EventHandler
    public void damageExplode(EntityDamageEvent event) {
        if (event.getEntity() instanceof Player) {
            Player player = (Player) event.getEntity();

            if (event.getCause() == EntityDamageEvent.DamageCause.ENTITY_EXPLOSION) {
                event.setCancelled(true);
            }
            if (event.getCause() == EntityDamageEvent.DamageCause.FALL) {
                if (cache.getIfPresent(player) != null) {
                    event.setCancelled(true);
                }
            }
        }
    }
/*
    @EventHandler
    public void death(PlayerDeathEvent com.redelegit.npc.event) {
        fireballCooldown.remove(com.redelegit.npc.event.getEntity());
    }*/

    private Fireball setFireballDirection(Fireball fireball, Vector vector) {
        EntityFireball fb = ((CraftFireball) fireball).getHandle();
        fb.dirX = vector.getX() * 0.1D;
        fb.dirY = vector.getY() * 0.1D;
        fb.dirZ = vector.getZ() * 0.1D;
        return (Fireball) fb.getBukkitEntity();
    }


    @EventHandler
    public void fireballHit(ProjectileHitEvent e) {
        if (!(e.getEntity() instanceof Fireball)) return;
        Location location = e.getEntity().getLocation();

        ProjectileSource projectileSource = e.getEntity().getShooter();
        if (!(projectileSource instanceof Player)) return;
        Player source = (Player) projectileSource;

        Vector vector = location.toVector();

        World world = location.getWorld();

        Collection<Entity> nearbyEntities = world
                .getNearbyEntities(location, fireballExplosionSize, fireballExplosionSize, fireballExplosionSize);
        for (Entity entity : nearbyEntities) {
            if (!(entity instanceof Player)) continue;
            Player player = (Player) entity;
            if (!arena.getUserService().get(player.getUniqueId()).isPlaying()) continue;

            Vector playerVector = player.getLocation().toVector();
            Vector normalizedVector = vector.subtract(playerVector).normalize();
            Vector horizontalVector = normalizedVector.multiply(fireballHorizontal);
            double y = normalizedVector.getY();
            if(y < 0 ) y += 1.5;
            if(y <= 0.5) {
                y = fireballVertical*1.5; // kb for not jumping
            } else {
                y = y*fireballVertical*1.5; // kb for jumping
            }
            player.setVelocity(horizontalVector.setY(y));

            cache.put(player, true);

            if (player.equals(source)) {
                if (damageSelf > 0) {
                    player.damage(damageSelf); // damage shooter
                }
            } else if (arena.getUserService().get(player.getUniqueId()).getTeam().getTeamColor().equals(arena.getUserService().get(source.getUniqueId()).getTeam().getTeamColor())) {
                if (damageTeammates > 0) {
                    player.damage(damageTeammates); // damage teammates
                }
            }
        }
    }


    @EventHandler
    public void fireballDirectHit(EntityDamageByEntityEvent e) {
        if (!(e.getDamager() instanceof Fireball)) return;
        if (!(e.getEntity() instanceof Player)) return;

        if (arena.getUserService().get(e.getEntity().getUniqueId()) == null) return;

        e.setCancelled(true);
    }

    @EventHandler
    public void fireballPrime(ExplosionPrimeEvent e) {
        if (!(e.getEntity() instanceof Fireball)) return;
        ProjectileSource shooter = ((Fireball) e.getEntity()).getShooter();
        if (!(shooter instanceof Player)) return;

        e.setFire(true);
    }

    public static Cache<Player, Boolean> getCache() {
        return cache;
    }
}