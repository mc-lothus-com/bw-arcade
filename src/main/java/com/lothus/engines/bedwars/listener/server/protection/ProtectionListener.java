package com.lothus.engines.bedwars.listener.server.protection;

import com.google.common.collect.Lists;
import com.lothus.core.games.state.GameState;
import com.lothus.engines.bedwars.Bedwars;
import com.lothus.engines.bedwars.arena.Arena;
import com.lothus.engines.bedwars.listener.server.fireball.FireballListener;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.TNTPrimed;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

public class ProtectionListener implements Listener {

    private final static List<Block> blocks = new ArrayList<>();
    private final Arena arena;
    private final double fireballExplosionSize;
    private final double fireballHorizontal;
    private final double fireballVertical;
    private final int server_generator;
    private final int team_generator;
    private final int villager;

    public ProtectionListener() {
        arena = Bedwars.getInstance().getGameManager().getArena();
        fireballExplosionSize = Bedwars.getInstance().getConfig().getInt("tnt.explosionSize");
        fireballHorizontal = Bedwars.getInstance().getConfig().getDouble("tnt.horizontal") * -1;
        fireballVertical = Bedwars.getInstance().getConfig().getDouble("tnt.vertical");
        server_generator = Bedwars.getInstance().getConfig().getInt("protection.server_generator");
        team_generator = Bedwars.getInstance().getConfig().getInt("protection.team_generator");
        villager = Bedwars.getInstance().getConfig().getInt("protection.villager");
    }

    @EventHandler
    public void onPlaceBlock(BlockPlaceEvent event) {
        Location location = event.getBlock().getLocation();

        if (arena.getGameInfo().getState() != GameState.EM_JOGO || !canBuild(location) || ((event.getBlock().getLocation().getY() - arena.getTeams().stream().findAny().get().getSpawn().getLocation().getY()) >= 80)) {
            event.getPlayer().sendMessage("§cVocê não pode colocar blocos aqui.");
            event.setCancelled(true);
        } else {
            if (event.getBlock().getType() == Material.TNT) {
                event.getBlock().setType(Material.AIR);

                location.getWorld().playSound(location, Sound.FUSE, 1F, 1F);
                TNTPrimed primed = location.getWorld().spawn(location.clone().add(0.5, 0, 0.5), TNTPrimed.class);

                primed.setFuseTicks(58);
                primed.setIsIncendiary(true);
                primed.setFireTicks(3);
                primed.setYield(4.0F);
                return;
            }
            blocks.add(location.getBlock());
        }
    }

    public boolean canBuild(Location location) {
        return getNearbyBlocks(location, server_generator).stream().noneMatch(block -> block.getType() == Material.DIAMOND_BLOCK || block.getType() == Material.EMERALD_BLOCK) && arena.getTeams().stream().noneMatch(team -> team.getIronGen().getLocation().distance(location) <= team_generator) &&
                (arena.getTeams().stream().noneMatch(team -> team.getShop().getLocation().distance(location) <= villager)) &&
                arena.getTeams().stream().noneMatch(team -> team.getUpgrade().getLocation().distance(location) <= villager);
    }

    @EventHandler
    public void onBreakBlock(BlockBreakEvent event) {
        Block b = event.getBlock();
        Optional<Block> any = blocks.stream().filter(block -> block.getLocation().getBlockX() == b.getLocation().getBlockX() && block.getLocation().getBlockZ() == b.getLocation().getBlockZ() && block.getLocation().getBlockY() == b.getLocation().getBlockY()).findAny();

        if (!any.isPresent()) {
            if (b.getType() != Material.BED && b.getType() != Material.BED_BLOCK && b.getType() != Material.AIR) {
                if (b.getType().isBlock()) {
                    event.setCancelled(true);
                    event.getPlayer().sendMessage("§cVocê não pode quebrar este bloco.");
                }
            }
        } else {
            blocks.remove(any.get());
        }
    }

    @EventHandler
    public void onExplode(EntityExplodeEvent event) {
        event.setCancelled(true);

        Location location = event.getLocation();
        Vector vector = location.toVector();

        Collection<Entity> nearbyEntities = location.getWorld().getNearbyEntities(location, fireballExplosionSize, fireballExplosionSize, fireballExplosionSize);

        for (Entity entity : nearbyEntities) {
            if (!(entity instanceof Player)) continue;
            Player player = (Player) entity;
            if (!arena.getUserService().get(player.getUniqueId()).isPlaying()) continue;

            Vector playerVector = player.getLocation().toVector();
            Vector normalizedVector = vector.subtract(playerVector).normalize();
            Vector horizontalVector = normalizedVector.multiply(fireballHorizontal);
            double y = normalizedVector.getY();
            if (y < 0) y += 1.5;
            if (y <= 0.5) {
                y = fireballVertical * 1.5; // kb for not jumping
            } else {
                y = y * fireballVertical * 1.5; // kb for jumping
            }
            player.setVelocity(horizontalVector.setY(y));

            FireballListener.getCache().put(player, true);
        }
        AtomicInteger a = new AtomicInteger(2);

        event.blockList().forEach(block -> blocks.stream().filter(b -> block.getLocation().getBlockX() == b.getLocation().getBlockX() && block.getLocation().getBlockZ() == b.getLocation().getBlockZ() && block.getLocation().getBlockY() == b.getLocation().getBlockY()).forEach(b -> {
            if (b.getType() != Material.AIR && b.getType() != Material.OBSIDIAN && b.getType() != Material.BEDROCK) {
                if (!(event.getEntity() instanceof TNTPrimed)) {
                    if (b.getType() == Material.ENDER_STONE) {
                        event.setCancelled(true);
                        return;
                    }
                }
                if (isGlass(block.getX(), block.getY(), block.getZ()) || isAdjacentToGlass(block)) {
                    event.setCancelled(true);
                    return;
                }
                if (a.getAndIncrement() % 2 == 0) {
                    b.getWorld().dropItemNaturally(location, new ItemStack(b.getType(), 1, b.getData()));
                    b.setType(Material.AIR);
                }
            }
        }));
    }

    @EventHandler
    public void bucketInteract(PlayerInteractEvent event) {
        if (event.getPlayer().getItemInHand().getType() == Material.WATER_BUCKET) {
            if (event.getClickedBlock() == null) return;

            if (canBuild(event.getClickedBlock().getLocation())) {
                Bukkit.getScheduler().scheduleSyncDelayedTask(Bedwars.getInstance(), () -> event.getPlayer().setItemInHand(null), 5L);
            } else {
                event.setCancelled(true);
                event.getPlayer().sendMessage("§cVocê não pode colocar água aqui.");
            }
        }
    }

    private boolean isAdjacentToGlass(Block block) {
        int x = block.getX();
        int y = block.getY();
        int z = block.getZ();

        return isGlass(x + 1, y, z) || isGlass(x - 1, y, z) || isGlass(x, y + 1, z) ||
                isGlass(x, y - 1, z) || isGlass(x, y, z + 1) || isGlass(x, y, z - 1);
    }

    private boolean isGlass(int x, int y, int z) {
        Block block = Bukkit.getWorld("world").getBlockAt(x, y, z);
        return block.getType().name().contains("GLASS");
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

    public static List<Block> getBlocks() {
        return blocks;
    }
}
