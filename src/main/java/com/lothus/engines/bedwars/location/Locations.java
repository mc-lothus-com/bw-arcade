package com.lothus.engines.bedwars.location;

import com.lothus.engines.bedwars.location.type.LocationType;
import com.lothus.engines.bedwars.team.color.TeamColor;
import lombok.Getter;
import org.bukkit.Location;

@Getter
public class Locations {

    private final LocationType locationType;
    private final Location location;
    private final TeamColor teamColor;

    public Locations(LocationType locationType, Location location, TeamColor teamColor) {
        this.locationType = locationType;
        this.location = location;
        this.teamColor = teamColor;
    }

}
