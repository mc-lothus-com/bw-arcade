package com.lothus.engines.bedwars.listener.server.arena;

import com.lothus.core.games.state.GameState;
import com.lothus.engines.bedwars.Bedwars;
import com.lothus.engines.bedwars.arena.Arena;
import com.lothus.engines.bedwars.user.User;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.FoodLevelChangeEvent;
import org.bukkit.event.player.PlayerAchievementAwardedEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerPickupItemEvent;
import org.bukkit.event.weather.WeatherChangeEvent;

import java.util.Arrays;

public class ArenaListener implements Listener {

    private final Arena arena;

    public ArenaListener() {
        arena = Bedwars.getInstance().getGameManager().getArena();
    }

    @EventHandler
    public void onDamage(EntityDamageEvent event) {
        if (arena.getGameInfo().getState() != GameState.EM_JOGO) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onBedBreak(BlockBreakEvent event) {
        if (arena.getGameInfo().getState() != GameState.EM_JOGO) {
            event.setCancelled(event.getPlayer().getGameMode() != GameMode.CREATIVE);
        } else {
            if (event.getBlock().getType() == Material.BED || event.getBlock().getType() == Material.BED_BLOCK) {
                Player player = event.getPlayer();
                User user = arena.getUserService().get(player.getUniqueId());

                event.setCancelled(true);

                if (user.getTeam() == null) return;

                if (user.getTeam().getBedLocation().getLocation().distanceSquared(event.getBlock().getLocation()) < 4) {
                    player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1F, 1F);
                    player.sendMessage("§cVocê não pode quebrar sua própria cama.");
                } else {
                    arena.getTeams().stream().filter(team -> team.getBedLocation().getLocation().distanceSquared(event.getBlock().getLocation()) < 3).
                            findAny().ifPresent(team -> team.breakBed(user));
                    Bedwars.getInstance().getGameManager().checkWinner();
                }
            }
        }
    }

    @EventHandler
    public void onPlace(BlockPlaceEvent event) {
        if (arena.getGameInfo().getState() != GameState.EM_JOGO) {
            event.setCancelled(event.getPlayer().getGameMode() != GameMode.CREATIVE);
        }
    }

    @EventHandler
    public void onDrop(PlayerDropItemEvent event) {
        if (event.getPlayer().getGameMode() == GameMode.ADVENTURE || arena.getGameInfo().getState() != GameState.EM_JOGO) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onPickUP(PlayerPickupItemEvent event) {
        Player player = event.getPlayer();

        if (player.getGameMode() == GameMode.ADVENTURE) {
            event.setCancelled(true);
        } else {
            if (event.getItem().getItemStack().getType().name().contains("SWORD")) {
                if (Arrays.stream(player.getInventory().getContents()).anyMatch(itemStack -> itemStack != null && itemStack.getType() == Material.WOOD_SWORD)) {
                    player.getInventory().remove(Material.WOOD_SWORD);
                }
            }
        }
    }

    @EventHandler
    public void onFoodLevel(FoodLevelChangeEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onWeather(WeatherChangeEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onAchievement(PlayerAchievementAwardedEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void onMobSpawn(CreatureSpawnEvent event) {
        if (event.getSpawnReason() != CreatureSpawnEvent.SpawnReason.CUSTOM) {
            event.setCancelled(true);
        }
    }

}
