package com.lothus.engines.bedwars.listener.server.inventory;

import com.lothus.core.games.state.GameState;
import com.lothus.core.utils.bukkit.ItemCreator;
import com.lothus.engines.bedwars.Bedwars;
import com.lothus.engines.bedwars.arena.Arena;
import com.lothus.engines.bedwars.user.User;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.*;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;

public class InventoryListener implements Listener {

    private final Arena arena;

    public InventoryListener() {
        arena = Bedwars.getInstance().getGameManager().getArena();
    }

    @EventHandler
    public void inventoryInteract(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            if (event.getSlotType() == null) return;

            if (event.getSlotType() == InventoryType.SlotType.ARMOR) {
                event.setCancelled(true);
                return;
            }
            if (event.getClickedInventory() != null && event.getClickedInventory().getType() == InventoryType.PLAYER) {
                if (event.getWhoClicked().getGameMode() == GameMode.ADVENTURE) {
                    event.setCancelled(true);
                    return;
                }
                if (arena.getGameInfo().getState() == GameState.EM_JOGO) {
                    if (event.getWhoClicked().getOpenInventory().getTopInventory().getType().name().contains("CHEST")) {
                        if (event.getCursor().getType().name().contains("AXE") || event.getCurrentItem().getType().name().contains("AXE")) {
                            event.setCancelled(true);
                        }
                        if (event.getCursor().getType() == Material.SHEARS || event.getCurrentItem().getType() == Material.SHEARS) {
                            event.setCancelled(true);
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void giveSword(InventoryCloseEvent event) {
        Player player = (Player) event.getPlayer();

        if (player != null) {
            if (arena.getGameInfo().getState() == GameState.EM_JOGO) {
                if (player.getGameMode() == GameMode.SURVIVAL) {
                    User user = arena.getUserService().get(player.getUniqueId());

                    if (Arrays.stream(player.getInventory().getContents()).noneMatch(is -> is != null && is.getType().name().contains("SWORD"))) {
                        if (player.getGameMode() != GameMode.ADVENTURE) {
                            ItemStack itemStack;

                            if (!user.getTeam().getTeamUpgrades().isSharpness()) {
                                itemStack = new ItemStack(Material.WOOD_SWORD);
                                ItemMeta itemMeta = itemStack.getItemMeta();
                                itemMeta.spigot().setUnbreakable(true);
                                itemStack.setItemMeta(itemMeta);
                            } else {
                                itemStack = new ItemCreator(Material.WOOD_SWORD).addEnchant(Enchantment.DAMAGE_ALL, 1).build();
                                ItemMeta itemMeta = itemStack.getItemMeta();
                                itemMeta.spigot().setUnbreakable(true);
                                itemStack.setItemMeta(itemMeta);
                            }
                            player.getInventory().addItem(itemStack);
                        }
                    }
                }
            }
        }
    }

    @EventHandler
    public void craftItem(CraftItemEvent event) {
        event.setCancelled(true);
    }

    @EventHandler
    public void prepareCraft(PrepareItemCraftEvent event) {
        event.getInventory().setResult(null);
    }

    @EventHandler
    public void dropTool(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        User user = arena.getUserService().get(player.getUniqueId());

        if (event.getItemDrop().getItemStack().getType().name().contains("SWORD")) {
            if (event.getItemDrop().getItemStack().getType() == Material.WOOD_SWORD) {
                event.setCancelled(true);
                return;
            }
            if (Arrays.stream(player.getInventory().getContents()).noneMatch(itemStack -> itemStack != null && itemStack.getType().name().contains("SWORD"))) {
                ItemStack itemStack;

                if (!user.getTeam().getTeamUpgrades().isSharpness()) {
                    itemStack = new ItemStack(Material.WOOD_SWORD);
                    ItemMeta itemMeta = itemStack.getItemMeta();
                    itemMeta.spigot().setUnbreakable(true);
                    itemStack.setItemMeta(itemMeta);
                } else {
                    itemStack = new ItemCreator(Material.WOOD_SWORD).addEnchant(Enchantment.DAMAGE_ALL, 1).build();
                    ItemMeta itemMeta = itemStack.getItemMeta();
                    itemMeta.spigot().setUnbreakable(true);
                    itemStack.setItemMeta(itemMeta);
                }
                player.getInventory().addItem(itemStack);
            }
        }
        if (event.getItemDrop().getItemStack().getType().name().contains("AXE") || event.getItemDrop().getItemStack().getType() == Material.SHEARS ||
                event.getItemDrop().getItemStack().getType() == Material.COMPASS) {
            event.setCancelled(true);
        }
        if (player.getGameMode() == GameMode.ADVENTURE) {
            event.setCancelled(true);
        }
    }

}
