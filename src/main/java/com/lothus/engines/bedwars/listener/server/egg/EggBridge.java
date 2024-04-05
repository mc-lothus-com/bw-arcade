package com.lothus.engines.bedwars.listener.server.egg;

import com.lothus.engines.bedwars.Bedwars;
import com.lothus.engines.bedwars.event.player.PlayerSummonEvent;
import com.lothus.engines.bedwars.service.user.UserService;
import com.lothus.engines.bedwars.task.EggBridgeTask;
import com.lothus.engines.bedwars.user.User;
import com.lothus.engines.bedwars.service.Services;
import org.bukkit.Bukkit;
import org.bukkit.entity.Egg;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.event.entity.ProjectileLaunchEvent;

import java.util.HashMap;
import java.util.HashSet;

public class EggBridge implements Listener {

    private static HashMap<Egg, EggBridgeTask> bridges = new HashMap<>();

    @EventHandler
    public void onLaunch(ProjectileLaunchEvent event) {
        if (event.getEntity() instanceof Egg) {
            Egg projectile = (Egg) event.getEntity();
            if (projectile.getShooter() instanceof Player) {
                Player shooter = (Player) projectile.getShooter();

                bridges.put(projectile, new EggBridgeTask(shooter, projectile, Bedwars.getInstance().getGameManager().getArena().getUserService().get(shooter.getUniqueId()).getTeam().getTeamColor()));
            }
        }
        if (event.getEntity() instanceof Snowball) {
            Snowball projectile = (Snowball) event.getEntity();

            if (projectile.getShooter() instanceof Player) {
                Player shooter = (Player) projectile.getShooter();
                User user = Services.get(UserService.class).get(shooter.getUniqueId());

                Bukkit.getPluginManager().callEvent(new PlayerSummonEvent(user.getPlayer(), user, EntityType.SILVERFISH, projectile.getLocation()));
            }
        }
    }

    @EventHandler
    public void onHit(ProjectileHitEvent e) {
        if (e.getEntity() instanceof Egg) {
            removeEgg((Egg) e.getEntity());
        }
    }

    public static void removeEgg(Egg e) {
        if (bridges.containsKey(e)) {
            if (bridges.get(e) != null) {
                bridges.get(e).getTask().cancel();
            }
            bridges.remove(e);
        }
    }

}
