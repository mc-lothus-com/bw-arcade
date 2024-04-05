package com.lothus.engines.bedwars.arena;

import com.lothus.core.games.GameInfo;
import com.lothus.core.games.room.RoomType;
import com.lothus.engines.bedwars.game.task.TimerTask;
import com.lothus.engines.bedwars.service.team.TeamService;
import com.lothus.engines.bedwars.service.user.UserService;
import com.lothus.engines.bedwars.team.Team;
import com.lothus.engines.bedwars.upgrades.server.ServerUpgrades;
import com.lothus.engines.bedwars.user.User;
import com.lothus.engines.bedwars.service.Services;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Location;

import java.util.Set;
import java.util.stream.Collectors;

@Getter
public class Arena {

    private final int minPlayers;
    private final GameInfo gameInfo;
    private final ServerUpgrades serverUpgrades;
    private final TeamService teamService;
    private final UserService userService;

    @Setter
    private Location lobby;

    @Setter
    private TimerTask timer;

    @Setter
    private boolean bedsBroken;

    public Arena(int minPlayers, GameInfo gameInfo) {
        this.minPlayers = minPlayers;
        this.gameInfo = gameInfo;
        this.serverUpgrades = new ServerUpgrades();
        this.teamService = Services.get(TeamService.class);
        this.userService = Services.get(UserService.class);
    }

    public Set<Team> getTeams() {
        return (gameInfo.getRoomType() != RoomType.RANQUEADO ? teamService.all() : teamService.all().stream().filter(team -> team.getTeamColor().isRanked()).collect(Collectors.toSet()));
    }

    public Set<User> getPlaying() {
        return userService.playing();
    }

}
