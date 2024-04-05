package com.lothus.engines.bedwars.listener.server.entity;

import com.lothus.engines.bedwars.Bedwars;
import com.lothus.engines.bedwars.entity.Despawnable;
import com.lothus.engines.bedwars.entity.golem.IGolem;
import com.lothus.engines.bedwars.entity.silverfish.Silverfish;
import com.lothus.engines.bedwars.event.player.PlayerSummonEvent;
import com.lothus.engines.bedwars.event.server.TimeTickEvent;
import com.lothus.engines.bedwars.user.User;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;

public class EntityListener implements Listener {

    @EventHandler
    public void summon(PlayerSummonEvent event) {
        Player player = event.getPlayer();
        User user = event.getUser();
        Location targetLocation = event.getTargetLocation();

        switch (event.getEntityType()) {
            case IRON_GOLEM:
                new Despawnable(IGolem.spawn(player.getLocation().add(0, 1, 0), user.getTeam(), 0.2F, 20, 250), user.getTeam(),
                        250);
                break;
            case SILVERFISH:
                new Despawnable(Silverfish.spawn(targetLocation, user.getTeam(), 0.3F, 20, 250, 2), user.getTeam(),
                        250);
                break;
        }
    }

    @EventHandler
    public void damage(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            if (event.getEntityType() == EntityType.IRON_GOLEM || event.getEntityType() == EntityType.SILVERFISH) {
                Bedwars.getInstance().getGameManager().getDespawnables().values().stream().filter(despawnable -> despawnable.getEntity() == event.getEntity()).
                        findAny().ifPresent(despawnable -> {
                            if (despawnable.getTeam().isMember(event.getDamager().getName())) {
                                event.setCancelled(true);
                            }
                        });
            }
        }
    }

    @EventHandler
    public void death(EntityDeathEvent event) {
        if (event.getEntityType() == EntityType.IRON_GOLEM || event.getEntityType() == EntityType.SILVERFISH) {
            event.getDrops().clear();
            event.setDroppedExp(0);

            Bedwars.getInstance().getGameManager().getDespawnables().values().stream().filter(despawnable -> despawnable.getEntity() == event.getEntity()).
                    findAny().ifPresent(Despawnable::destroy);
        }
    }

    @EventHandler
    public void timeTick(TimeTickEvent event) {
        Bedwars.getInstance().getGameManager().getDespawnables().values().iterator().forEachRemaining(Despawnable::refresh);
    }
}
