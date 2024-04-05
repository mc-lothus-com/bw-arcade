package com.lothus.engines.bedwars.menu.server;

import com.lothus.core.games.room.RoomType;
import com.lothus.core.utils.bukkit.ItemCreator;
import com.lothus.engines.bedwars.Bedwars;
import com.lothus.engines.bedwars.arena.Arena;
import com.lothus.engines.bedwars.upgrades.user.UserUpgrades;
import com.lothus.engines.bedwars.user.User;
import net.minecraft.server.v1_8_R3.NBTTagCompound;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.PotionMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.List;

public class ShopMenu implements Listener {

    private final Arena arena;

    public ShopMenu(Arena arena) {
        this.arena = arena;

        Bukkit.getPluginManager().registerEvents(this, Bedwars.getInstance());
    }

    private final int[] slots = {19, 20, 21, 22, 23, 24, 25, 28, 29, 30, 31, 32, 33, 34, 37, 38, 39, 40, 41, 42, 43};

    public void quickbuy(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 54, "Compra rápida");
        User user = arena.getUserService().get(player.getUniqueId());
        UserUpgrades upgrades = user.getUserUpgrades();

        int ironCount = 0, goldCount = 0, emeraldCount = 0, diamondCount = 0;

        for (int i = 0; i < 32; i++) {
            ItemStack item = player.getInventory().getItem(i);

            if (item != null && item.getType() != Material.AIR) {
                if (item.getType() == Material.IRON_INGOT) {
                    ironCount += item.getAmount();
                }
                if (item.getType() == Material.GOLD_INGOT) {
                    goldCount += item.getAmount();
                }
                if (item.getType() == Material.DIAMOND) {
                    diamondCount += item.getAmount();
                }
                if (item.getType() == Material.EMERALD) {
                    emeraldCount += item.getAmount();
                }
            }
        }

        inventory.setItem(1, new ItemCreator(Material.WOOD).setDisplayName("§aBlocos").build());
        inventory.setItem(2, new ItemCreator(Material.STONE_SWORD).setDisplayName("§aCombate").build());
        inventory.setItem(3, new ItemCreator(Material.DIAMOND_BOOTS).setDisplayName("§aArmadura").build());
        inventory.setItem(4, new ItemCreator(Material.IRON_PICKAXE).setDisplayName("§aFerramentas").build());
        inventory.setItem(5, new ItemCreator(Material.BOW).setDisplayName("§aProjéteis").build());
        inventory.setItem(6, new ItemCreator(Material.BREWING_STAND_ITEM).setDisplayName("§aPoções").build());
        inventory.setItem(7, new ItemCreator(Material.TNT).setDisplayName("§aUtilitários").build());
        inventory.setItem(49, new ItemCreator(Material.NETHER_STAR).addEnchant(Enchantment.DURABILITY, 1).setDisplayName("§aCompra rápida").build());

        for (int i = 9; i < 18; i++) {
            inventory.setItem(i, new ItemCreator(Material.STAINED_GLASS_PANE).setId(15).setDisplayName("").build());
        }

        for (int slot : slots) {
            inventory.setItem(slot, new ItemCreator(Material.STAINED_GLASS_PANE).setId(14).setDisplayName("§cVazio").setLore("§7Clique em um item pressionando", "§7shift para adicionar na", "§7compra rápida.").build());
        }

        int finalIronCount = ironCount;
        int finalGoldCount = goldCount;
        int finalEmeraldCount = emeraldCount;

        user.getQuickbuy().forEach(itemStack -> {
            ItemMeta itemMeta = itemStack.getItemMeta();
            String displayName = itemMeta.getDisplayName();
            String s = itemStack.getItemMeta().getLore().get(0);
            Material material = getItemLore(s);
            int price = Integer.parseInt(s.substring(11, s.lastIndexOf(" ")));

            itemMeta.setDisplayName("§e" + (material == Material.IRON_INGOT ? (finalIronCount >= price ? "§a" : "§c") :
                    (material == Material.GOLD_INGOT ? (finalGoldCount >= price ? "§a" : "§c") : (finalEmeraldCount >= price ? "§a" : "§c"))) +
                    displayName.replace("§e", "").replace("§c", "").replace("§a", ""));
            itemStack.setItemMeta(itemMeta);

            if (itemStack.getType().name().contains("PICKAXE")) {
                ItemStack i = new ItemCreator(pickLevel((upgrades.getPickLevel() < 5 ? upgrades.getPickLevel() + 1 : 5))).build();
                net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(i);

                NBTTagCompound tag = new NBTTagCompound();
                tag.setInt("level", upgrades.getPickLevel());
                tag.setString("material", itemStack.getType().name());

                nmsItem.setTag(tag);

                i = CraftItemStack.asBukkitCopy(nmsItem);

                i.addEnchantment(Enchantment.DIG_SPEED, 1);
                ItemMeta meta = i.getItemMeta();

                meta.setDisplayName("§e" + (upgrades.getPickLevel() < 5 ? "§c" : "§a") + "Picareta " + convertNumberRomain(upgrades.getPickLevel() + 1));
                meta.setLore(
                        Arrays.asList(
                                "§7Preço: " + pickPrice((upgrades.getPickLevel() + 1 == 6 ? upgrades.getPickLevel() : upgrades.getPickLevel() + 1)),
                                "§1",
                                (upgrades.getPickLevel() < 5 ? "§7Clique para comprar." : "§cVocê já possui.")));
                i.setItemMeta(meta);

                inventory.setItem(getEmptySlot(inventory), i);
            } else if (itemStack.getType().name().contains("AXE")) {
                ItemStack i = new ItemCreator(axeLevel((upgrades.getAxeLevel() < 5 ? upgrades.getAxeLevel() + 1 : 5))).build();
                net.minecraft.server.v1_8_R3.ItemStack nmsItem = CraftItemStack.asNMSCopy(i);

                NBTTagCompound tag = new NBTTagCompound();
                tag.setInt("level", upgrades.getPickLevel());
                tag.setString("material", itemStack.getType().name());

                nmsItem.setTag(tag);

                i = CraftItemStack.asBukkitCopy(nmsItem);

                i.addEnchantment(Enchantment.DIG_SPEED, 1);
                ItemMeta meta = i.getItemMeta();

                meta.setDisplayName("§e" + (upgrades.getAxeLevel() < 5 ? "§c" : "§a") + "Machado " + convertNumberRomain(upgrades.getAxeLevel() + 1));
                meta.setLore(Arrays.asList("§7Preço: " + axePrice((upgrades.getAxeLevel() + 1 == 5 ? upgrades.getAxeLevel() : upgrades.getAxeLevel() + 1)), "§1", (upgrades.getAxeLevel() < 5 ? "§7Clique para comprar." : "§cVocê já possui.")));
                i.setItemMeta(meta);
                inventory.setItem(getEmptySlot(inventory), i);
            } else {
                inventory.setItem(getEmptySlot(inventory), itemStack);
            }
        });

