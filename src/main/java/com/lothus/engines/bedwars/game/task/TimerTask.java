package com.lothus.engines.bedwars.game.task;

import com.lothus.core.Core;
import com.lothus.core.games.state.GameState;
import com.lothus.core.servers.type.ServerType;
import com.lothus.core.storage.redis.channels.RedisChannel;
import com.lothus.engines.bedwars.Bedwars;
import com.lothus.engines.bedwars.api.TaskChain;
import com.lothus.engines.bedwars.arena.Arena;
import com.lothus.engines.bedwars.event.server.TimeSecondEvent;
import com.lothus.engines.bedwars.game.GameManager;
import com.lothus.engines.bedwars.service.user.UserService;
import com.lothus.engines.bedwars.utils.PlayerUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.github.paperspigot.Title;

import static org.bukkit.Material.MAP;

public class TimerTask extends BukkitRunnable {

    private final Arena arena;
    private final UserService userService;
    private int time;

    public TimerTask(Arena arena, int time) {
        this.arena = arena;
        this.time = time;
        this.userService = arena.getUserService();

        runTaskTimer(Bedwars.getInstance(), 0L, 20L);
    }

    @Override
    public void run() {
        if (arena.getGameInfo().getState() == GameState.ESPERANDO) {
            if (userService.playing().size() >= arena.getMinPlayers()) {
                arena.getGameInfo().setState(GameState.INICIANDO);
            }
        } else if (arena.getGameInfo().getState() == GameState.INICIANDO) {
            if (userService.playing().size() >= arena.getMinPlayers()) {
                time--;

                if (time > 11 && userService.playing().size() >= arena.getGameInfo().getMaxPlayers()) {
                    time = 10;
                }

                if (time % 30 == 0 && time > 20) {
                    sendTimerMessage();
                    sendTitleMessage();
                }

                if (time % 10 == 0 && time < 25) {
                    sendTimerMessage();
                    sendTitleMessage();
                }

                if (time < 10 && time > 0) {
                    sendTimerMessage();
                    sendTitleMessage();
                }
                if (time == 0) {
                    arena.getGameInfo().setState(GameState.EM_JOGO);

                    Bedwars.getInstance().getGameManager().selectTeams();
                    Bukkit.getOnlinePlayers().forEach(player -> {
                        player.sendTitle(new Title("§6§lINICIOU", "", 20, 20, 20));
                        player.playSound(player.getLocation(), Sound.LEVEL_UP, 1F, 1F);
                    });
                }
            } else {
                time = 30;
            }
        }
        if (arena.getGameInfo().getState() == GameState.EM_JOGO) {
            time++;

            if (time == 5) {
                TaskChain.newChain().add(new TaskChain.GenericTask() {
                    @Override
                    protected void run() {
                        Bedwars.getInstance().getGameManager().removeWaitRoom();
                    }
                }).execute();
            }

            if (userService.playing().size() < 1) {
                cancel();

                Bukkit.getOnlinePlayers().forEach(player -> PlayerUtil.send(player, ServerType.LOBBY_BEDWARS));
                Bukkit.getScheduler().scheduleSyncDelayedTask(Bedwars.getInstance(), () -> Bukkit.spigot().restart(), 20L * 2);
                return;
            }
            if (time == 300) {
                arena.getServerUpgrades().setDiamondTier(2);

                Bedwars.getInstance().getGameManager().getServerGenerators().upgradeGenerator();
                Bukkit.getOnlinePlayers().forEach(player -> player.playSound(player.getLocation(), Sound.LEVEL_UP, 1F, 1F));
                Bukkit.broadcastMessage("§aOs geradores de diamante evoluíram para o nível II.");
            } else if (time == 600) {
                arena.getServerUpgrades().setEmeraldTier(2);

                Bedwars.getInstance().getGameManager().getServerGenerators().upgradeGenerator();
                Bukkit.getOnlinePlayers().forEach(player -> player.playSound(player.getLocation(), Sound.LEVEL_UP, 1F, 1F));
                Bukkit.broadcastMessage("§aOs geradores de esmeralda evoluíram para o nível II.");
            } else if (time == 900) {
                arena.getServerUpgrades().setDiamondTier(3);

                Bedwars.getInstance().getGameManager().getServerGenerators().upgradeGenerator();
                Bukkit.getOnlinePlayers().forEach(player -> player.playSound(player.getLocation(), Sound.LEVEL_UP, 1F, 1F));
                Bukkit.broadcastMessage("§aOs geradores de diamante evoluíram para o nível III.");
            } else if (time == 1200) {
                arena.getServerUpgrades().setEmeraldTier(3);

                Bedwars.getInstance().getGameManager().getServerGenerators().upgradeGenerator();
                Bukkit.getOnlinePlayers().forEach(player -> player.playSound(player.getLocation(), Sound.LEVEL_UP, 1F, 1F));
                Bukkit.broadcastMessage("§aOs geradores de esmeralda evoluíram para o nível III.");
            } else if (time == 1500) {
                arena.getTeams().forEach(team -> team.breakBed(null));
                arena.setBedsBroken(true);

                Bukkit.getOnlinePlayers().forEach(player -> player.playSound(player.getLocation(), Sound.ENDERDRAGON_GROWL, 1F, 1F));
                Bukkit.broadcastMessage("");
                Bukkit.broadcastMessage("§c§lCAMA QUEBRADA §f» §7Todas as camas foram quebradas.");
                Bukkit.broadcastMessage("");
            } else if (time == 1800) {
                World world = Bukkit.getWorld("world");

                world.spawnEntity(world.getSpawnLocation(), EntityType.ENDER_DRAGON);
            }
            Bukkit.getPluginManager().callEvent(new TimeSecondEvent());
        }
        userService.all().forEach(u -> u.getBedScore().update());
        Core.getRedis().message(RedisChannel.GAME_UPDATE.name(), Core.getGson().toJson(arena.getGameInfo()));
    }

    private void sendTimerMessage() {
        if (time > 0) {
            Bukkit.broadcastMessage("§aO jogo iniciará em §f" + time + "§a " + (time > 1 ? "segundos" : "segundo") + "!");
        }
    }

    private void sendTitleMessage() {
        if (time > 15) {
            Bukkit.getOnlinePlayers().forEach(player -> {
                player.playSound(player.getLocation(), Sound.NOTE_PLING, 1f, 1f);
                player.sendTitle(new Title("§a§l" + time, "", 20, 20, 20));
            });
        } else if (time > 5) {
            Bukkit.getOnlinePlayers().forEach(player -> {
                player.playSound(player.getLocation(), Sound.NOTE_PLING, 1f, 1f);
                player.sendTitle(new Title("§e§l" + time, "", 20, 20, 20));
            });
        } else {
            Bukkit.getOnlinePlayers().forEach(player -> {
                player.playSound(player.getLocation(), Sound.NOTE_PLING, 1f, 1f);
                player.sendTitle(new Title("§c§l" + time, "", 20, 20, 20));
            });
        }
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }
}
