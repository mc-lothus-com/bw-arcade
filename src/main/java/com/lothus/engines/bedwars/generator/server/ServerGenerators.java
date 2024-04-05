package com.lothus.engines.bedwars.generator.server;

import com.lothus.core.games.state.GameState;
import com.lothus.engines.bedwars.Bedwars;
import com.lothus.engines.bedwars.arena.Arena;
import com.lothus.engines.bedwars.game.GameManager;
import com.lothus.engines.bedwars.generator.Generator;
import com.lothus.engines.bedwars.hologram.HologramV2;
import com.lothus.engines.bedwars.location.type.LocationType;
import com.lothus.engines.bedwars.upgrades.server.ServerUpgrades;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class ServerGenerators extends BukkitRunnable {

    private final Arena arena;
    private final List<Generator> diamondTier, emeraldTier;
    private final List<HologramV2> diamondHolo, emeraldHolo;
    private final Map<Player, List<Location>> containsHolo = new HashMap<>();

    public ServerGenerators(GameManager gameManager) {
        this.arena = gameManager.getArena();
        this.diamondTier = new ArrayList<>();
        this.emeraldTier = new ArrayList<>();
        this.diamondHolo = new ArrayList<>();
        this.emeraldHolo = new ArrayList<>();

        runTaskTimer(Bedwars.getInstance(), 0L, 20L);

        gameManager.getLocationManager().get(LocationType.DIAMOND_GENERATOR).forEach(locations -> {
            HologramV2 hologram = new HologramV2();

            locations.getLocation().subtract(0, 2, 0).getBlock().setType(Material.DIAMOND_BLOCK);
            locations.getLocation().add(0, 1, 0);

            diamondTier.add(new Generator(Material.DIAMOND, 30, "I", locations.getLocation()));
            diamondHolo.add(hologram);
        });
        gameManager.getLocationManager().get(LocationType.EMERALD_GENERATOR).forEach(locations -> {
            HologramV2 hologram = new HologramV2();

            locations.getLocation().subtract(0, 2, 0).getBlock().setType(Material.EMERALD_BLOCK);
            locations.getLocation().add(0, 1, 0);

            emeraldTier.add(new Generator(Material.EMERALD, 45, "I", locations.getLocation()));
            emeraldHolo.add(hologram);
        });
    }

    @Override
    public void run() {
        if (arena.getGameInfo().getState() == GameState.EM_JOGO) {
            diamondTier.forEach(generator -> {
                generator.setCooldown((generator.getCooldown() - 1));

                if (generator.getCooldown() == 0) {
                    dropItemNaturally(generator);
                    generator.setCooldown(30);
                }
                diamondHolo.forEach(hologram -> Bukkit.getOnlinePlayers().forEach(player -> {
                    if (!containsHolo.containsKey(player)) {
                        containsHolo.put(player, new ArrayList<>());
                    }
                    List<Location> list = containsHolo.get(player);

                    if (list.stream().noneMatch(location -> location.getX() == generator.getLocation().getX() && location.getZ() == generator.getLocation().getZ())) {
                        hologram.showLine(player, generator.getLocation().clone().add(0, 3.1, 0), "§eNível §c" + generator.getLevel());
                        hologram.showLine(player, generator.getLocation().clone().add(0, 2.8, 0), "§bDiamante");
                        hologram.showLine(player, generator.getLocation().clone().add(0, 2.5, 0), "§eSpawna em §c" + generator.getCooldown() + " §esegundos");

                        list.add(generator.getLocation().clone().add(0, 2.5, 0));

                        containsHolo.put(player, list);
                    } else {
                        hologram.showLine(player, generator.getLocation().clone().add(0, 2.5, 0), "§eSpawna em §c" + generator.getCooldown() + " §esegundos");
                    }
                }));
            });
            emeraldTier.forEach(generator -> {
                generator.setCooldown((generator.getCooldown() - 1));

                if (generator.getCooldown() == 0) {
                    dropItemNaturally(generator);
                    generator.setCooldown(60);
                }
                emeraldHolo.forEach(hologram -> Bukkit.getOnlinePlayers().forEach(player -> {
                    if (!containsHolo.containsKey(player)) {
                        containsHolo.put(player, new ArrayList<>());
                    }
                    List<Location> list = containsHolo.get(player);

                    if (list.stream().noneMatch(location -> location.getX() == generator.getLocation().getX() && location.getZ() == generator.getLocation().getZ())) {
                        hologram.showLine(player, generator.getLocation().clone().add(0, 3.1, 0), "§eNível §c" + generator.getLevel());
                        hologram.showLine(player, generator.getLocation().clone().add(0, 2.8, 0), "§bEsmeralda");
                        hologram.showLine(player, generator.getLocation().clone().add(0, 2.5, 0), "§eSpawna em §c" + generator.getCooldown() + " §esegundos");

                        list.add(generator.getLocation().clone().add(0, 2.5, 0));

                        containsHolo.put(player, list);
                    } else {
                        hologram.showLine(player, generator.getLocation().clone().add(0, 2.5, 0), "§eSpawna em §c" + generator.getCooldown() + " §esegundos");
                    }
                }));
            });
        }
    }

    public void upgradeGenerator() {
        ServerUpgrades serverUpgrades = arena.getServerUpgrades();

        diamondTier.forEach(generator -> {
            Bukkit.getOnlinePlayers().forEach(player -> diamondHolo.forEach(hologram -> {
                generator.getLocation().getWorld().getNearbyEntities(generator.getLocation().clone().add(0, 3.1, 0), 1, 1, 1).stream().filter(entity -> !(entity instanceof Player)).forEach(Entity::remove);

                hologram.showLine(player, generator.getLocation().clone().add(0, 3.1, 0), "§eNível §c" + generator.getLevel());
                hologram.showLine(player, generator.getLocation().clone().add(0, 2.8, 0), "§bDiamante");
            }));
            generator.setLevel((serverUpgrades.getDiamondTier() == 1 ? "I" : (serverUpgrades.getDiamondTier() == 2 ? "II" : "III")));
        });
        emeraldTier.forEach(generator -> {
            Bukkit.getOnlinePlayers().forEach(player -> emeraldHolo.forEach(hologram -> {
                generator.getLocation().getWorld().getNearbyEntities(generator.getLocation().clone().add(0, 3.1, 0), 1, 1, 1).stream().filter(entity -> !(entity instanceof Player)).forEach(Entity::remove);

                hologram.showLine(player, generator.getLocation().clone().add(0, 3.1, 0), "§eNível §c" + generator.getLevel());
                hologram.showLine(player, generator.getLocation().clone().add(0, 2.8, 0), "§bEsmeralda");
            }));
            generator.setLevel((serverUpgrades.getEmeraldTier() == 1 ? "I" : (serverUpgrades.getEmeraldTier() == 2 ? "II" : "III")));
        });
    }

    private void dropItemNaturally(Generator tier) {
        long count = 0;

        for (Item item : tier.getLocation().getWorld().getNearbyEntities(tier.getLocation(), 5, 5, 5).stream().filter(entity -> entity instanceof Item).map(entity -> (Item) entity).filter(item -> item.getItemStack().getType() == tier.getMaterial()).collect(Collectors.toSet())) {
            count += item.getItemStack().getAmount();
        }
        if (count >= (tier.getMaterial() == Material.DIAMOND ? 4 : 3)) {
            return;
        }
        if (tier.getLevel().equalsIgnoreCase("I")) {
            Item item = tier.getLocation().getWorld().dropItemNaturally(tier.getLocation(), new ItemStack(tier.getMaterial(), arena.getGameInfo().getRoomType().getMaxPlayersPerTeam()));

            Bukkit.getScheduler().scheduleSyncDelayedTask(Bedwars.getInstance(), () -> item.teleport(tier.getLocation().clone().add(0, 1, 0)), 30L);
        } else if (tier.getLevel().equalsIgnoreCase("II")) {
            for (int i = 0; i < 2; i++) {
                Item item = tier.getLocation().getWorld().dropItemNaturally(tier.getLocation(), new ItemStack(tier.getMaterial(), arena.getGameInfo().getRoomType().getMaxPlayersPerTeam()));

                Bukkit.getScheduler().scheduleSyncDelayedTask(Bedwars.getInstance(), () -> item.teleport(tier.getLocation().clone().add(0, 1, 0)), 30L);
            }
        } else {
            for (int i = 0; i < 3; i++) {
                Item item = tier.getLocation().getWorld().dropItemNaturally(tier.getLocation(), new ItemStack(tier.getMaterial(), arena.getGameInfo().getRoomType().getMaxPlayersPerTeam()));

                Bukkit.getScheduler().scheduleSyncDelayedTask(Bedwars.getInstance(), () -> item.teleport(tier.getLocation().clone().add(0, 1, 0)), 30L);
            }
        }
    }

    public Map<Player, List<Location>> getContainsHolo() {
        return containsHolo;
    }

    public List<HologramV2> getDiamondHolo() {
        return diamondHolo;
    }

    public List<HologramV2> getEmeraldHolo() {
        return emeraldHolo;
    }
}
