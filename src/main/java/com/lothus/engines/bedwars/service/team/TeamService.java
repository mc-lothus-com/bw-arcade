package com.lothus.engines.bedwars.service.team;

import com.lothus.engines.bedwars.team.Team;
import com.lothus.engines.bedwars.team.color.TeamColor;

import java.util.Set;
import java.util.stream.Stream;

public interface TeamService {

    void create(Team model);

    void remove(TeamColor s);

    Team get(TeamColor s);

    Stream<Team> search(TeamColor s);

    Set<Team> all();

}
