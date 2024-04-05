package com.lothus.engines.bedwars.service.team.impl;

import com.lothus.core.games.room.RoomType;
import com.lothus.engines.bedwars.Bedwars;
import com.lothus.engines.bedwars.team.Team;
import com.lothus.engines.bedwars.service.team.TeamService;
import com.lothus.engines.bedwars.team.color.TeamColor;

import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class TeamServiceImpl implements TeamService {

    private final Set<Team> teams = new HashSet<>();

    @Override
    public void create(Team model) {
        teams.add(model);
    }

    @Override
    public void remove(TeamColor s) {
        teams.remove(get(s));
    }

    @Override
    public Team get(TeamColor s) {
        return search(s).findAny().orElse(null);
    }

    @Override
    public Stream<Team> search(TeamColor s) {
        return teams.stream().filter(team -> team.getTeamColor() == s);
    }

    @Override
    public Set<Team> all() {
        boolean ranked = Bedwars.getInstance().getGameManager().getArena().getGameInfo().getRoomType() == RoomType.RANQUEADO;
        return (ranked ? teams.stream().filter(team -> team.getTeamColor().isRanked()).collect(Collectors.toSet()) : teams);
    }
}
