package com.lothus.engines.bedwars.location.manager;

import com.lothus.engines.bedwars.utils.ConfigUtil;
import com.lothus.engines.bedwars.utils.LocationUtils;
import com.lothus.engines.bedwars.location.Locations;
import com.lothus.engines.bedwars.location.type.LocationType;
import com.lothus.engines.bedwars.team.color.TeamColor;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

public class LocationManager {

    private final Set<Locations> locations;
    private final ConfigUtil configUtil;

    public LocationManager() {
        locations = new HashSet<>();
        configUtil = new ConfigUtil("locations");
    }

    public void load() {
        if (configUtil.getConfigurationSection("locations") == null) return;

        configUtil.getConfigurationSection("locations").getKeys(false).forEach(s -> configUtil.getConfigurationSection("locations." + s).getKeys(false).forEach(s1 -> {
            String color = (configUtil.contains("locations." + s + "." + s1 + ".color") ? configUtil.getString("locations." + s + "." + s1 + ".color") : null);

            locations.add(new Locations(LocationType.valueOf(s1), LocationUtils.getLocation(configUtil.getString("locations." + s + "." + s1 + ".location")), (color != null ? TeamColor.valueOf(color) : null)));
        }));
    }

    public void put(Locations loc) {
        Locations location = get(loc.getLocationType(), loc.getTeamColor());

        if (location != null) {
            if (location.getLocationType() == LocationType.LOBBY || location.getLocation().getBlockX() == loc.getLocation().getBlockX() && location.getLocation().getBlockZ() == loc.getLocation().getBlockZ()) {
                locations.remove(location);
            }
        }
        locations.add(loc);

        configUtil.put("locations." + locations.size() + "." + loc.getLocationType().name() + ".location", LocationUtils.getData(loc.getLocation()));

        if (loc.getTeamColor() != null) {
            configUtil.put("locations." + locations.size() + "." + loc.getLocationType().name() + ".color", loc.getTeamColor().name());
        }
        configUtil.save();
    }

    public List<Locations> get(LocationType locationType) {
        return locations.stream().filter(loc -> loc.getLocationType() == locationType).collect(Collectors.toList());
    }

    public Locations get(LocationType locationType, TeamColor teamColor) {
        if (teamColor == null) {
            return locations.stream().filter(loc -> loc.getLocationType() == locationType).findAny().orElse(null);
        } else {
            return locations.stream().filter(loc -> loc.getLocationType() == locationType && loc.getTeamColor() == teamColor).findAny().orElse(null);
        }
    }

}
