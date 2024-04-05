package com.lothus.engines.bedwars.generator;

import lombok.AllArgsConstructor;
import lombok.Data;
import org.bukkit.Location;
import org.bukkit.Material;

@Data
@AllArgsConstructor
public class Generator {

    private Material material;
    private int cooldown;
    private String level;
    private Location location;

}
