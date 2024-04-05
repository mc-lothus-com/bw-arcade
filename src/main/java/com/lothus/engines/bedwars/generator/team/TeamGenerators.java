package com.lothus.engines.bedwars.generator.team;

import com.lothus.core.games.state.GameState;
import com.lothus.engines.bedwars.Bedwars;
import com.lothus.engines.bedwars.arena.Arena;
import com.lothus.engines.bedwars.game.GameManager;
import com.lothus.engines.bedwars.generator.Generator;
import com.lothus.engines.bedwars.location.type.LocationType;
import com.lothus.engines.bedwars.team.Team;
import com.lothus.engines.bedwars.upgrades.team.TeamUpgrades;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Item;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class TeamGenerators extends BukkitRunnable {

    private final Arena arena;
    private final List<Generator> ironTier, goldTier;
    private int emeraldTimer = 60;

    public TeamGenerators(GameManager gameManager) {
        this.arena = gameManager.getArena();
        this.ironTier = new ArrayList<>();
        this.goldTier = new ArrayList<>();

        runTaskTimer(Bedwars.getInstance(), 0L, 20L);

        gameManager.getLocationManager().get(LocationType.IRON_GENERATOR).forEach(locations -> ironTier.add(new Generator(Material.IRON_INGOT, 1, "I", locations.getLocation())));
        gameManager.getLocationManager().get(LocationType.GOLD_GENERATOR).forEach(locations -> goldTier.add(new Generator(Material.GOLD_INGOT, 7, "I", locations.getLocation())));
    }

    @Override
    public void run() {
        if (arena.getGameInfo().getState() == GameState.EM_JOGO) {
            emeraldTimer--;

            ironTier.forEach(generator -> {
                generator.setCooldown((generator.getCooldown() - 1));

                if (generator.getCooldown() == 0) {
                    dropItemNaturally(generator);
                    generator.setCooldown(1);
                }
                if (generator.getLevel().equals("IV") || generator.getLevel().equals("V")) {
                    if (emeraldTimer == 0) {
                        Item item = generator.getLocation().getWorld().dropItemNaturally(generator.getLocation(), new ItemStack(Material.EMERALD, 1));

                        Bukkit.getScheduler().scheduleSyncDelayedTask(Bedwars.getInstance(), () -> item.teleport(generator.getLocation().clone().add(0, 1, 0)), 30L);
                    }
                }
            });
            goldTier.forEach(generator -> {
                generator.setCooldown((generator.getCooldown() - 1));

                if (generator.getCooldown() == 0) {
                    dropItemNaturally(generator);
                    generator.setCooldown(7);
                }
            });
            if (emeraldTimer == 0) {
                emeraldTimer = 60;
            }
        }
    }

    public void upgradeGenerator(Team team) {
        TeamUpgrades teamUpgrades = team.getTeamUpgrades();
        Location ironGen = team.getIronGen().getLocation();
        Location goldGen = team.getGoldGen().getLocation();

        ironTier.stream().filter(generator -> generator.getLocation().getBlockX() == ironGen.getBlockX() && generator.getLocation().getBlockZ() == ironGen.getBlockZ()).findAny().ifPresent(generator -> generator.setLevel((teamUpgrades.getForge() == 1 ? "II" : (teamUpgrades.getForge() == 2 ? "III" : (teamUpgrades.getForge() == 3 ? "IV" : "V")))));
        goldTier.stream().filter(generator -> generator.getLocation().getBlockX() == goldGen.getBlockX() && generator.getLocation().getBlockZ() == goldGen.getBlockZ()).findAny().ifPresent(generator -> generator.setLevel((teamUpgrades.getForge() == 1 ? "II" : (teamUpgrades.getForge() == 2 ? "III" : (teamUpgrades.getForge() == 3 ? "IV" : "V")))));
    }

    private void dropItemNaturally(Generator tier) {
        long count = 0;

        for (Item item : tier.getLocation().getWorld().getNearbyEntities(tier.getLocation(), 5, 5, 5).stream().filter(entity -> entity instanceof Item).map(entity -> (Item) entity).filter(item -> item.getItemStack().getType() == tier.getMaterial()).collect(Collectors.toSet())) {
            count += item.getItemStack().getAmount();
        }
        if (count >= (tier.getMaterial() == Material.IRON_INGOT ? 48 : (tier.getMaterial() == Material.GOLD_INGOT ? 16 : 2))) {
            return;
        }
        switch (tier.getLevel()) {
            case "I":
                Item item = tier.getLocation().getWorld().dropItemNaturally(tier.getLocation(), new ItemStack(tier.getMaterial(), arena.getGameInfo().getRoomType().getMaxPlayersPerTeam()));

                Bukkit.getScheduler().scheduleSyncDelayedTask(Bedwars.getInstance(), () -> item.teleport(tier.getLocation().clone().add(0, 1, 0)), 30L);
                break;
            case "II":
                for (int i = 0; i < 2; i++) {
                    Item it = tier.getLocation().getWorld().dropItemNaturally(tier.getLocation(), new ItemStack(tier.getMaterial(), arena.getGameInfo().getRoomType().getMaxPlayersPerTeam()));

                    Bukkit.getScheduler().scheduleSyncDelayedTask(Bedwars.getInstance(), () -> it.teleport(tier.getLocation().clone().add(0, 1, 0)), 30L);
                }
                break;
            case "III":
            case "IV":
                for (int i = 0; i < 3; i++) {
                    Item it = tier.getLocation().getWorld().dropItemNaturally(tier.getLocation(), new ItemStack(tier.getMaterial(), arena.getGameInfo().getRoomType().getMaxPlayersPerTeam()));

                    Bukkit.getScheduler().scheduleSyncDelayedTask(Bedwars.getInstance(), () -> it.teleport(tier.getLocation().clone().add(0, 1, 0)), 30L);
                }
                break;
            case "V":
                for (int i = 0; i < 4; i++) {
                    Item it = tier.getLocation().getWorld().dropItemNaturally(tier.getLocation(), new ItemStack(tier.getMaterial(), arena.getGameInfo().getRoomType().getMaxPlayersPerTeam()));

                    Bukkit.getScheduler().scheduleSyncDelayedTask(Bedwars.getInstance(), () -> it.teleport(tier.getLocation().clone().add(0, 1, 0)), 30L);
                }
                break;
        }
    }

}
