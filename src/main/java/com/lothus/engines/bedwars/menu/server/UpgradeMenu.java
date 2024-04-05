package com.lothus.engines.bedwars.menu.server;

import com.google.common.collect.Lists;
import com.lothus.core.utils.bukkit.ItemCreator;
import com.lothus.engines.bedwars.Bedwars;
import com.lothus.engines.bedwars.arena.Arena;
import com.lothus.engines.bedwars.upgrades.team.TeamUpgrades;
import com.lothus.engines.bedwars.user.User;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.Arrays;
import java.util.List;

public class UpgradeMenu implements Listener {

    private final Arena arena;
    private final int trap;

    public UpgradeMenu(Arena arena) {
        this.arena = arena;
        this.trap = Bedwars.getInstance().getConfig().getInt("protection.trap");

        Bukkit.getPluginManager().registerEvents(this, Bedwars.getInstance());
    }

    public void open(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 27, "Melhorias do time");
        TeamUpgrades upgrades = arena.getUserService().get(player.getUniqueId()).getTeam().getTeamUpgrades();

        inventory.setItem(10, new ItemCreator(Material.IRON_SWORD).setDisplayName((Arrays.stream(player.getInventory().getContents()).anyMatch(itemStack -> itemStack != null && itemStack.getType() == Material.DIAMOND && itemStack.getAmount() >= 4) ? "§a" : (upgrades.isSharpness() ? "§a" : "§c")) + "Afiação na espada").setLore("§7O seu time ganha Afiação I", "§7em todas as espadas.", "", "§fPreço: §b4 diamantes", "", (!upgrades.isSharpness() ? "§7Clique para comprar." : "§cVocê já possui.")).build());
        inventory.setItem(11, new ItemCreator(Material.IRON_CHESTPLATE).setDisplayName((Arrays.stream(player.getInventory().getContents()).anyMatch(itemStack -> itemStack != null && itemStack.getType() == Material.DIAMOND && itemStack.getAmount() >= priceArmor(upgrades)) ? "§a" : (upgrades.getProtection() == 4 ? "§a" : "§c")) + "Proteção na armadura " + convertNumberRomain((upgrades.getProtection() < 4 ? upgrades.getProtection() + 1 : 4))).setLore("§7O seu time ganha Proteção em", "§7todas as peças da armadura.", "",
                (upgrades.getProtection() >= 1 ? "§a✔" : "§c✖§7") + " Proteção I: §b2 diamantes",
                (upgrades.getProtection() >= 2 ? "§a✔" : "§c✖§7") + " Proteção II: §b4 diamantes",
                (upgrades.getProtection() >= 3 ? "§a✔" : "§c✖§7") + " Proteção III: §b8 diamantes",
                (upgrades.getProtection() >= 4 ? "§a✔" : "§c✖§7") + " Proteção IV: §b16 diamantes").build());
        inventory.setItem(12, new ItemCreator(Material.GOLD_PICKAXE).setDisplayName((Arrays.stream(player.getInventory().getContents()).anyMatch(itemStack -> itemStack != null && itemStack.getType() == Material.DIAMOND && itemStack.getAmount() >= priceHaste(upgrades)) ? "§a" : (upgrades.getHaste() == 2 ? "§a" : "§c")) + "Mineração " + convertNumberRomain((upgrades.getHaste() < 2 ? upgrades.getHaste() + 1 : 2))).setLore("§7O seu time ganha agilidade", "§7para minerar.", "",
                (upgrades.getHaste() >= 1 ? "§a✔" : "§c✖§7") + " Mineração I: §b2 diamantes",
                (upgrades.getHaste() >= 2 ? "§a✔" : "§c✖§7") + " Mineração II: §b4 diamantes").build());
        inventory.setItem(13, new ItemCreator(Material.FURNACE).setDisplayName((Arrays.stream(player.getInventory().getContents()).anyMatch(itemStack -> itemStack != null && itemStack.getType() == Material.DIAMOND && itemStack.getAmount() >= priceForge(upgrades)) ? "§a" : (upgrades.getForge() == 4 ? "§a" : "§c")) + "Forja " + convertNumberRomain((upgrades.getForge() < 4 ? upgrades.getForge() + 1 : 4))).setLore("§7Aumente o número de minérios", "§7que nascem em sua ilha.", "",
                (upgrades.getForge() >= 1 ? "§a✔" : "§c✖§7") + " +50% de minérios: §b2 diamantes",
                (upgrades.getForge() >= 2 ? "§a✔" : "§c✖§7") + " +100% de minérios: §b4 diamantes",
                (upgrades.getForge() >= 3 ? "§a✔" : "§c✖§7") + " Gera esmeraldas: §b6 diamantes",
                (upgrades.getForge() == 4 ? "§a✔" : "§c✖§7") + " +200% de minérios: §b8 diamantes").build());
        inventory.setItem(14, new ItemCreator(Material.BEACON).setDisplayName((Arrays.stream(player.getInventory().getContents()).anyMatch(itemStack -> itemStack != null && itemStack.getType() == Material.DIAMOND && itemStack.getAmount() >= 1) ? "§a" : (upgrades.isRegen() ? "§a" : "§c")) + "Regeneração na ilha").setLore("§7Crie um campo regenerativo", "§7em volta da ilha.", "", "§fPreço: §b1 diamante", "", (!upgrades.isRegen() ? "§7Clique para comprar." : "§cVocê já possui.")).build());
        inventory.setItem(15, new ItemCreator(Material.TRIPWIRE_HOOK).setDisplayName((Arrays.stream(player.getInventory().getContents()).anyMatch(itemStack -> itemStack != null && itemStack.getType() == Material.DIAMOND && itemStack.getAmount() >= 1) ? "§a" : (upgrades.isTrap() ? "§a" : "§c")) + "Armadilha na ilha").setLore("§7Cause cegueira e lentidão durante", "§78 segundos nos invasores.", "", "§fPreço: §b1 diamante", "", (!upgrades.isTrap() ? "§7Clique para comprar." : "§cVocê já possui.")).build());