        player.openInventory(inventory);
    }

    private int getEmptySlot(Inventory inventory) {
        for (int slot : slots) {
            if (inventory.getItem(slot) != null && inventory.getItem(slot).getType() == Material.STAINED_GLASS_PANE && inventory.getItem(slot).getDurability() == 14) {
                return slot;
            }
        }
        return 19;
    }

    public void blocks(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 54, "Loja - Blocos");

        int ironCount = 0, goldCount = 0, emeraldCount = 0, diamondCount = 0;

        for (int i = 0; i < 32; i++) {
            ItemStack item = player.getInventory().getItem(i);

            if (item != null && item.getType() != Material.AIR) {
                if (item.getType() == Material.IRON_INGOT) {
                    ironCount += item.getAmount();
                }
                if (item.getType() == Material.GOLD_INGOT) {
                    goldCount += item.getAmount();
                }
                if (item.getType() == Material.DIAMOND) {
                    diamondCount += item.getAmount();
                }
                if (item.getType() == Material.EMERALD) {
                    emeraldCount += item.getAmount();
                }
            }
        }
        inventory.setItem(1, new ItemCreator(Material.WOOD).addEnchant(Enchantment.DURABILITY, 1).setDisplayName("§aBlocos").build());
        inventory.setItem(2, new ItemCreator(Material.STONE_SWORD).setDisplayName("§aCombate").build());
        inventory.setItem(3, new ItemCreator(Material.DIAMOND_BOOTS).setDisplayName("§aArmadura").build());
        inventory.setItem(4, new ItemCreator(Material.IRON_PICKAXE).setDisplayName("§aFerramentas").build());
        inventory.setItem(5, new ItemCreator(Material.BOW).setDisplayName("§aProjéteis").build());
        inventory.setItem(6, new ItemCreator(Material.BREWING_STAND_ITEM).setDisplayName("§aPoções").build());
        inventory.setItem(7, new ItemCreator(Material.TNT).setDisplayName("§aUtilitários").build());
        inventory.setItem(49, new ItemCreator(Material.NETHER_STAR).setDisplayName("§aCompra rápida").build());

        for (int i = 9; i < 18; i++) {
            inventory.setItem(i, new ItemCreator(Material.STAINED_GLASS_PANE).setId(15).setDisplayName("").build());
        }

        inventory.setItem(19, new ItemCreator(Material.WOOL).setAmount(16).setDisplayName("§e" + (ironCount >= 4 ? "§a" : "§c") + "Lã").setLore("§7Preço: §f4 ferros§7.", "§1", "§eClique pressionando shift para", "§eadicionar na compra rápida.", " ", "§7Clique para comprar.").build());
        inventory.setItem(20, new ItemCreator(Material.STAINED_CLAY).setAmount(16).setDisplayName("§e" + (ironCount >= 12 ? "§a" : "§c") + "Argila").setLore("§7Preço: §f12 ferros§7.", "§1", "§eClique pressionando shift para", "§eadicionar na compra rápida.", " ", "§7Clique para comprar.").build());
        inventory.setItem(21, new ItemCreator(Material.GLASS).setAmount(4).setDisplayName("§e" + (ironCount >= 16 ? "§a" : "§c") + "Vidro").setLore("§7Preço: §f16 ferros§7.", "§1", "§eClique pressionando shift para", "§eadicionar na compra rápida.", " ", "§7Clique para comprar.").build());
        inventory.setItem(22, new ItemCreator(Material.ENDER_STONE).setAmount(12).setDisplayName("§e" + (ironCount >= 24 ? "§a" : "§c") + "Pedra do fim").setLore("§7Preço: §f24 ferros§7.", "§1", "§eClique pressionando shift para", "§eadicionar na compra rápida.", " ", "§7Clique para comprar.").build());
        inventory.setItem(23, new ItemCreator(Material.LADDER).setAmount(16).setDisplayName("§e" + (ironCount >= 4 ? "§a" : "§c") + "Escada").setLore("§7Preço: §f4 ferros§7.", "§1", "§eClique pressionando shift para", "§eadicionar na compra rápida.", " ", "§7Clique para comprar.").build());
        inventory.setItem(24, new ItemCreator(Material.WOOD).setAmount(16).setDisplayName("§e" + (goldCount >= 4 ? "§a" : "§c") + "Madeira").setLore("§7Preço: §64 ouros§7.", "§1", "§eClique pressionando shift para", "§eadicionar na compra rápida.", " ", "§7Clique para comprar.").build());
        if (arena.getGameInfo().getRoomType() != RoomType.RANQUEADO) {
            inventory.setItem(25, new ItemCreator(Material.OBSIDIAN).setAmount(4).setDisplayName("§e" + (emeraldCount >= 4 ? "§a" : "§c") + "Obsidiana").setLore("§7Preço: §24 esmeraldas§7.", "§1", "§eClique pressionando shift para", "§eadicionar na compra rápida.", " ", "§7Clique para comprar.").build());
        }
        player.openInventory(inventory);
    }

    public void combat(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 54, "Loja - Combate");

        int ironCount = 0, goldCount = 0, emeraldCount = 0, diamondCount = 0;

        for (int i = 0; i < 32; i++) {
            ItemStack item = player.getInventory().getItem(i);

            if (item != null && item.getType() != Material.AIR) {
                if (item.getType() == Material.IRON_INGOT) {
                    ironCount += item.getAmount();
                }
                if (item.getType() == Material.GOLD_INGOT) {
                    goldCount += item.getAmount();
                }
                if (item.getType() == Material.DIAMOND) {
                    diamondCount += item.getAmount();
                }
                if (item.getType() == Material.EMERALD) {
                    emeraldCount += item.getAmount();
                }
            }
        }
        inventory.setItem(1, new ItemCreator(Material.WOOD).setDisplayName("§aBlocos").build());
        inventory.setItem(2, new ItemCreator(Material.STONE_SWORD).addEnchant(Enchantment.DURABILITY, 1).setDisplayName("§aCombate").build());
        inventory.setItem(3, new ItemCreator(Material.DIAMOND_BOOTS).setDisplayName("§aArmadura").build());
        inventory.setItem(4, new ItemCreator(Material.IRON_PICKAXE).setDisplayName("§aFerramentas").build());
        inventory.setItem(5, new ItemCreator(Material.BOW).setDisplayName("§aProjéteis").build());
        inventory.setItem(6, new ItemCreator(Material.BREWING_STAND_ITEM).setDisplayName("§aPoções").build());
        inventory.setItem(7, new ItemCreator(Material.TNT).setDisplayName("§aUtilitários").build());
        inventory.setItem(49, new ItemCreator(Material.NETHER_STAR).setDisplayName("§aCompra rápida").build());

        for (int i = 9; i < 18; i++) {
            inventory.setItem(i, new ItemCreator(Material.STAINED_GLASS_PANE).setId(15).setDisplayName("").build());
        }

        inventory.setItem(19, new ItemCreator(Material.STONE_SWORD).setDisplayName("§e" + (ironCount >= 10 ? "§a" : "§c") + "Espada de pedra").setLore("§7Preço: §f10 ferros§7.", "§1", "§eClique pressionando shift para", "§eadicionar na compra rápida.", " ", "§7Clique para comprar.").build());
        inventory.setItem(20, new ItemCreator(Material.IRON_SWORD).setDisplayName("§e" + (goldCount >= 7 ? "§a" : "§c") + "Espada de ferro").setLore("§7Preço: §67 ouros§7.", "§1", "§eClique pressionando shift para", "§eadicionar na compra rápida.", " ", "§7Clique para comprar.").build());
        inventory.setItem(21, new ItemCreator(Material.DIAMOND_SWORD).setDisplayName("§e" + (emeraldCount >= 4 ? "§a" : "§c") + "Espada de diamante").setLore("§7Preço: §24 esmeraldas§7.", "§1", "§eClique pressionando shift para", "§eadicionar na compra rápida.", " ", "§7Clique para comprar.").build());
        inventory.setItem(22, new ItemCreator(Material.STICK).addEnchant(Enchantment.KNOCKBACK, 1).setDisplayName("§e" + (goldCount >= 5 ? "§a" : "§c") + "Graveto").setLore("§7Preço: §65 ouros.", "§1", "§eClique pressionando shift para", "§eadicionar na compra rápida.", " ", "§7Clique para comprar.").build());

        player.openInventory(inventory);
    }

    public void armor(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 54, "Loja - Armadura");
        UserUpgrades upgrades = arena.getUserService().get(player.getUniqueId()).getUserUpgrades();

        int ironCount = 0, goldCount = 0, emeraldCount = 0, diamondCount = 0;

        for (int i = 0; i < 32; i++) {
            ItemStack item = player.getInventory().getItem(i);

            if (item != null && item.getType() != Material.AIR) {
                if (item.getType() == Material.IRON_INGOT) {
                    ironCount += item.getAmount();
                }
                if (item.getType() == Material.GOLD_INGOT) {
                    goldCount += item.getAmount();
                }
                if (item.getType() == Material.DIAMOND) {
                    diamondCount += item.getAmount();
                }
                if (item.getType() == Material.EMERALD) {
                    emeraldCount += item.getAmount();
                }
            }
        }
        inventory.setItem(1, new ItemCreator(Material.WOOD).setDisplayName("§aBlocos").build());
        inventory.setItem(2, new ItemCreator(Material.STONE_SWORD).setDisplayName("§aCombate").build());
        inventory.setItem(3, new ItemCreator(Material.DIAMOND_BOOTS).addEnchant(Enchantment.DURABILITY, 1).setDisplayName("§aArmadura").build());
        inventory.setItem(4, new ItemCreator(Material.IRON_PICKAXE).setDisplayName("§aFerramentas").build());
        inventory.setItem(5, new ItemCreator(Material.BOW).setDisplayName("§aProjéteis").build());
        inventory.setItem(6, new ItemCreator(Material.BREWING_STAND_ITEM).setDisplayName("§aPoções").build());
        inventory.setItem(7, new ItemCreator(Material.TNT).setDisplayName("§aUtilitários").build());
        inventory.setItem(49, new ItemCreator(Material.NETHER_STAR).setDisplayName("§aCompra rápida").build());

        for (int i = 9; i < 18; i++) {
            inventory.setItem(i, new ItemCreator(Material.STAINED_GLASS_PANE).setId(15).setDisplayName("").build());
        }

        inventory.setItem(19, new ItemCreator(Material.CHAINMAIL_BOOTS).setDisplayName("§e" + (ironCount >= 20 ? "§a" : "§c") + "Armadura de malha").setLore("§7Preço: §f20 ferros§7.", "§1", "§eClique pressionando shift para", "§eadicionar na compra rápida.", " ", (upgrades.getArmorLevel() < 1 ? "§7Clique para comprar." : "§cVocê já possui.")).build());
        inventory.setItem(20, new ItemCreator(Material.IRON_BOOTS).setDisplayName("§e" + (goldCount >= 12 ? "§a" : "§c") + "Armadura de ferro").setLore("§7Preço: §612 ouros§7.", "§1", "§eClique pressionando shift para", "§eadicionar na compra rápida.", " ", (upgrades.getArmorLevel() < 2 ? "§7Clique para comprar." : "§cVocê já possui.")).build());
        inventory.setItem(21, new ItemCreator(Material.DIAMOND_BOOTS).setDisplayName("§e" + (emeraldCount >= 6 ? "§a" : "§c") + "Armadura de diamante").setLore("§7Preço: §26 esmeraldas§7.", "§1", "§eClique pressionando shift para", "§eadicionar na compra rápida.", " ", (upgrades.getArmorLevel() < 3 ? "§7Clique para comprar." : "§cVocê já possui.")).build());

        player.openInventory(inventory);
    }

    public void tools(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 54, "Loja - Ferramentas");
        UserUpgrades upgrades = arena.getUserService().get(player.getUniqueId()).getUserUpgrades();

        int ironCount = 0, goldCount = 0, emeraldCount = 0, diamondCount = 0;

        for (int i = 0; i < 32; i++) {
            ItemStack item = player.getInventory().getItem(i);

            if (item != null && item.getType() != Material.AIR) {
                if (item.getType() == Material.IRON_INGOT) {
                    ironCount += item.getAmount();
                }
                if (item.getType() == Material.GOLD_INGOT) {
                    goldCount += item.getAmount();
                }
                if (item.getType() == Material.DIAMOND) {
                    diamondCount += item.getAmount();
                }
                if (item.getType() == Material.EMERALD) {
                    emeraldCount += item.getAmount();
                }
            }
        }
        inventory.setItem(1, new ItemCreator(Material.WOOD).setDisplayName("§aBlocos").build());
        inventory.setItem(2, new ItemCreator(Material.STONE_SWORD).setDisplayName("§aCombate").build());
        inventory.setItem(3, new ItemCreator(Material.DIAMOND_BOOTS).setDisplayName("§aArmadura").build());
        inventory.setItem(4, new ItemCreator(Material.IRON_PICKAXE).addEnchant(Enchantment.DURABILITY, 1).setDisplayName("§aFerramentas").build());
        inventory.setItem(5, new ItemCreator(Material.BOW).setDisplayName("§aProjéteis").build());
        inventory.setItem(6, new ItemCreator(Material.BREWING_STAND_ITEM).setDisplayName("§aPoções").build());
        inventory.setItem(7, new ItemCreator(Material.TNT).setDisplayName("§aUtilitários").build());
        inventory.setItem(49, new ItemCreator(Material.NETHER_STAR).setDisplayName("§aCompra rápida").build());

        for (int i = 9; i < 18; i++) {
            inventory.setItem(i, new ItemCreator(Material.STAINED_GLASS_PANE).setId(15).setDisplayName("").build());
        }

        inventory.setItem(19, new ItemCreator(Material.SHEARS).setDisplayName("§e" + (upgrades.isShears() ? "§a" : (ironCount >= 20 ? "§a" : "§c")) + "Tesoura").setLore("§7Preço: §f20 ferros§7.", "§1", (!upgrades.isShears() ? "§7Clique para comprar." : "§cVocê já possui.")).build());
        inventory.setItem(20, new ItemCreator(pickLevel((upgrades.getPickLevel() < 5 ? upgrades.getPickLevel() + 1 : 5))).addEnchant(Enchantment.DIG_SPEED, 1).setDisplayName("§e" + (upgrades.getPickLevel() < 5 ? "§c" : "§a") + "Picareta " + convertNumberRomain(upgrades.getPickLevel() + 1)).setLore("§7Preço: " + pickPrice((upgrades.getPickLevel() + 1)), "§1", (upgrades.getPickLevel() < 5 ? "§7Clique para comprar." : "§cVocê já possui.")).build());
        inventory.setItem(21, new ItemCreator(axeLevel((upgrades.getAxeLevel() < 5 ? upgrades.getAxeLevel() + 1 : 5))).addEnchant(Enchantment.DIG_SPEED, 1).setDisplayName("§e" + (upgrades.getAxeLevel() < 5 ? "§c" : "§a") + "Machado " + convertNumberRomain(upgrades.getAxeLevel() + 1)).setLore("§7Preço: " + axePrice((upgrades.getAxeLevel() + 1)), "§1", (upgrades.getAxeLevel() < 5 ? "§7Clique para comprar." : "§cVocê já possui.")).build());

        player.openInventory(inventory);
    }

    public void projectiles(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 54, "Loja - Projéteis");

        int ironCount = 0, goldCount = 0, emeraldCount = 0, diamondCount = 0;

        for (int i = 0; i < 32; i++) {
            ItemStack item = player.getInventory().getItem(i);

            if (item != null && item.getType() != Material.AIR) {
                if (item.getType() == Material.IRON_INGOT) {
                    ironCount += item.getAmount();
                }
                if (item.getType() == Material.GOLD_INGOT) {
                    goldCount += item.getAmount();
                }
                if (item.getType() == Material.DIAMOND) {
                    diamondCount += item.getAmount();
                }
                if (item.getType() == Material.EMERALD) {
                    emeraldCount += item.getAmount();
                }
            }
        }
        inventory.setItem(1, new ItemCreator(Material.WOOD).setDisplayName("§aBlocos").build());
        inventory.setItem(2, new ItemCreator(Material.STONE_SWORD).setDisplayName("§aCombate").build());
        inventory.setItem(3, new ItemCreator(Material.DIAMOND_BOOTS).setDisplayName("§aArmadura").build());
        inventory.setItem(4, new ItemCreator(Material.IRON_PICKAXE).setDisplayName("§aFerramentas").build());
        inventory.setItem(5, new ItemCreator(Material.BOW).addEnchant(Enchantment.DURABILITY, 1).setDisplayName("§aProjéteis").build());
        inventory.setItem(6, new ItemCreator(Material.BREWING_STAND_ITEM).setDisplayName("§aPoções").build());
        inventory.setItem(7, new ItemCreator(Material.TNT).setDisplayName("§aUtilitários").build());
        inventory.setItem(49, new ItemCreator(Material.NETHER_STAR).setDisplayName("§aCompra rápida").build());

        for (int i = 9; i < 18; i++) {
            inventory.setItem(i, new ItemCreator(Material.STAINED_GLASS_PANE).setId(15).setDisplayName("").build());
        }

        inventory.setItem(19, new ItemCreator(Material.ARROW).setAmount(8).setDisplayName("§e" + (goldCount >= 2 ? "§a" : "§c") + "Flechas").setLore("§7Preço: §62 ouros§7.", "§1", "§eClique pressionando shift para", "§eadicionar na compra rápida.", " ", "§7Clique para comprar.").build());
        inventory.setItem(20, new ItemCreator(Material.BOW).setDisplayName("§e" + (goldCount >= 12 ? "§a" : "§c") + "Arco").setLore("§7Preço: §612 ouros§7.", "§1", "§eClique pressionando shift para", "§eadicionar na compra rápida.", " ", "§7Clique para comprar.").build());
        inventory.setItem(21, new ItemCreator(Material.BOW).addEnchant(Enchantment.ARROW_DAMAGE, 1).setDisplayName("§e" + (goldCount >= 24 ? "§a" : "§c") + "Arco encantado").setLore("§7Preço: §624 ouros§7.", "§1", "§eClique pressionando shift para", "§eadicionar na compra rápida.", " ", "§7Clique para comprar.").build());
        inventory.setItem(22, new ItemCreator(Material.BOW).addEnchant(Enchantment.ARROW_DAMAGE, 1).addEnchant(Enchantment.ARROW_KNOCKBACK, 1).setDisplayName("§e" + (emeraldCount >= 6 ? "§a" : "§c") + "Arco encantado").setLore("§7Preço: §26 emeraldas§7.", "§1", "§eClique pressionando shift para", "§eadicionar na compra rápida.", " ", "§7Clique para comprar.").build());

        player.openInventory(inventory);
    }

    public void potions(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 54, "Loja - Poções");

        int ironCount = 0, goldCount = 0, emeraldCount = 0, diamondCount = 0;

        for (int i = 0; i < 32; i++) {
            ItemStack item = player.getInventory().getItem(i);

            if (item != null && item.getType() != Material.AIR) {
                if (item.getType() == Material.IRON_INGOT) {
                    ironCount += item.getAmount();
                }
                if (item.getType() == Material.GOLD_INGOT) {
                    goldCount += item.getAmount();
                }
                if (item.getType() == Material.DIAMOND) {
                    diamondCount += item.getAmount();
                }
                if (item.getType() == Material.EMERALD) {
                    emeraldCount += item.getAmount();
                }
            }
        }

        inventory.setItem(1, new ItemCreator(Material.WOOD).setDisplayName("§aBlocos").build());
        inventory.setItem(2, new ItemCreator(Material.STONE_SWORD).setDisplayName("§aCombate").build());
        inventory.setItem(3, new ItemCreator(Material.DIAMOND_BOOTS).setDisplayName("§aArmadura").build());
        inventory.setItem(4, new ItemCreator(Material.IRON_PICKAXE).setDisplayName("§aFerramentas").build());
        inventory.setItem(5, new ItemCreator(Material.BOW).setDisplayName("§aProjéteis").build());
        inventory.setItem(6, new ItemCreator(Material.BREWING_STAND_ITEM).addEnchant(Enchantment.DURABILITY, 1).setDisplayName("§aPoções").build());
        inventory.setItem(7, new ItemCreator(Material.TNT).setDisplayName("§aUtilitários").build());
        inventory.setItem(49, new ItemCreator(Material.NETHER_STAR).setDisplayName("§aCompra rápida").build());


        for (int i = 9; i < 18; i++) {
            inventory.setItem(i, new ItemCreator(Material.STAINED_GLASS_PANE).setId(15).setDisplayName("").build());
        }

        ItemStack speed = new ItemCreator(Material.POTION).setId(8194).setDisplayName("§e" + (emeraldCount >= 1 ? "§a" : "§c") + "Poção de agilidade").setLore("§7Preço: §21 esmeralda§7.", "§1", "§eClique pressionando shift para", "§eadicionar na compra rápida.", " ", "§7Clique para comprar.").build();
        PotionMeta meta = (PotionMeta) speed.getItemMeta();
        meta.clearCustomEffects();
        meta.addCustomEffect(new PotionEffect(PotionEffectType.SPEED, (20 * 30), 0), true);
        speed.setItemMeta(meta);

        ItemStack jump = new ItemCreator(Material.POTION).setId(8203).setDisplayName("§e" + (emeraldCount >= 1 ? "§a" : "§c") + "Poção de super pulo").setLore("§7Preço: §21 esmeralda§7.", "§1", "§eClique pressionando shift para", "§eadicionar na compra rápida.", " ", "§7Clique para comprar.").build();
        PotionMeta jumpMeta = (PotionMeta) jump.getItemMeta();
        jumpMeta.clearCustomEffects();
        jumpMeta.addCustomEffect(new PotionEffect(PotionEffectType.JUMP, (20 * 30), 0), true);
        jump.setItemMeta(jumpMeta);

        ItemStack inv = new ItemCreator(Material.POTION).setId(8238).setDisplayName("§e" + (emeraldCount >= 2 ? "§a" : "§c") + "Poção de invisibilidade").setLore("§7Preço: §22 esmeralda§7.", "§1", "§eClique pressionando shift para", "§eadicionar na compra rápida.", " ", "§7Clique para comprar.").build();
        PotionMeta invItemMeta = (PotionMeta) inv.getItemMeta();
        invItemMeta.clearCustomEffects();
        invItemMeta.addCustomEffect(new PotionEffect(PotionEffectType.INVISIBILITY, (20 * 30), 0), true);
        inv.setItemMeta(invItemMeta);

        inventory.setItem(19, speed);
        inventory.setItem(20, jump);
        inventory.setItem(21, inv);

        player.openInventory(inventory);
    }

    public void utility(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 54, "Loja - Utilitários");

        int ironCount = 0, goldCount = 0, emeraldCount = 0, diamondCount = 0;

        for (int i = 0; i < 32; i++) {
            ItemStack item = player.getInventory().getItem(i);

            if (item != null && item.getType() != Material.AIR) {
                if (item.getType() == Material.IRON_INGOT) {
                    ironCount += item.getAmount();
                }
                if (item.getType() == Material.GOLD_INGOT) {
                    goldCount += item.getAmount();
                }
                if (item.getType() == Material.DIAMOND) {
                    diamondCount += item.getAmount();
                }
                if (item.getType() == Material.EMERALD) {
                    emeraldCount += item.getAmount();
                }
            }
        }

        inventory.setItem(1, new ItemCreator(Material.WOOD).setDisplayName("§aBlocos").build());
        inventory.setItem(2, new ItemCreator(Material.STONE_SWORD).setDisplayName("§aCombate").build());
        inventory.setItem(3, new ItemCreator(Material.DIAMOND_BOOTS).setDisplayName("§aArmadura").build());
        inventory.setItem(4, new ItemCreator(Material.IRON_PICKAXE).setDisplayName("§aFerramentas").build());
        inventory.setItem(5, new ItemCreator(Material.BOW).setDisplayName("§aProjéteis").build());
        inventory.setItem(6, new ItemCreator(Material.BREWING_STAND_ITEM).setDisplayName("§aPoções").build());
        inventory.setItem(7, new ItemCreator(Material.TNT).addEnchant(Enchantment.DURABILITY, 1).setDisplayName("§aUtilitários").build());
        inventory.setItem(49, new ItemCreator(Material.NETHER_STAR).setDisplayName("§aCompra rápida").build());


        for (int i = 9; i < 18; i++) {
            inventory.setItem(i, new ItemCreator(Material.STAINED_GLASS_PANE).setId(15).setDisplayName("").build());
        }

        inventory.setItem(19, new ItemCreator(Material.GOLDEN_APPLE).setDisplayName("§e" + (goldCount >= 3 ? "§a" : "§c") + "Maçã dourada").setLore("§7Preço: §63 ouros§7.", "§1", "§eClique pressionando shift para", "§eadicionar na compra rápida.", " ", "§7Clique para comprar.").build());
        inventory.setItem(20, new ItemCreator(Material.SNOW_BALL).setDisplayName("§e" + (ironCount >= 20 ? "§a" : "§c") + "Traça").setLore("§7Preço: §f20 ferros§7.", "§1", "§eClique pressionando shift para", "§eadicionar na compra rápida.", " ", "§7Clique para comprar.").build());
        inventory.setItem(21, new ItemCreator(Material.FIREBALL).setDisplayName("§e" + (ironCount >= 40 ? "§a" : "§c") + "Bola de fogo").setLore("§7Preço: §f40 ferros§7.", "§1", "§eClique pressionando shift para", "§eadicionar na compra rápida.", " ", "§7Clique para comprar.").build());
        inventory.setItem(22, new ItemCreator(Material.MONSTER_EGG).setDisplayName("§e" + (ironCount >= 90 ? "§a" : "§c") + "Golem").setLore("§7Preço: §f90 ferros§7.", "§1", "§eClique pressionando shift para", "§eadicionar na compra rápida.", " ", "§7Clique para comprar.").build());
        inventory.setItem(23, new ItemCreator(Material.TNT).setDisplayName("§e" + (goldCount >= 4 ? "§a" : "§c") + "TNT").setLore("§7Preço: §64 ouros§7.", "§1", "§eClique pressionando shift para", "§eadicionar na compra rápida.", " ", "§7Clique para comprar.").build());
        inventory.setItem(24, new ItemCreator(Material.ENDER_PEARL).setDisplayName("§e" + (emeraldCount >= 4 ? "§a" : "§c") + "Pérola do fim").setLore("§7Preço: §24 esmeraldas§7.", "§1", "§eClique pressionando shift para", "§eadicionar na compra rápida.", " ", "§7Clique para comprar.").build());
        inventory.setItem(25, new ItemCreator(Material.WATER_BUCKET).setDisplayName("§e" + (goldCount >= 3 ? "§a" : "§c") + "Balde de água").setLore("§7Preço: §63 ouros§7.", "§1", "§eClique pressionando shift para", "§eadicionar na compra rápida.", " ", "§7Clique para comprar.").build());
        inventory.setItem(28, new ItemCreator(Material.MILK_BUCKET).setDisplayName("§e" + (goldCount >= 2 ? "§a" : "§c") + "Leite").setLore("§7Preço: §62 ouros§7.", "§1", "§eClique pressionando shift para", "§eadicionar na compra rápida.", " ", "§7Clique para comprar.").build());
        inventory.setItem(29, new ItemCreator(Material.SPONGE).setAmount(4).setDisplayName("§e" + (goldCount >= 1 ? "§a" : "§c") + "Esponja").setLore("§7Preço: §61 ouro§7.", "§1", "§eClique pressionando shift para", "§eadicionar na compra rápida.", " ", "§7Clique para comprar.").build());
        inventory.setItem(30, new ItemCreator(Material.CHEST).setDisplayName("§e" + (ironCount >= 16 ? "§a" : "§c") + "Torre").setLore("§7Preço: §f16 ferros§7.", "§1", "§eClique pressionando shift para", "§eadicionar na compra rápida.", " ", "§7Clique para comprar.").build());
        inventory.setItem(31, new ItemCreator(Material.EGG).setDisplayName("§e" + (emeraldCount >= 1 ? "§a" : "§c") + "Ovo das pontes").setLore("§7Preço: §21 esmeralda§7.", "§1", "§eClique pressionando shift para", "§eadicionar na compra rápida.", " ", "§7Clique para comprar.").build());
        inventory.setItem(32, new ItemCreator(Material.COMPASS).setDisplayName("§e" + (emeraldCount >= 2 ? "§a" : "§c") + "Rastreador").setLore("§7Preço: §22 esmeraldas§7.", "§1", "§eClique pressionando shift para", "§eadicionar na compra rápida.", "§cDisponível apenas quando não há camas", "§cinimigas restantes.", " ", "§7Clique para comprar.").build());

        player.openInventory(inventory);
    }

    private Material pickLevel(int level) {
        switch (level) {
            case 1:
                return Material.WOOD_PICKAXE;
            case 2:
                return Material.STONE_PICKAXE;
            case 3:
                return Material.IRON_PICKAXE;
            case 4:
                return Material.GOLD_PICKAXE;
            case 5:
                return Material.DIAMOND_PICKAXE;
        }
        return null;
    }

    private String pickPrice(int level) {
        switch (level) {
            case 1:
            case 2:
            case 3:
                return "§f10 ferros§7.";
            case 4:
                return "§63 ouros§7.";
            case 5:
                return "§66 ouros§7.";
        }
        return "";
    }

    private Material axeLevel(int level) {
        switch (level) {
            case 1:
                return Material.WOOD_AXE;
            case 2:
                return Material.STONE_AXE;
            case 3:
                return Material.IRON_AXE;
            case 4:
                return Material.GOLD_AXE;
            case 5:
                return Material.DIAMOND_AXE;
        }
        return null;
    }

    private String axePrice(int level) {
        switch (level) {
            case 1:
            case 2:
            case 3:
                return "§f10 ferros§7.";
            case 4:
                return "§63 ouros§7.";
            case 5:
                return "§66 ouros§7.";
        }
        return "";
    }

    private String convertNumberRomain(int number) {
        switch (number) {
            case 1:
                return "I";
            case 2:
                return "II";
            case 3:
                return "III";
            case 4:
                return "IV";
            case 5:
                return "V";
        }
        return "";
    }

    @EventHandler
    public void onInventory(InventoryClickEvent event) {
        if (event.getWhoClicked() instanceof Player) {
            ItemStack itemStack = event.getCurrentItem();
            Player player = (Player) event.getWhoClicked();
            Inventory inventory = event.getInventory();
            User u = arena.getUserService().get(player.getUniqueId());
            UserUpgrades upgrades = u.getUserUpgrades();

            if (inventory == null) return;

            if (inventory.getTitle().equals("Compra rápida") || inventory.getTitle().startsWith("Loja - ")) {
                event.setCancelled(true);

                if (itemStack == null) return;
                if (itemStack.getType() == Material.AIR) return;
                if (itemStack.getItemMeta() == null) return;
                if (itemStack.getItemMeta().getDisplayName() == null) return;

                if (itemStack.getItemMeta().getDisplayName().startsWith("§a")) {
                    switch (itemStack.getType()) {
                        case NETHER_STAR:
                            quickbuy(player);
                            break;
                        case WOOD:
                            blocks(player);
                            break;
                        case STONE_SWORD:
                            combat(player);
                            break;
                        case DIAMOND_BOOTS:
                            armor(player);
                            break;
                        case IRON_PICKAXE:
                            tools(player);
                            break;
                        case BOW:
                            projectiles(player);
                            break;
                        case BREWING_STAND_ITEM:
                            potions(player);
                            break;
                        case TNT:
                            utility(player);
                            break;
                    }
                    return;
                }
                if (itemStack.getItemMeta().getDisplayName().startsWith("§e") || itemStack.getItemMeta().getDisplayName().startsWith("§c")) {
                    if (event.isShiftClick()) {
                        if (!inventory.getTitle().equals("Compra rápida")) {
                            if (u.getQuickbuy().stream().noneMatch(is -> is.getType() == itemStack.getType() && is.getDurability() == itemStack.getDurability() && is.getEnchantments() == itemStack.getEnchantments())) {
                                if (u.getQuickbuy().size() == 21) {
                                    player.sendMessage("§cVocê deve remover um item da compra rápida para adicionar outro.");
                                    return;
                                }

                                if (u.getQuickbuy().stream().filter(i -> i.getType() == itemStack.getType()).filter(i -> i.getDurability() == itemStack.getDurability()).findFirst().orElse(null) != null) {
                                    player.sendMessage("§cVocê já possui este item na sua compra rápida.");
                                    return;
                                }

                                ItemStack clone = itemStack.clone();
                                ItemMeta itemMeta = clone.getItemMeta();
                                List<String> lore = itemMeta.getLore();
                                String text = "";

                                for (String s : lore) {
                                    if (s.startsWith("§7Preço:")) {
                                        text = s;
                                        break;
                                    }
                                }
                                lore.clear();
                                lore.add(text);
                                lore.add(" ");
                                lore.add("§eClique pressionando shift para");
                                lore.add("§eremover da sua compra rápida.");
                                lore.add(" ");
                                lore.add("§7Clique para comprar.");

                                itemMeta.setLore(lore);
                                clone.setItemMeta(itemMeta);

                                u.getQuickbuy().add(clone);
                                player.sendMessage("§eVocê adicionou este item na sua compra rápida.");
                            }
                            return;
                        } else {
                            for (ItemStack is : u.getQuickbuy()) {
                                net.minecraft.server.v1_8_R3.ItemStack i = CraftItemStack.asNMSCopy(itemStack);

                                if (itemStack.getType() != is.getType())continue;

                                u.getQuickbuy().remove(is);
                                player.sendMessage("§cVocê removeu este item da sua compra rápida.");
                                quickbuy(player);
                                return;
                            }
                        }
                    }
                    if (itemStack.getItemMeta().getDisplayName().startsWith("§c")) {
                        return;
                    }
                    String s = itemStack.getItemMeta().getLore().get(0);
                    int price = Integer.parseInt(s.substring(11, s.lastIndexOf(" ")));
                    int count = 0;

                    for (int i = 0; i < 32; i++) {
                        ItemStack item = player.getInventory().getItem(i);

                        if (item != null && item.getType() != Material.AIR) {
                            if (item.getType() == getItemLore(s)) {
                                count = (count + item.getAmount());
                            }
                        }
                    }
                    if (itemStack.getItemMeta().hasEnchant(Enchantment.ARROW_KNOCKBACK)) {
                        if (Arrays.stream(player.getInventory().getContents()).anyMatch(is -> is != null && is.getType() == Material.EMERALD)) {
                            Arrays.stream(player.getInventory().getContents()).filter(is -> is != null && is.getType() == Material.EMERALD).findAny().ifPresent(is -> {
                                ItemStack clone = itemStack.clone();
                                ItemMeta itemMeta = clone.getItemMeta();
                                itemMeta.setDisplayName(null);
                                itemMeta.setLore(null);

                                if (!clone.getType().isBlock()) {
                                    itemMeta.spigot().setUnbreakable(true);
                                }
                                clone.setItemMeta(itemMeta);

                                if (is.getAmount() > 6) {
                                    is.setAmount((is.getAmount() - 6));
                                    player.getInventory().addItem(clone);
                                } else if (is.getAmount() == 6) {
                                    player.getInventory().remove(is);
                                    player.getInventory().addItem(clone);
                                } else {
                                    player.sendMessage("§cVocê não possui recursos suficientes.");
                                }
                            });
                        } else {
                            player.sendMessage("§cVocê não possui recursos suficientes.");
                        }
                        return;
                    }

                    if (count >= price) {
                        User user = arena.getUserService().get(player.getUniqueId());

                        if (itemStack.getType() == Material.SHEARS) {
                            if (user.getUserUpgrades().isShears()) {
                                player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1F, 1F);
                                return;
                            }
                            user.getUserUpgrades().setShears(true);
                        }
                        if (itemStack.getType().name().contains("PICKAXE")) {
                            if (user.getUserUpgrades().getPickLevel() == 5) {
                                player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1F, 1F);
                                return;

                            }
                        }
                        if (itemStack.getType().name().contains("_AXE")) {
                            if (user.getUserUpgrades().getAxeLevel() == 5) {
                                player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1F, 1F);
                                return;
                            }
                        }
                        ItemStack clone = itemStack.clone();
                        ItemMeta itemMeta = clone.getItemMeta();
                        itemMeta.setDisplayName(null);
                        itemMeta.setLore(null);

                        if (!clone.getType().isBlock()) {
                            itemMeta.spigot().setUnbreakable(true);
                        }
                        if (clone.getType() == Material.WOOL || clone.getType() == Material.GLASS || clone.getType() == Material.STAINED_CLAY) {
                            if (clone.getType() == Material.GLASS) {
                                clone.setType(Material.STAINED_GLASS);
                            }
                            clone.setDurability((short) arena.getUserService().get(player.getUniqueId()).getTeam().getTeamColor().getWoolId());
                        }
                        clone.setItemMeta(itemMeta);

                        if (clone.getType().name().contains("BOOTS")) {
                            if (user.getUserUpgrades().getArmorLevel() >= (clone.getType() == Material.CHAINMAIL_BOOTS ? 1 : (clone.getType() == Material.IRON_BOOTS ? 2 : 3))) {
                                player.sendMessage("§cVocê já possui essa armadura ou superior.");
                                return;
                            }
                        }
                        if (clone.getType() == Material.COMPASS) {
                            if (arena.getTeams().stream().anyMatch(team -> !team.isBedBroken())) {
                                player.sendMessage("§cVocê não pode comprar o rastreador, pois ainda existem times com camas.");
                                return;
                            }
                            if (Arrays.stream(player.getInventory().getContents()).anyMatch(is -> is != null && is.getType() == Material.COMPASS)) {
                                player.sendMessage("§cVocê já possui um rastreador.");
                                return;
                            }
                        }
                        player.getInventory().remove(getItemLore(s));

                        for (int i = 0; i < (count - price); i++) {
                            player.getInventory().addItem(new ItemStack(getItemLore(s)));
                        }
                        player.playSound(player.getLocation(), Sound.LEVEL_UP, 1F, 1F);
                        player.sendMessage("§aVocê comprou §f" + itemStack.getItemMeta().getDisplayName() + "§a.");

                        if (clone.getType().name().contains("SWORD")) {
                            if (arena.getUserService().get(player.getUniqueId()).getTeam().getTeamUpgrades().isSharpness()) {
                                clone.addEnchantment(Enchantment.DAMAGE_ALL, 1);
                            }
                            if (player.getInventory().getItem(0).getType() == Material.WOOD_SWORD) {
                                player.getInventory().setItem(0, clone);
                                return;
                            }
                        }
                        if (clone.getType().name().contains("BOOTS")) {
                            user.getUserUpgrades().setArmorLevel((clone.getType() == Material.CHAINMAIL_BOOTS ? 1 : (clone.getType() == Material.IRON_BOOTS ? 2 : 3)));
                            user.giveArmor();

                            armor(player);
                            return;
                        }
                        if (clone.getType().name().contains("PICKAXE")) {
                            Arrays.stream(player.getInventory().getContents()).filter(is -> is != null && is.getType().name().contains("PICKAXE")).forEach(is -> player.getInventory().remove(is));

                            user.getUserUpgrades().setPickLevel((clone.getType() == Material.WOOD_PICKAXE ? 1 : (clone.getType() == Material.STONE_PICKAXE ? 2 : (clone.getType() == Material.IRON_PICKAXE ? 3 : (clone.getType() == Material.GOLD_PICKAXE ? 4 : 5)))));
                            user.givePickaxe();

                            tools(player);
                            return;
                        }
                        if (clone.getType().name().contains("_AXE")) {
                            Arrays.stream(player.getInventory().getContents()).filter(is -> is != null && is.getType().name().contains("_AXE")).forEach(is -> player.getInventory().remove(is));

                            user.getUserUpgrades().setAxeLevel((clone.getType() == Material.WOOD_AXE ? 1 : (clone.getType() == Material.STONE_AXE ? 2 : (clone.getType() == Material.IRON_AXE ? 3 : (clone.getType() == Material.GOLD_AXE ? 4 : 5)))));
                            user.giveAxe();

                            tools(player);
                            return;
                        }
                        player.getInventory().addItem(clone);
                        player.updateInventory();
                    } else {
                        player.sendMessage("§cVocê não possui recursos suficientes.");
                    }
                }
            }
        }

    }

    private Material getItemLore(String lore) {
        if (lore.contains("ferro")) {
            return Material.IRON_INGOT;
        } else if (lore.contains("ouro")) {
            return Material.GOLD_INGOT;
        } else if (lore.contains("esmeralda")) {
            return Material.EMERALD;
        } else {
            return null;
        }
    }
}
