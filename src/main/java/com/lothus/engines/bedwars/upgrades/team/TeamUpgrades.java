package com.lothus.engines.bedwars.upgrades.team;

import lombok.Data;

@Data
public class TeamUpgrades {

    private int protection = 0, haste = 0, forge = 0;
    private boolean sharpness, regen, trap;

}