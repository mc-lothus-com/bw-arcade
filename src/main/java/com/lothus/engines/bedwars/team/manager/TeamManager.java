package com.lothus.engines.bedwars.team.manager;

import com.lothus.engines.bedwars.location.manager.LocationManager;
import com.lothus.engines.bedwars.service.team.TeamService;
import com.lothus.engines.bedwars.team.Team;
import com.lothus.engines.bedwars.team.color.TeamColor;
import com.lothus.engines.bedwars.upgrades.team.TeamUpgrades;
import com.lothus.engines.bedwars.service.Services;
import lombok.Getter;
import com.lothus.engines.bedwars.location.type.LocationType;

public class TeamManager {

    @Getter
    private final TeamService teamService;
    private final LocationManager locationManager;

    public TeamManager(LocationManager locationManager) {
        this.teamService = Services.get(TeamService.class);
        this.locationManager = locationManager;
    }

    public void load() {
        for (TeamColor value : TeamColor.values()) {
            teamService.create(new Team(locationManager.get(LocationType.BED, value),
                    locationManager.get(LocationType.SPAWN, value),
                    locationManager.get(LocationType.IRON_GENERATOR, value),
                    locationManager.get(LocationType.GOLD_GENERATOR, value),
                    locationManager.get(LocationType.SHOP_VILLAGER, value),
                    locationManager.get(LocationType.UPGRADE_VILLAGER, value),
                    value, new TeamUpgrades()));
        }
    }

}
