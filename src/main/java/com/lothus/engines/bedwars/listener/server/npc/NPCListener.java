package com.lothus.engines.bedwars.listener.server.npc;

import com.lothus.engines.bedwars.Bedwars;
import net.jitse.npclib.api.events.NPCInteractEvent;
import net.jitse.npclib.api.state.NPCSlot;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

public class NPCListener implements Listener {

    @EventHandler
    public void onOpenShop(NPCInteractEvent event) {
        if (event.getWhoClicked().getGameMode() != GameMode.ADVENTURE) {
            if (event.getNPC().getItem(NPCSlot.MAINHAND).getType() == Material.DIAMOND_SWORD) {
                Bedwars.getInstance().getGameManager().getMenus().getShopMenu().quickbuy(event.getWhoClicked());
            } else {
                Bedwars.getInstance().getGameManager().getMenus().getUpgradeMenu().open(event.getWhoClicked());
            }
        }
    }

}
