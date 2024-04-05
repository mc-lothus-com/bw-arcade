package com.lothus.engines.bedwars.menu;

import com.lothus.engines.bedwars.arena.Arena;
import com.lothus.engines.bedwars.menu.player.CompassMenu;
import com.lothus.engines.bedwars.menu.player.TrackMenu;
import com.lothus.engines.bedwars.menu.server.ShopMenu;
import com.lothus.engines.bedwars.menu.server.UpgradeMenu;
import lombok.Getter;

@Getter
public class Menus {

    private final ShopMenu shopMenu;
    private final UpgradeMenu upgradeMenu;
    private final CompassMenu compassMenu;
    private final TrackMenu trackMenu;

    public Menus(Arena arena) {
        shopMenu = new ShopMenu(arena);
        upgradeMenu = new UpgradeMenu(arena);
        compassMenu = new CompassMenu(arena);
        trackMenu = new TrackMenu(arena);
    }
}
