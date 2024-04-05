package com.lothus.engines.bedwars.upgrades.server;

import lombok.Data;
import lombok.Getter;
import lombok.Setter;

@Data
public class ServerUpgrades {

    private int diamondTier;
    private int emeraldTier;

    public ServerUpgrades() {
        this.diamondTier = 1;
        this.emeraldTier = 1;
    }

}