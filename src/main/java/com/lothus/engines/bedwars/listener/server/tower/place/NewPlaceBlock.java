package com.lothus.engines.bedwars.listener.server.tower.place;

import com.google.common.collect.Lists;
import com.lothus.engines.bedwars.Bedwars;
import com.lothus.engines.bedwars.arena.Arena;
import com.lothus.engines.bedwars.listener.server.protection.ProtectionListener;
import com.lothus.engines.bedwars.team.color.TeamColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

import java.util.List;

public class NewPlaceBlock {

    private final Arena arena;
    private final int server_generator;
    private final int team_generator;
    private final int villager;

    public NewPlaceBlock(Block b, String xyz, TeamColor color, Player p, boolean ladder, int ladderdata) {
        this.arena = Bedwars.getInstance().getGameManager().getArena();
        this.server_generator = Bedwars.getInstance().getConfig().getInt("protection.server_generator");
        this.team_generator = Bedwars.getInstance().getConfig().getInt("protection.team_generator");
        this.villager = Bedwars.getInstance().getConfig().getInt("protection.villager");

        int x = Integer.parseInt(xyz.split(", ")[0]);
        int y = Integer.parseInt(xyz.split(", ")[1]);
        int z = Integer.parseInt(xyz.split(", ")[2]);

        Block block = b.getRelative(x, y, z);

        if (block.getType().equals(Material.AIR) || !block.getType().isSolid()) {
            if (!canBuild(block.getLocation())) {
                return;
            }
            if (!ladder) {
                block.setType(Material.WOOL);
                block.setData((byte) color.getWoolId());

                ProtectionListener.getBlocks().add(block);
            } else {
                block.setType(Material.LADDER);
                block.setData((byte) ladderdata);
            }
        }
    }

    public boolean canBuild(Location location) {
        return getNearbyBlocks(location, server_generator).stream().noneMatch(block -> block.getType() == Material.DIAMOND_BLOCK || block.getType() == Material.EMERALD_BLOCK) && arena.getTeams().stream().noneMatch(team -> team.getIronGen().getLocation().distance(location) <= team_generator) &&
                arena.getTeams().stream().noneMatch(team -> team.getShop().getLocation().distance(location) <= villager) &&
                arena.getTeams().stream().noneMatch(team -> team.getUpgrade().getLocation().distance(location) <= villager);
    }

    private List<Block> getNearbyBlocks(Location location, int radius) {
        List<Block> blocks = Lists.newArrayList();

        for (int x = location.getBlockX() - radius; x <= location.getBlockX() + radius; x++) {
            for (int y = location.getBlockY() - radius; y <= location.getBlockY() + radius; y++) {
                for (int z = location.getBlockZ() - radius; z <= location.getBlockZ() + radius; z++) {
                    blocks.add(location.getWorld().getBlockAt(x, y, z));
                }
            }
        }
        return blocks;
    }

}