package com.lothus.engines.bedwars.scoreboard;

import com.lothus.core.Core;
import com.lothus.core.api.scoreboard.TScoreboard;
import com.lothus.core.games.room.RoomType;
import com.lothus.engines.bedwars.Bedwars;
import com.lothus.engines.bedwars.arena.Arena;
import com.lothus.engines.bedwars.team.Team;
import com.lothus.engines.bedwars.user.User;
import org.bukkit.entity.Player;

import java.util.stream.Collectors;

public class BedScore extends TScoreboard {

    private final User user;
    private final Arena arena;

    public BedScore(Player player) {
        super(player, "BEDWARS", Core.getServerInfo().getConfiguration().getScoreboardTitle().replace("&", "§"));

        this.arena = Bedwars.getInstance().getGameManager().getArena();
        this.user = arena.getUserService().get(player.getUniqueId());
    }

    @Override
    public void create() {
        score();
    }

    @Override
    public void update() {
        score();
    }

    private void score() {
        int time = arena.getTimer().getTime();

        setDisplayName(Core.getServerInfo().getConfiguration().getScoreboardTitle().replace("&", "§"));
        switch (arena.getGameInfo().getState()) {
            case ESPERANDO:
                setRow(0, "");
                setRow(1, "§fJogadores: §a" + arena.getUserService().playing().size() + "/" + arena.getGameInfo().getMaxPlayers());
                setRow(2, "");
                setRow(3, "§fAguardando...");
                setRow(4, "");
                setRow(5, "§fMapa: §a" + arena.getGameInfo().getDisplay());
                setRow(6, "§fModo: " + (arena.getGameInfo().getRoomType() == RoomType.RANQUEADO ? "§6" : "§7") + arena.getGameInfo().getRoomType().getName());
                setRow(7, "");
                setRow(8, Core.getServerInfo().getConfiguration().getScoreboardFooter().replace("&", "§"));
                break;
            case INICIANDO:
                setRow(0, "");
                setRow(1, "§fJogadores: §a" + arena.getUserService().playing().size() + "/" + arena.getGameInfo().getMaxPlayers());
                setRow(2, "");
                setRow(3, "§fIniciando em §a" + Bedwars.getInstance().getGameManager().formatTime(time));
                setRow(4, "");
                setRow(5, "§fMapa: §a" + arena.getGameInfo().getDisplay());
                setRow(6, "§fModo: " + (arena.getGameInfo().getRoomType() == RoomType.RANQUEADO ? "§6" : "§7") + arena.getGameInfo().getRoomType().getName());
                setRow(7, "");
                setRow(8, Core.getServerInfo().getConfiguration().getScoreboardFooter().replace("&", "§"));
                break;
            case EM_JOGO:
            case ENCERRANDO:
                setRow(0, "");

                if (time <= 300) {
                    setRow(1, "§fDiamante II em §a" + secondToMinutes((300 - time)));
                } else if (time > 301 && time <= 600) {
                    setRow(1, "§fEsmeralda II em §a" + secondToMinutes((600 - time)));
                } else if (time > 601 && time <= 900) {
                    setRow(1, "§fDiamante III em §a" + secondToMinutes((900 - time)));
                } else if (time > 901 && time <= 1200) {
                    setRow(1, "§fEsmeralda III em §a" + secondToMinutes((1200 - time)));
                } else if (time > 1201 && time <= 1500) {
                    setRow(1, "§fSem camas em §a" + secondToMinutes((1500 - time)));
                } else if (time > 1501 && time <= 1800) {
                    setRow(1, "§fFim de jogo em §a" + secondToMinutes((1800 - time)));
                } else {
                    setRow(1, "§fFim de jogo...");
                }
                setRow(2, "");

                int i = 3;

                for (Team team : (arena.getGameInfo().getRoomType() != RoomType.RANQUEADO ? arena.getTeams() : arena.getTeams().stream().filter(team -> team.getTeamColor().isRanked()).collect(Collectors.toList()))) {
                    setRow(i, team.getTeamColor().getColoredName().substring(0, 5) + " §f" + team.getTeamColor().getNormalName() + ": " +
                            (user.getTeam() != null ? (team.getTeamColor() == user.getTeam().getTeamColor() ? "§7(VOCÊ) " : "") : "") +
                            ((!team.isBedBroken() && team.getPlayers().stream().anyMatch(u -> u != null && u.getPlayer() != null) ? "§a✔" :
                                    (!team.isBedBroken() ? "§e✔" : (team.isAlive() ? "§a" + team.getPlayers().size() : "§c✖")))));
                    i++;
                }

                setRow(i, "");
                setRow((i+1), Core.getServerInfo().getConfiguration().getScoreboardFooter().replace("&", "§"));
                break;
        }
    }

    private String secondToMinutes(int segundos) {
        int minutos = segundos / 60;
        int segundosRestantes = segundos % 60;
        String formato = "%02d:%02d";
        return String.format(formato, minutos, segundosRestantes);
    }

}
