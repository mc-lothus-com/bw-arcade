package com.lothus.engines.bedwars.listener.player.damage;

import com.lothus.core.games.room.RoomType;
import com.lothus.core.games.state.GameState;
import com.lothus.engines.bedwars.Bedwars;
import com.lothus.engines.bedwars.arena.Arena;
import com.lothus.engines.bedwars.entity.Despawnable;
import com.lothus.engines.bedwars.game.GameManager;
import com.lothus.engines.bedwars.user.User;
import com.lothus.sync.stats.games.addons.deathcries.DeathCry;
import com.lothus.sync.stats.games.addons.slaughter.Slaughter;
import com.lothus.sync.stats.platform.Platform;
import com.lothus.sync.stats.player.games.bedwars.BedPlayer;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Arrays;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

public class DamageListener implements Listener {

    private final GameManager gameManager;
    private final Arena arena;

    public DamageListener() {
        gameManager = Bedwars.getInstance().getGameManager();
        arena = gameManager.getArena();
    }

    @EventHandler
    public void death(PlayerDeathEvent event) {
        Player entity = event.getEntity();

        if (entity != null) {
            User entityUser = arena.getUserService().get(entity.getUniqueId());
            Player killer = entity.getKiller();
            String message = entityUser.getTeam().getTeamColor().getColoredName().substring(0, 2) + entity.getName() + " §7morreu." + (entityUser.getTeam().isBedBroken() ? " §b§lKILL FINAL!" : "");

            if (!entityUser.getTeam().isBedBroken()) {
                entityUser.setDead(true);
            } else {
                entityUser.setSpectator(true);
            }
            if (entityUser.getUserUpgrades().getPickLevel() > 1) {
                entityUser.getUserUpgrades().setPickLevel((entityUser.getUserUpgrades().getPickLevel() - 1));
            }
            if (entityUser.getUserUpgrades().getAxeLevel() > 1) {
                entityUser.getUserUpgrades().setAxeLevel((entityUser.getUserUpgrades().getAxeLevel() - 1));
            }
            new BukkitRunnable() {
                @Override
                public void run() {
                    entity.spigot().respawn();
                }
            }.runTask(Bedwars.getInstance());

            Despawnable despawnable = null;

            if (entityUser.getLastDamage() != null && (System.currentTimeMillis() - entityUser.getLastDamageTick()) <= TimeUnit.SECONDS.toMillis(3)) {
                if (!(entityUser.getLastDamage() instanceof Player)) {
                    despawnable = Bedwars.getInstance().getGameManager().getDespawnables().values().stream().filter(d -> d.getEntity().getEntityId() == entityUser.getLastDamage().getEntityId()).findAny().orElse(null);

                    if (despawnable != null) {
                        killer = despawnable.getTeam().getPlayers().get(0).getPlayer();
                    }
                }
            }

            if (killer != null) {
                User killerUser = arena.getUserService().get(killer.getUniqueId());
                BedPlayer bedPlayer = Platform.getBedPlatform().getBedPlayerController().getAccount(killer.getUniqueId());
                BedPlayer bedPlayerEntity = Platform.getBedPlatform().getBedPlayerController().getAccount(entity.getUniqueId());
                killerUser.addKill();
                killerUser.addCoins(killerUser.getTeam().isBedBroken() ? 10 : 5);
                killerUser.addXp(killerUser.getTeam().isBedBroken() ? 10 : 5);

                if (arena.getGameInfo().getRoomType() == RoomType.RANQUEADO) {
                    killerUser.addPoints((entityUser.getTeam().isBedBroken() ? 20 : 10));
                    entityUser.removePoints((entityUser.getTeam().isBedBroken() ? 5 : 2));
                }

                if (entityUser.isSpectator()) {
                    killerUser.addFinalKill();
                }
                long iron = 0, gold = 0, diamond = 0, emerald = 0;

                for (ItemStack item : Arrays.stream(entity.getInventory().getContents()).filter(item -> item != null && item.getType() == Material.IRON_INGOT).collect(Collectors.toSet())) {
                    iron += item.getAmount();
                }
                for (ItemStack item : Arrays.stream(entity.getInventory().getContents()).filter(item -> item != null && item.getType() == Material.GOLD_INGOT).collect(Collectors.toSet())) {
                    gold += item.getAmount();
                }
                for (ItemStack item : Arrays.stream(entity.getInventory().getContents()).filter(item -> item != null && item.getType() == Material.DIAMOND).collect(Collectors.toSet())) {
                    diamond += item.getAmount();
                }
                for (ItemStack item : Arrays.stream(entity.getInventory().getContents()).filter(item -> item != null && item.getType() == Material.EMERALD).collect(Collectors.toSet())) {
                    emerald += item.getAmount();
                }
                if (iron > 0) {
                    killer.sendMessage("§f+" + iron + " ferros");
                    killer.getInventory().addItem(new ItemStack(Material.IRON_INGOT, (int) iron));
                }
                if (gold > 0) {
                    killer.sendMessage("§6+" + gold + " ouros");
                    killer.getInventory().addItem(new ItemStack(Material.GOLD_INGOT, (int) gold));
                }
                if (diamond > 0) {
                    killer.sendMessage("§b+" + diamond + " diamantes");
                    killer.getInventory().addItem(new ItemStack(Material.DIAMOND, (int) diamond));
                }
                if (emerald > 0) {
                    killer.sendMessage("§a+" + emerald + " esmeraldas");
                    killer.getInventory().addItem(new ItemStack(Material.EMERALD, (int) emerald));
                }

                if (arena.getGameInfo().getRoomType() == RoomType.RANQUEADO) {
                    entity.sendMessage("§c-" + (entityUser.getTeam().isBedBroken() ? 5 : 2) + " pontos");
                }

                DeathCry d = Platform.getDeathController().getKit(bedPlayerEntity.getDeathCry());

                if (d != null) {
                    d.playSound(killer);
                } else {
                    killer.playSound(killer.getLocation(), Sound.NOTE_PLING, 2.0f, 2.0f);
                }

                String defaultMessage = "{player} §efoi morto por {killer}§e.";
                String entityNameWithColor = entityUser.getTeam().getTeamColor().getColoredName().substring(0, 2) + entity.getName();
                String killerNameWithColor = killerUser.getTeam().getTeamColor().getColoredName().substring(0, 2) + killer.getName();
                String despawnableName = despawnable != null ? "(" + despawnable.getEntity().getType().name() + ")" : "";
                String killFinal = (entityUser.getTeam().isBedBroken() ? " §b§lKILL FINAL!" : "");

                Slaughter slaughter = Platform.getSlaughterController().getKit(bedPlayer.getSlaughter());

                if (slaughter == null) {
                    message = defaultMessage.replace("{player}", entityNameWithColor).replace("{killer}", killerNameWithColor + despawnableName + killFinal);
                } else {
                    message = slaughter.getMessage().replace("{killer}", killerNameWithColor).replace("{dead}", entityNameWithColor) + despawnableName + killFinal;
                }
            }
            event.setKeepInventory(true);
            event.setDeathMessage(null);

            Bukkit.broadcastMessage(message);
            Bukkit.getScheduler().scheduleSyncDelayedTask(Bedwars.getInstance(), () -> {
                entity.getInventory().clear();
                entity.getInventory().setArmorContents(null);
            }, 1L);

            if (entityUser.isSpectator()) {
                entityUser.getTeam().eliminateTeam();
            }
            gameManager.getUserService().all().stream().filter(user -> user.getTracking() != null && !user.getTracking().isAlive()).forEach(user -> user.setTracking(null));

            Bukkit.getScheduler().scheduleSyncDelayedTask(Bedwars.getInstance(), gameManager::checkWinner, 10L);
        }
    }

