package com.lothus.engines.bedwars.listener.server.chat;

import com.lothus.bukkit.events.chat.CoreChatEvent;
import com.lothus.core.Core;
import com.lothus.core.player.LothPlayer;
import com.lothus.core.player.group.rank.Rank;
import com.lothus.engines.bedwars.service.user.UserService;
import com.lothus.engines.bedwars.user.User;
import com.lothus.engines.bedwars.Bedwars;
import com.lothus.engines.bedwars.arena.Arena;
import com.lothus.engines.bedwars.team.color.TeamColor;
import com.lothus.sync.stats.platform.Platform;
import com.lothus.sync.stats.player.games.bedwars.BedPlayer;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class AsyncChatListener implements Listener {

    private final Arena arena;
    private final UserService userService;

    public AsyncChatListener() {
        arena = Bedwars.getInstance().getGameManager().getArena();
        userService = arena.getUserService();
    }

    @EventHandler
    public void onAsyncChat(CoreChatEvent event) {
        Player player = event.getPlayer();

        LothPlayer lothPlayer = event.getAccount();

        if (arena == null) {
            return;
        }
        String message = (lothPlayer.getGroup().getRank().ordinal() <= Rank.VIP.ordinal() ? event.getMessage().replaceAll("&", "§") : event.getMessage());

        if (!(lothPlayer.getGroup().getRank().ordinal() <= Rank.GER.ordinal())) {
            if (!Core.getServerInfo().getConfiguration().isChat()) {
                player.sendMessage("§cO chat está desativado no momento.");
                return;
            }
        }

        User user = userService.get(player.getUniqueId());
        TeamColor teamColor;

        if (user.getTeam() != null) {
            teamColor = user.getTeam().getTeamColor();
        } else {
            teamColor = null;
        }
        if (user.isSpectator()) {
            userService.spectators().forEach(spectator -> {
                if (lothPlayer.getSocial().getFake() == null || lothPlayer.getSocial().getFake().getName().equalsIgnoreCase(lothPlayer.getName())) {
                    spectator.getPlayer().sendMessage("§8[E] " + lothPlayer.getMedal().getColor() + lothPlayer.getMedal().getSymbol() + " " + (lothPlayer.getGroup().getTag() == Rank.MEMBRO ? "§7" : lothPlayer.getGroup().getTag().getColor() + "§l" + lothPlayer.getGroup().getTag().getName().toUpperCase() + " " + lothPlayer.getGroup().getTag().getColor()) + player.getName() + ": §7" + message);
                } else {
                    spectator.getPlayer().sendMessage("§8[E] " + lothPlayer.getMedal().getColor() + lothPlayer.getMedal().getSymbol() + " " + (lothPlayer.getSocial().getFake().getRank() == Rank.MEMBRO ? "§7" : lothPlayer.getSocial().getFake().getRank().getColor() + "§l" + lothPlayer.getSocial().getFake().getRank().getName().toUpperCase() + " " + lothPlayer.getSocial().getFake().getRank().getColor()) + player.getName() + ": §7" + message);
                }
            });
            return;
        }

        BedPlayer bedPlayer = Platform.getBedPlatform().getBedPlayerController().getAccount(player.getUniqueId());
        userService.playing().forEach(p -> {
            if (lothPlayer.getSocial().getFake() == null || lothPlayer.getSocial().getFake().getName().equalsIgnoreCase(lothPlayer.getName())) {
                p.getPlayer().sendMessage(bedPlayer.getLevelColor().getColor() + "[" + bedPlayer.getLevel() + "✧] " + (teamColor != null ? teamColor.getColoredName().substring(0, 2) + "[" + teamColor.getNormalName().charAt(0) + "] " : "") + lothPlayer.getMedal().getColor() + lothPlayer.getMedal().getSymbol() + " " + (lothPlayer.getGroup().getTag() == Rank.MEMBRO ? "§7" : lothPlayer.getGroup().getTag().getColor() + "§l" + lothPlayer.getGroup().getTag().getName().toUpperCase() + " " + lothPlayer.getGroup().getTag().getColor()) + player.getName() + ": §7" + message);
            } else {
                p.getPlayer().sendMessage(bedPlayer.getLevelColor().getColor() + "[" + bedPlayer.getLevel() + "✧] " + (teamColor != null ? teamColor.getColoredName().substring(0, 2) + "[" + teamColor.getNormalName().charAt(0) + "] " : "") + lothPlayer.getMedal().getColor() + lothPlayer.getMedal().getSymbol() + " " + (lothPlayer.getSocial().getFake().getRank() == Rank.MEMBRO ? "§7" : lothPlayer.getSocial().getFake().getRank().getColor() + "§l" + lothPlayer.getSocial().getFake().getRank().getName().toUpperCase() + " " + lothPlayer.getSocial().getFake().getRank().getColor()) + player.getName() + ": §7" + message);
            }
        });
        userService.spectators().forEach(s -> {
            if (lothPlayer.getSocial().getFake() == null || lothPlayer.getSocial().getFake().getName().equalsIgnoreCase(lothPlayer.getName())) {
                s.getPlayer().sendMessage(bedPlayer.getLevelColor().getColor() + "[" + bedPlayer.getLevel() + "✧] " + (teamColor != null ? teamColor.getColoredName().substring(0, 2) + "[" + teamColor.getNormalName().charAt(0) + "] " : "") + lothPlayer.getMedal().getColor() + lothPlayer.getMedal().getSymbol() + " " + (lothPlayer.getGroup().getTag() == Rank.MEMBRO ? "§7" : lothPlayer.getGroup().getTag().getColor() + "§l" + lothPlayer.getGroup().getTag().getName().toUpperCase() + " " + lothPlayer.getGroup().getTag().getColor()) + player.getName() + ": §7" + message);
            } else {
                s.getPlayer().sendMessage(bedPlayer.getLevelColor().getColor() + "[" + bedPlayer.getLevel() + "✧] " + (teamColor != null ? teamColor.getColoredName().substring(0, 2) + "[" + teamColor.getNormalName().charAt(0) + "] " : "") + lothPlayer.getMedal().getColor() + lothPlayer.getMedal().getSymbol() + " " + (lothPlayer.getSocial().getFake().getRank() == Rank.MEMBRO ? "§7" : lothPlayer.getSocial().getFake().getRank().getColor() + "§l" + lothPlayer.getSocial().getFake().getRank().getName().toUpperCase() + " " + lothPlayer.getSocial().getFake().getRank().getColor()) + player.getName() + ": §7" + message);
            }
        });
    }
}