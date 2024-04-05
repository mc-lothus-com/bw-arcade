package com.lothus.engines.bedwars.menu.player;

import com.lothus.core.Core;
import com.lothus.core.player.LothPlayer;
import com.lothus.core.utils.bukkit.ItemCreator;
import com.lothus.engines.bedwars.Bedwars;
import com.lothus.engines.bedwars.arena.Arena;
import com.lothus.engines.bedwars.user.User;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class CompassMenu implements Listener {

    private final Arena arena;

    public CompassMenu(Arena arena) {
        this.arena = arena;

        Bukkit.getPluginManager().registerEvents(this, Bedwars.getInstance());
    }

    public void open(Player player) {
        int p = arena.getPlaying().size();
        Inventory inventory = Bukkit.createInventory(null, (p < 9 ? 9 : 9 * 2), "Bússola");

        int slot = -1;
        for (User players : arena.getPlaying()) {
            if (players == null || players.getPlayer() == null) continue;

            slot++;

            int health = (int) players.getPlayer().getHealth();
            LothPlayer lothPlayer = Core.getPlayerController().get(players.getPlayer().getUniqueId());
            inventory.setItem(slot, new ItemCreator(
                    Material.SKULL_ITEM,
                    (lothPlayer.getSocial().getFake().getName().equals(lothPlayer.getName()) ? lothPlayer.getGroup().getRank().getColor() + lothPlayer.getName() : lothPlayer.getSocial().getFake().getRank().getColor() + lothPlayer.getSocial().getFake().getName())
            ).withSkullOwner(players.getName())
                    .setLore(
                            "§fVida: §c" + health + "♥",
                            "§eClique para teleportar."
                    ).setAmount(1)
                    .setId(3).build());
        }
        player.openInventory(inventory);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Player player = (Player) event.getWhoClicked();
        Inventory inventory = event.getClickedInventory();
        ItemStack itemStack = event.getCurrentItem();

        if (inventory == null) return;
        if (itemStack == null) return;
        if (!inventory.getName().equalsIgnoreCase("Bússola")) return;

        event.setCancelled(true);

        if (itemStack.getType() == Material.AIR) return;

        for (User players : arena.getPlaying()) {
            if (itemStack.getItemMeta().getDisplayName().endsWith(players.getName())) {
                player.teleport(players.getPlayer());
                player.sendMessage("§aVocê foi teleportado para " + itemStack.getItemMeta().getDisplayName() + "§a.");
                player.closeInventory();
                return;
            }
        }
    }


}
