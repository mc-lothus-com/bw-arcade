package com.lothus.engines.bedwars.team;

import com.google.common.collect.Lists;
import com.lothus.core.games.room.RoomType;
import com.lothus.core.utils.bukkit.ItemCreator;
import com.lothus.engines.bedwars.Bedwars;
import com.lothus.engines.bedwars.location.Locations;
import com.lothus.engines.bedwars.team.color.TeamColor;
import com.lothus.engines.bedwars.upgrades.team.TeamUpgrades;
import com.lothus.engines.bedwars.user.User;
import lombok.Data;
import net.jitse.npclib.api.NPC;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.github.paperspigot.Title;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Data
public class Team {

    private final Locations bedLocation, spawn, ironGen, goldGen, shopVillager, upgradeVillager;
    private final TeamColor teamColor;
    private final TeamUpgrades teamUpgrades;
    private ArrayList<User> players;
    private NPC shop, upgrade;

    public Team(Locations bedLocation, Locations spawn, Locations ironGen, Locations goldGen, Locations shopVillager, Locations upgradeVillager, TeamColor teamColor, TeamUpgrades teamUpgrades) {
        this.bedLocation = bedLocation;
        this.spawn = spawn;
        this.ironGen = ironGen;
        this.goldGen = goldGen;
        this.shopVillager = shopVillager;
        this.upgradeVillager = upgradeVillager;
        this.teamColor = teamColor;
        this.teamUpgrades = teamUpgrades;
        this.players = new ArrayList<>();
    }

    public boolean isMember(String name){
        return getPlayers().stream().anyMatch(user -> user.getName().equalsIgnoreCase(name));
    }

    public boolean isMember(UUID name){
        return getPlayers().stream().anyMatch(user -> user.getUuid().equals(name));
    }

    public boolean isBedBroken() {
        return (getNearbyBlocks(bedLocation.getLocation(), 2).stream().noneMatch(block -> block.getType().name().contains("BED") && !block.getType().name().endsWith("ROCK")));
    }

    public boolean isAlive() {
        return (!getPlayers().isEmpty() && getPlayers().stream().anyMatch(user -> user != null && !user.isSpectator()) && getPlayers().stream().anyMatch(user -> user != null && user.getPlayer() != null));
    }

    public void breakBed(User user) {
        if (user != null) {
            user.addBrokenBed();
            user.addCoins(5);
            user.addPoints(10);
            user.addXp(10);

            Bukkit.getOnlinePlayers().forEach(player -> player.playSound(player.getLocation(), Sound.ENDERDRAGON_GROWL, 1F, 1F));
            Bukkit.broadcastMessage("");
            Bukkit.broadcastMessage("§c§lCAMA QUEBRADA §f» §7A cama do time " + teamColor.getColoredName().substring(0, 2) + teamColor.getNormalName() + " §7foi destruída pelo " + user.getTeam().getTeamColor().getColoredName().substring(0, 2) + user.getName() + "§7.");
            Bukkit.broadcastMessage("");

            eliminateTeam();
        }
        getPlayers().stream().filter(u -> u != null && u.getPlayer() != null).forEach(u -> u.getPlayer().sendTitle(new Title("§c§lCAMA QUEBRADA", "§7Você não pode mais renascer.", 0, 40, 0)));
        getNearbyBlocks(bedLocation.getLocation(), 4).stream().filter(block -> block.getType() == Material.BED_BLOCK || block.getType() == Material.BED).forEach(block -> block.setType(Material.AIR));

        bedLocation.getLocation().getWorld().getNearbyEntities(bedLocation.getLocation(), 4, 4, 4).stream().filter(entity -> entity instanceof Item).map(entity -> (Item) entity).forEach(item -> {
            if (item.getItemStack().getType() == Material.BED) {
                item.remove();
            }
        });
    }

    public void eliminateTeam() {
        if (isBedBroken() && getPlayers().stream().noneMatch(u -> u != null && u.isPlaying())) {
            Bukkit.broadcastMessage("");
            Bukkit.broadcastMessage("§c§lTIME ELIMINADO §f» §7O time " + teamColor.getColoredName().substring(0, 2) + teamColor.getNormalName() + " §7foi eliminado.");
            Bukkit.broadcastMessage("");
        }
    }

    public void updateSword() {
        if (teamUpgrades.isSharpness()) {
            getPlayers().forEach(user -> Arrays.stream(user.getPlayer().getInventory().getContents()).filter(itemStack -> itemStack != null && itemStack.getType().name().contains("SWORD")).forEach(itemStack -> {
                ItemStack clone = itemStack.clone();

                user.getPlayer().getInventory().remove(clone.getType());
                user.getPlayer().getInventory().addItem(new ItemCreator(clone.getType()).addEnchant(Enchantment.DAMAGE_ALL, 1).build());
                user.getPlayer().updateInventory();
            }));
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
