package com.lothus.engines.bedwars.listener.player.spectator;

import com.lothus.core.Core;
import com.lothus.core.api.tag.TagManager;
import com.lothus.core.games.state.GameState;
import com.lothus.core.player.LothPlayer;
import com.lothus.core.storage.redis.channels.RedisChannel;
import com.lothus.core.utils.bukkit.ItemCreator;
import com.lothus.engines.bedwars.Bedwars;
import com.lothus.engines.bedwars.arena.Arena;
import com.lothus.engines.bedwars.team.color.TeamColor;
import com.lothus.engines.bedwars.user.User;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class SpectatorListener implements Listener {

    private final Arena arena;

    public SpectatorListener() {
        arena = Bedwars.getInstance().getGameManager().getArena();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void join(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        User user = arena.getUserService().get(player.getUniqueId());
        LothPlayer lothPlayer = Core.getPlayerController().get(player.getUniqueId());

        if (arena.getGameInfo().getState() == GameState.ESPERANDO || arena.getGameInfo().getState() == GameState.INICIANDO) {
            if (arena.getUserService().playing().size() > arena.getGameInfo().getMaxPlayers() || lothPlayer.getPrefs().isVanish()) {
                user.setSpectator(true);

                arena.getPlaying().forEach(u -> u.getPlayer().hidePlayer(player));

                Bedwars.getInstance().getGameManager().spectate(user);
            } else {
                player.getInventory().setItem(8, new ItemCreator(Material.BED).setDisplayName("§cVoltar para o lobby").build());

                Bukkit.broadcastMessage(player.getDisplayName() + " §eentrou na partida. §7(" + arena.getUserService().playing().size() + "/" + arena.getGameInfo().getMaxPlayers() + ")");
            }
        } else {
            arena.getTeams().forEach(team -> {
                team.getShop().show(user.getPlayer());
                team.getUpgrade().show(user.getPlayer());
            });
            if (user.getTeam() != null && !user.getTeam().isBedBroken() && !user.isSpectator()) {
                TeamColor teamColor = user.getTeam().getTeamColor();

                user.setSpectator(false);
                user.setDead(true);
                user.setup();
                user.getBedScore().create();

                TagManager.setTag(player, teamColor.getColoredName().substring(0, 5) + teamColor.getColoredName().substring(0, 2) + " ", "", teamColor.getPosition());
                TagManager.update(player);

                Bedwars.getInstance().getGameManager().respawn(user);
//                Bedwars.getInstance().getGameManager().generateVillagerHolo(player);

                Bukkit.broadcastMessage(player.getDisplayName().substring(0, 2) + player.getName() + "§7 retornou.");
            } else {
                user.setSpectator(true);

                Bedwars.getInstance().getGameManager().spectate(user);
            }
        }
        arena.getGameInfo().setPlayers(arena.getUserService().playing().size());

        Core.getRedis().message(RedisChannel.GAME_UPDATE.name(), Core.getGson().toJson(arena.getGameInfo()));
    }

    @EventHandler
    public void interact(PlayerInteractEvent event) {
        Player player = event.getPlayer();

        if (player.getGameMode() == GameMode.ADVENTURE || arena.getUserService().get(player.getUniqueId()).isSpectator()) {
            event.setCancelled(true);
        }
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void quit(PlayerQuitEvent event) {
        Player player = event.getPlayer();

        if (arena.getGameInfo().getState() == GameState.ESPERANDO || arena.getGameInfo().getState() == GameState.INICIANDO) {
            if (player.getGameMode() == GameMode.SURVIVAL) {
                Bukkit.broadcastMessage(player.getDisplayName() + " §esaiu da partida. §7(" + arena.getUserService().playing().size() + "/" + arena.getGameInfo().getMaxPlayers() + ")");
            }
        } else {
            if (player.getGameMode() == GameMode.SURVIVAL) {
                Bukkit.broadcastMessage(player.getDisplayName().substring(0, 2) + player.getName() + "§7 desconectou.");
            }
        }

        if (arena.getGameInfo().getState() != GameState.ENCERRANDO) {
            Bedwars.getInstance().getGameManager().generateRejoin(player);
        }

        arena.getGameInfo().setPlayers(arena.getUserService().playing().size());

        Core.getRedis().message(RedisChannel.GAME_UPDATE.name(), Core.getGson().toJson(arena.getGameInfo()));
    }

}
