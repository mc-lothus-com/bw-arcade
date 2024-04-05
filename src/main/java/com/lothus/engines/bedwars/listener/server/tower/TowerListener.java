package com.lothus.engines.bedwars.listener.server.tower;

import com.lothus.engines.bedwars.Bedwars;
import com.lothus.engines.bedwars.listener.server.tower.direction.TowerEast;
import com.lothus.engines.bedwars.listener.server.tower.direction.TowerNorth;
import com.lothus.engines.bedwars.listener.server.tower.direction.TowerSouth;
import com.lothus.engines.bedwars.listener.server.tower.direction.TowerWest;
import com.lothus.engines.bedwars.team.color.TeamColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;

public class TowerListener implements Listener {

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlace(BlockPlaceEvent e) {
        Player player = e.getPlayer();

        if (e.getBlockPlaced().getType() == Material.CHEST && !e.isCancelled()) {
            e.setCancelled(true);

            Location loc = e.getBlockPlaced().getLocation();
            Block chest = e.getBlockPlaced();
            TeamColor col = Bedwars.getInstance().getGameManager().getArena().getUserService().get(player.getUniqueId()).getTeam().getTeamColor();

            double rotation = ((player.getLocation().getYaw() - 90.0F) % 360.0F);
            if (rotation < 0.0D)
                rotation += 360.0D;
            if (45.0D <= rotation && rotation < 135.0D) {
                new TowerSouth(loc, chest, col, player);
            } else if (225.0D <= rotation && rotation < 315.0D) {
                new TowerNorth(loc, chest, col, player);
            } else if (135.0D <= rotation && rotation < 225.0D) {
                new TowerWest(loc, chest, col, player);
            } else if (0.0D <= rotation && rotation < 45.0D) {
                new TowerEast(loc, chest, col, player);
            } else if (315.0D <= rotation && rotation < 360.0D) {
                new TowerEast(loc, chest, col, player);
            }
        }
    }

}
