package com.lothus.engines.bedwars.task;

import com.google.common.collect.Lists;
import com.lothus.engines.bedwars.Bedwars;
import com.lothus.engines.bedwars.arena.Arena;
import com.lothus.engines.bedwars.listener.server.protection.ProtectionListener;
import com.lothus.engines.bedwars.team.color.TeamColor;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Egg;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.List;

public class EggBridgeTask implements Runnable {

    private final Egg projectile;
    private final TeamColor teamColor;
    private final Player player;
    private final Arena arena;
    private final int server_generator;
    private final int team_generator;
    private final int villager;
    private final BukkitTask task;

    public EggBridgeTask(Player player, Egg projectile, TeamColor teamColor) {
        this.projectile = projectile;
        this.teamColor = teamColor;
        this.player = player;
        this.arena = Bedwars.getInstance().getGameManager().getArena();
        this.server_generator = Bedwars.getInstance().getConfig().getInt("protection.server_generator");
        this.team_generator = Bedwars.getInstance().getConfig().getInt("protection.team_generator");
        this.villager = Bedwars.getInstance().getConfig().getInt("protection.villager");
        this.task = Bukkit.getScheduler().runTaskTimer(Bedwars.getInstance(), this, 0, 1);
    }

    @Override
    public void run() {
        Location loc = projectile.getLocation();

        if (projectile.isDead() || player.getLocation().distance(projectile.getLocation()) > 27 || player.getLocation().getY() - projectile.getLocation().getY() > 9) {
            task.cancel();
            return;
        }
        if (player.getLocation().distance(loc) > 4.0D) {
            Block b2 = loc.clone().subtract(0.0D, 2.0D, 0.0D).getBlock();

            if (canBuild(b2.getLocation())) {
                if (b2.getType() == Material.AIR) {
                    b2.setType(Material.WOOL);
                    b2.setData((byte) teamColor.getWoolId());
                    ProtectionListener.getBlocks().add(b2);
                    loc.getWorld().playEffect(b2.getLocation(), Effect.MOBSPAWNER_FLAMES, 3);
                    player.playSound(player.getLocation(), Sound.CHICKEN_EGG_POP, 1F, 1F);
                }
            }
            Block b3 = loc.clone().subtract(1.0D, 2.0D, 0.0D).getBlock();

            if (canBuild(b3.getLocation())) {
                if (b3.getType() == Material.AIR) {
                    b3.setType(Material.WOOL);
                    b3.setData((byte) teamColor.getWoolId());
                    ProtectionListener.getBlocks().add(b3);
                    loc.getWorld().playEffect(b3.getLocation(), Effect.MOBSPAWNER_FLAMES, 3);
                    player.playSound(player.getLocation(), Sound.CHICKEN_EGG_POP, 1F, 1F);
                }
            }
            Block b4 = loc.clone().subtract(0.0D, 2.0D, 1.0D).getBlock();

            if (canBuild(b4.getLocation())) {
                if (b4.getType() == Material.AIR) {
                    b4.setType(Material.WOOL);
                    b4.setData((byte) teamColor.getWoolId());
                    ProtectionListener.getBlocks().add(b4);
                    loc.getWorld().playEffect(b4.getLocation(), Effect.MOBSPAWNER_FLAMES, 3);
                    player.playSound(player.getLocation(), Sound.CHICKEN_EGG_POP, 1F, 1F);
                }
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

    public BukkitTask getTask() {
        return task;
    }
}
