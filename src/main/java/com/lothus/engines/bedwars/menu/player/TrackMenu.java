package com.lothus.engines.bedwars.menu.player;

import com.lothus.core.utils.bukkit.ItemCreator;
import com.lothus.engines.bedwars.Bedwars;
import com.lothus.engines.bedwars.arena.Arena;
import com.lothus.engines.bedwars.team.Team;
import com.lothus.engines.bedwars.user.User;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

public class TrackMenu implements Listener {

    private final Arena arena;

    public TrackMenu(Arena arena) {
        this.arena = arena;

        Bukkit.getPluginManager().registerEvents(this, Bedwars.getInstance());
    }

    public void open(Player player) {
        long count = arena.getTeams().stream().filter(Team::isAlive).count();
        Inventory inventory = Bukkit.createInventory(null, (count < 5 ? 27 : 36), "Rastreador");
        User user = arena.getUserService().get(player.getUniqueId());

        arena.getTeams().stream().filter(team -> team != user.getTeam()).filter(Team::isAlive).forEach(team -> inventory.setItem(getEmptySlot(inventory), new ItemCreator(Material.WOOL).
                setId(team.getTeamColor().getWoolId()).
                setDisplayName("§aRastrear time " + team.getTeamColor().getColoredName().substring(0, 2) + team.getTeamColor().getNormalName()).
                setLore("§7Clique para rastrear o", "§7time " + team.getTeamColor().getNormalName() + "§7.").
                build()));

        player.openInventory(inventory);
    }

    @EventHandler
    public void inventoryClick(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            ItemStack itemStack = event.getCurrentItem();
            Player player = (Player) event.getWhoClicked();
            Inventory inventory = event.getInventory();

            if (inventory == null) return;

            if (inventory.getTitle().equals("Rastreador")) {
                event.setCancelled(true);

                if (itemStack == null) return;
                if (itemStack.getType() == Material.AIR) return;
                if (itemStack.getItemMeta() == null) return;
                if (itemStack.getItemMeta().getDisplayName() == null) return;

                User user = arena.getUserService().get(player.getUniqueId());

                arena.getTeams().stream().filter(t -> t.getTeamColor().getWoolId() == itemStack.getDurability()).findAny().ifPresent(team -> {
                    user.setTracking(team);

                    player.closeInventory();
                    player.playSound(player.getLocation(), Sound.LEVEL_UP, 1F, 1F);
                    player.sendMessage("§aVocê está rastreando o time " + team.getTeamColor().getColoredName().substring(0, 2) + team.getTeamColor().getNormalName() + "§a.");
                });
            }
        }
    }

    private int getEmptySlot(Inventory inventory) {
        int[] slots = {10, 12, 14, 16};

        for (int slot : slots) {
            if (inventory.getItem(slot) == null || inventory.getItem(slot).getType() == Material.AIR) {
                return slot;
            }
        }
        return 10;
    }

}