        player.openInventory(inventory);
    }

    private int priceArmor(TeamUpgrades upgrades) {
        switch ((upgrades.getProtection() + 1)) {
            case 1:
                return 2;
            case 2:
                return 4;
            case 3:
                return 8;
            case 4:
                return 16;
        }
        return 0;
    }

    private int priceHaste(TeamUpgrades upgrades) {
        switch ((upgrades.getHaste() + 1)) {
            case 1:
                return 2;
            case 2:
                return 4;
        }
        return 0;
    }

    private int priceForge(TeamUpgrades upgrades) {
        switch ((upgrades.getForge() + 1)) {
            case 1:
                return 2;
            case 2:
                return 4;
            case 3:
                return 6;
            case 4:
                return 8;
        }
        return 0;
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
            Inventory inventory = event.getInventory();
            ItemStack itemStack = event.getCurrentItem();
            Player player = (Player) event.getWhoClicked();

            if (inventory == null) return;
            if (itemStack == null) return;
            if (itemStack.getType() == Material.AIR) return;

            if (inventory.getTitle().equals("Melhorias do time")) {
                event.setCancelled(true);

                User user = arena.getUserService().get(player.getUniqueId());
                TeamUpgrades upgrades = user.getTeam().getTeamUpgrades();
                int count = 0;

                for (int i = 0; i < 32; i++) {
                    ItemStack item = player.getInventory().getItem(i);

                    if (item != null && item.getType() != Material.AIR) {
                        if (item.getType() == Material.DIAMOND) {
                            count = (count + item.getAmount());
                        }
                    }
                }
                switch (itemStack.getType()) {
                    case IRON_SWORD:
                        if (count >= 4) {
                            if (upgrades.isSharpness()) {
                                player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1F, 1F);
                                player.sendMessage("§cA melhoria já está no nível máximo.");
                                return;
                            }
                            for (int i = 0; i < 32; i++) {
                                ItemStack item = player.getInventory().getItem(i);

                                if (item != null && item.isSimilar(new ItemStack(Material.DIAMOND))) {
                                    int amountItem = item.getAmount();

                                    if (amountItem <= 4) {
                                        count -= amountItem;
                                        player.getInventory().setItem(i, null);
                                    } else {
                                        item.setAmount(amountItem - 4);
                                        player.getInventory().setItem(i, item);
                                        break;
                                    }
                                }
                            }
                            upgrades.setSharpness(true);
                            user.getTeam().updateSword();
                            user.getTeam().getPlayers().forEach(u -> {
                                u.getPlayer().playSound(u.getPlayer().getLocation(), Sound.LEVEL_UP, 1F, 1F);
                                u.getPlayer().sendMessage(user.getTeam().getTeamColor().getColoredName().substring(0, 2) + user.getName() + " §eadquiriu a melhoria §aAfiação I§e.");
                            });
                            open(player);
                        } else {
                            player.sendMessage("§cVocê não possui recursos suficientes.");
                        }
                        break;
                    case IRON_CHESTPLATE:
                        if (count >= priceArmor(user.getTeam().getTeamUpgrades())) {
                            if (upgrades.getProtection() == 4) {
                                player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1F, 1F);
                                player.sendMessage("§cA melhoria já está no nível máximo.");
                                return;
                            }
                            for (int i = 0; i < 32; i++) {
                                ItemStack item = player.getInventory().getItem(i);

                                if (item != null && item.isSimilar(new ItemStack(Material.DIAMOND))) {
                                    int amountItem = item.getAmount();

                                    if (amountItem <= priceArmor(user.getTeam().getTeamUpgrades())) {
                                        count -= amountItem;
                                        player.getInventory().setItem(i, null);
                                    } else {
                                        item.setAmount(amountItem - priceArmor(user.getTeam().getTeamUpgrades()));
                                        player.getInventory().setItem(i, item);
                                        break;
                                    }
                                }
                            }
                            upgrades.setProtection((upgrades.getProtection() + 1));
                            user.giveArmor();
                            user.getTeam().getPlayers().forEach(u -> {
                                u.getPlayer().playSound(u.getPlayer().getLocation(), Sound.LEVEL_UP, 1F, 1F);
                                u.getPlayer().sendMessage(user.getTeam().getTeamColor().getColoredName().substring(0, 2) + user.getName() + " §eadquiriu a melhoria §aProteção " + convertNumberRomain(upgrades.getProtection()) + "§e.");
                            });
                            open(player);
                        } else {
                            player.sendMessage("§cVocê não possui recursos suficientes.");
                        }
                        break;
                    case GOLD_PICKAXE:
                        if (count >= priceHaste(user.getTeam().getTeamUpgrades())) {
                            if (upgrades.getHaste() == 2) {
                                player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1F, 1F);
                                player.sendMessage("§cA melhoria já está no nível máximo.");
                                return;
                            }
                            for (int i = 0; i < 32; i++) {
                                ItemStack item = player.getInventory().getItem(i);

                                if (item != null && item.isSimilar(new ItemStack(Material.DIAMOND))) {
                                    int amountItem = item.getAmount();

                                    if (amountItem <= priceHaste(user.getTeam().getTeamUpgrades())) {
                                        count -= amountItem;
                                        player.getInventory().setItem(i, null);
                                    } else {
                                        item.setAmount(amountItem - priceHaste(user.getTeam().getTeamUpgrades()));
                                        player.getInventory().setItem(i, item);
                                        break;
                                    }
                                }
                            }
                            upgrades.setHaste((upgrades.getHaste() + 1));
                            user.getTeam().getPlayers().forEach(u -> {
                                if (u.getPlayer().getActivePotionEffects().stream().anyMatch(potionEffect -> potionEffect.getType() == PotionEffectType.FAST_DIGGING)) {
                                    u.getPlayer().removePotionEffect(PotionEffectType.FAST_DIGGING);
                                }
                                u.getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, Integer.MAX_VALUE, (upgrades.getHaste() - 1)));
                                u.getPlayer().playSound(u.getPlayer().getLocation(), Sound.LEVEL_UP, 1F, 1F);
                                u.getPlayer().sendMessage(user.getTeam().getTeamColor().getColoredName().substring(0, 2) + user.getName() + " §eadquiriu a melhoria §aMineração " + convertNumberRomain(upgrades.getHaste()) + "§e.");
                            });
                            open(player);
                        } else {
                            player.sendMessage("§cVocê não possui recursos suficientes.");
                        }
                        break;
                    case FURNACE:
                        if (count >= priceForge(user.getTeam().getTeamUpgrades())) {
                            if (upgrades.getForge() == 4) {
                                player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1F, 1F);
                                player.sendMessage("§cA melhoria já está no nível máximo.");
                                return;
                            }
                            for (int i = 0; i < 32; i++) {
                                ItemStack item = player.getInventory().getItem(i);

                                if (item != null && item.isSimilar(new ItemStack(Material.DIAMOND))) {
                                    int amountItem = item.getAmount();

                                    if (amountItem <= priceForge(user.getTeam().getTeamUpgrades())) {
                                        count -= amountItem;
                                        player.getInventory().setItem(i, null);
                                    } else {
                                        item.setAmount(amountItem - priceForge(user.getTeam().getTeamUpgrades()));
                                        player.getInventory().setItem(i, item);
                                        break;
                                    }
                                }
                            }
                            upgrades.setForge((upgrades.getForge() + 1));
                            user.getTeam().getPlayers().forEach(u -> {
                                u.getPlayer().playSound(u.getPlayer().getLocation(), Sound.LEVEL_UP, 1F, 1F);
                                u.getPlayer().sendMessage(user.getTeam().getTeamColor().getColoredName().substring(0, 2) + user.getName() + " §eadquiriu a melhoria §aForja " + convertNumberRomain(upgrades.getForge()) + "§e.");
                            });
                            Bedwars.getInstance().getGameManager().getTeamGenerators().upgradeGenerator(user.getTeam());
                            open(player);
                        } else {
                            player.sendMessage("§cVocê não possui recursos suficientes.");
                        }
                        break;
                    case BEACON:
                        if (Arrays.stream(player.getInventory().getContents()).anyMatch(is -> is != null && is.getType() == Material.DIAMOND)) {
                            if (upgrades.isRegen()) {
                                player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1F, 1F);
                                player.sendMessage("§cA melhoria já está no nível máximo.");
                                return;
                            }
                            Arrays.stream(player.getInventory().getContents()).filter(is -> is != null && is.getType() == Material.DIAMOND).findAny().ifPresent(is -> {
                                if (is.getAmount() > 1) {
                                    is.setAmount((is.getAmount() - 1));
                                } else {
                                    player.getInventory().remove(is);
                                    player.updateInventory();
                                }
                                upgrades.setRegen(true);
                                user.getTeam().getPlayers().forEach(u -> {
                                    u.getPlayer().playSound(u.getPlayer().getLocation(), Sound.LEVEL_UP, 1F, 1F);
                                    u.getPlayer().sendMessage(user.getTeam().getTeamColor().getColoredName().substring(0, 2) + user.getName() + " §eadquiriu a melhoria §aRegeneração na ilha§e.");
                                });
                                Bukkit.getScheduler().scheduleSyncRepeatingTask(Bedwars.getInstance(), () -> {
                                    int j = 65;
                                    List<Block> blocks = getNearbyBlocks(user.getTeam().getSpawn().getLocation(), trap);

                                    for (Block value : blocks) {
                                        if (j % 65 == 0) {
                                            value.getLocation().getWorld().playEffect(value.getLocation(), Effect.HAPPY_VILLAGER, 1);
                                        }
                                        j++;
                                    }
                                }, 0L, 20L);
                                open(player);
                            });
                        } else {
                            player.sendMessage("§cVocê não possui recursos suficientes.");
                        }
                        break;
                    case TRIPWIRE_HOOK:
                        if (Arrays.stream(player.getInventory().getContents()).anyMatch(is -> is != null && is.getType() == Material.DIAMOND)) {
                            if (upgrades.isTrap()) {
                                player.playSound(player.getLocation(), Sound.VILLAGER_NO, 1F, 1F);
                                player.sendMessage("§cA melhoria já está no nível máximo.");
                                return;
                            }
                            Arrays.stream(player.getInventory().getContents()).filter(is -> is != null && is.getType() == Material.DIAMOND).findAny().ifPresent(is -> {
                                if (is.getAmount() > 1) {
                                    is.setAmount((is.getAmount() - 1));
                                } else {
                                    player.getInventory().remove(is);
                                    player.updateInventory();
                                }
                                upgrades.setTrap(true);
                                user.getTeam().getPlayers().forEach(u -> {
                                    u.getPlayer().playSound(u.getPlayer().getLocation(), Sound.LEVEL_UP, 1F, 1F);
                                    u.getPlayer().sendMessage(user.getTeam().getTeamColor().getColoredName().substring(0, 2) + user.getName() + " §eadquiriu a melhoria §aArmadilha na ilha§e.");
                                });
                                open(player);
                            });
                        } else {
                            player.sendMessage("§cVocê não possui recursos suficientes.");
                        }
                        break;
                }
            }
        }
    }

    private List<Block> getNearbyBlocks(Location location, int radius) {
        List<Block> blocks = Lists.newArrayList();

        for (int x = location.getBlockX() - radius; x <= location.getBlockX() + radius; x++) {
            for (int y = location.getBlockY() - radius; y <= location.getBlockY() + radius; y++) {
                for (int z = location.getBlockZ() - radius; z <= location.getBlockZ() + radius; z++) {
                    blocks.add(location.getWorld().getBlockAt(x, y, z));
                }
            }
        }
        return blocks;
    }

}