    @EventHandler
    public void damageByEntity(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player) {
            if(event.getEntity() instanceof Player) {
                User user = arena.getUserService().get(event.getEntity().getUniqueId());

                if (user == null || user.getTeam() == null) {
                    event.setCancelled(true);
                    return;
                }
                if (user.getTeam().getPlayers().stream().anyMatch(u -> u.getPlayer().getUniqueId().equals(event.getDamager().getUniqueId()))) {
                    event.setCancelled(true);
                }
                if (!user.isPlaying() || ((Player) event.getDamager()).getGameMode() == GameMode.ADVENTURE) {
                    event.setCancelled(true);
                }
            }
            User user = arena.getUserService().get(event.getDamager().getUniqueId());

            if (user == null || user.getTeam() == null) {
                event.setCancelled(true);
                return;
            }
            if(user.isDead() || user.isSpectator()){
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void damageSpectator(EntityDamageEvent event) {
        if (arena.getGameInfo().getState() != GameState.EM_JOGO) {
            event.setCancelled(true);
        }
        if (event.getEntity() instanceof Player) {
            User user = arena.getUserService().get(event.getEntity().getUniqueId());

            if (user.isSpectator() || user.isDead()) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void respawn(PlayerRespawnEvent event) {
        User user = arena.getUserService().get(event.getPlayer().getUniqueId());

        if (user.isSpectator()) {
            Bukkit.getScheduler().scheduleSyncDelayedTask(Bedwars.getInstance(), () -> {
                gameManager.spectate(user);
//                gameManager.generateVillagerHolo(user.getPlayer());
                gameManager.getServerGenerators().getContainsHolo().remove(event.getPlayer());
            }, 10L);
        } else {
            Bukkit.getScheduler().scheduleSyncDelayedTask(Bedwars.getInstance(), () -> {
                gameManager.respawn(user);
//                gameManager.generateVillagerHolo(user.getPlayer());
                gameManager.getServerGenerators().getContainsHolo().remove(event.getPlayer());
            }, 10L);
        }
        event.setRespawnLocation(arena.getLobby().clone().subtract(0, 6, 0));
    }

    @EventHandler
    public void fallVoid(PlayerMoveEvent event) {
        if (arena.getGameInfo().getState() == GameState.EM_JOGO) {
            Player player = event.getPlayer();
            User user = arena.getUserService().get(player.getUniqueId());

            if (player.getLocation().getY() <= 0) {
                if (user.isSpectator() || user.isDead()) {
                    player.teleport(player.getWorld().getSpawnLocation());
                    return;
                }
                if (player.getGameMode() == GameMode.SURVIVAL) {
                    player.damage((player.getMaxHealth() + 100), player.getKiller());
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void quit(PlayerQuitEvent event) {
        gameManager.checkWinner();
    }

}
