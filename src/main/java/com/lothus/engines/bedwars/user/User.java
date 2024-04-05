package com.lothus.engines.bedwars.user;

import com.lothus.core.Core;
import com.lothus.core.games.room.RoomType;
import com.lothus.core.games.type.GameType;
import com.lothus.core.player.LothPlayer;
import com.lothus.core.player.booster.GameBooster;
import com.lothus.core.player.booster.status.BoosterStatus;
import com.lothus.core.player.booster.type.BoosterType;
import com.lothus.core.utils.bukkit.ItemCreator;
import com.lothus.engines.bedwars.Bedwars;
import com.lothus.engines.bedwars.scoreboard.BedScore;
import com.lothus.engines.bedwars.team.Team;
import com.lothus.engines.bedwars.upgrades.user.UserUpgrades;
import com.lothus.sync.stats.data.type.DataType;
import com.lothus.sync.stats.platform.Platform;
import com.lothus.sync.stats.player.games.bedwars.BedPlayer;
import com.lothus.sync.stats.player.games.bedwars.stats.BedStats;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;

@Getter
public class User {

    private final String name;
    private final UUID uuid;
    private final UserUpgrades userUpgrades;
    private BedScore bedScore;
    private int kills, finalKills, brokenBeds, coins, xp, points,deaths;

    @Setter
    private boolean spectator, dead, winner;

    @Setter
    private Entity lastDamage;

    @Setter
    private long lastDamageTick;

    @Setter
    private Team team;

    @Setter
    private Team tracking;

    private final List<ItemStack> quickbuy;

    public User(String name, UUID uniqueId) {
        this.name = name;
        this.uuid = uniqueId;
        this.userUpgrades = new UserUpgrades();
        this.quickbuy = new ArrayList<>();
    }

    public void setup() {
        this.bedScore = new BedScore(getPlayer());

        RoomType roomType = Bedwars.getInstance().getGameManager().getArena().getGameInfo().getRoomType();
        DataType dataType = roomType == RoomType.SOLO ? DataType.BED_WARS_SOLO : (roomType == RoomType.DUPLAS ? DataType.BED_WARS_TEAM :
                (roomType == RoomType.TRIOS ? DataType.BED_WARS_TRIO : roomType == RoomType.RANQUEADO ? DataType.BED_WARS_RANKED : DataType.BED_WARS_QUARTETO));

        BedPlayer bedPlayer = Platform.getDataPlayer().getBed(DataType.BED_WARS_ACCOUNT, uuid);

        quickbuy.clear();

        if (bedPlayer.getQuickBuy() != null) {
            quickbuy.addAll(bedPlayer.getQuickBuy());
        }
    }

    public void addDeath() {
        this.deaths = (deaths + 1);
    }

    public void removeDeath() {
        this.deaths = (deaths - 1);
    }
    public void addPoints(int points) {
        this.points = (this.points + points);

        LothPlayer lothPlayer = Core.getPlayerController().get(getPlayer().getUniqueId());

        boolean boosterActive = false;
        GameBooster booster = null;

        if (lothPlayer.getBoosters() != null) {
            boosterActive = lothPlayer.getBoosters().stream().filter(gameBooster -> gameBooster.getGameType() == GameType.BED_WARS).collect(Collectors.toList()).stream().anyMatch(b -> b.getStatus() == BoosterStatus.ACTIVE);
        }

        if (boosterActive) {
            booster = lothPlayer.getBoosters().stream().filter(gameBooster -> gameBooster.getGameType() == GameType.BED_WARS).collect(Collectors.toList()).stream().filter(b -> b.getStatus() == BoosterStatus.ACTIVE).findFirst().orElse(null);
        }

        String boostPrefix = boosterActive && booster.getType() == BoosterType.POINTS ? " (" + booster.getMultiplier() + "x)" : "";
        getPlayer().sendMessage("§a+" + (boosterActive && booster.getType() == BoosterType.POINTS ? points * (int)booster.getMultiplier() : points) + " pontos" + boostPrefix + "!");
    }
    public void addKill() {
        this.kills = (kills + 1);
    }
    public void addXp(int exp) {
        this.xp = (xp + exp);

        LothPlayer lothPlayer = Core.getPlayerController().get(getPlayer().getUniqueId());

        boolean boosterActive = false;
        GameBooster booster = null;

        if (lothPlayer.getBoosters() != null) {
            boosterActive = lothPlayer.getBoosters().stream().filter(gameBooster -> gameBooster.getGameType() == GameType.BED_WARS).collect(Collectors.toList()).stream().anyMatch(b -> b.getStatus() == BoosterStatus.ACTIVE);
        }

        if (boosterActive) {
            booster = lothPlayer.getBoosters().stream().filter(gameBooster -> gameBooster.getGameType() == GameType.BED_WARS).collect(Collectors.toList()).stream().filter(b -> b.getStatus() == BoosterStatus.ACTIVE).findFirst().orElse(null);
        }

        String boostPrefix = boosterActive && booster.getType() == BoosterType.XP ? " (" + booster.getMultiplier() + "x)" : "";
        getPlayer().sendMessage("§b+" + (boosterActive && booster.getType() == BoosterType.XP ? exp * (int)booster.getMultiplier() : exp) + " XP" + boostPrefix + "!");
    }

    public void addFinalKill() {
        this.finalKills = (finalKills + 1);
        addKill();
    }

    public void removePoints(int points) {
        this.points = (this.points - points);
    }

    public void addBrokenBed() {
        this.brokenBeds = (brokenBeds + 1);
    }

    public void addCoins(int coin) {
        this.coins = coins + coin;

        LothPlayer lothPlayer = Core.getPlayerController().get(getPlayer().getUniqueId());

        boolean boosterActive = false;
        GameBooster booster = null;

        if (lothPlayer.getBoosters() != null) {
            boosterActive = lothPlayer.getBoosters().stream().filter(gameBooster -> gameBooster.getGameType() == GameType.BED_WARS).collect(Collectors.toList()).stream().anyMatch(b -> b.getStatus() == BoosterStatus.ACTIVE);
        }

        if (boosterActive) {
            booster = lothPlayer.getBoosters().stream().filter(gameBooster -> gameBooster.getGameType() == GameType.BED_WARS).collect(Collectors.toList()).stream().filter(b -> b.getStatus() == BoosterStatus.ACTIVE).findFirst().orElse(null);
        }

        String boostPrefix = boosterActive && booster.getType() == BoosterType.COINS ? " (" + booster.getMultiplier() + "x)" : "";
        getPlayer().sendMessage("§6+" + (boosterActive && booster.getType() == BoosterType.COINS ? coin * (int)booster.getMultiplier() : coin) + " coins" + boostPrefix + "!");
    }

    public boolean isPlaying() {
        return !spectator && !isDead();
    }

    public Player getPlayer() {
        return Bukkit.getPlayer(name);
    }

    public void update() {
        LothPlayer lothPlayer = Core.getPlayerController().get(getPlayer().getUniqueId());

        boolean boosterActive = false;
        GameBooster booster = null;

        if (lothPlayer != null && lothPlayer.getBoosters() != null) {
            boosterActive = lothPlayer.getBoosters().stream().filter(gameBooster -> gameBooster.getGameType() == GameType.BED_WARS).collect(Collectors.toList()).stream().anyMatch(b -> b.getStatus() == BoosterStatus.ACTIVE);
        }

        if (boosterActive) {
            booster = lothPlayer.getBoosters().stream().filter(gameBooster -> gameBooster.getGameType() == GameType.BED_WARS).collect(Collectors.toList()).stream().filter(b -> b.getStatus() == BoosterStatus.ACTIVE).findFirst().orElse(null);
        }

        RoomType roomType = Bedwars.getInstance().getGameManager().getArena().getGameInfo().getRoomType();
        DataType dataType = roomType == RoomType.SOLO ? DataType.BED_WARS_SOLO : (roomType == RoomType.DUPLAS ? DataType.BED_WARS_TEAM :
                (roomType == RoomType.TRIOS ? DataType.BED_WARS_TRIO : roomType == RoomType.RANQUEADO ? DataType.BED_WARS_RANKED : DataType.BED_WARS_QUARTETO));

        BedStats bedStats = Platform.getBedPlatform().getBedPlayerController().get(dataType, uuid);
        BedPlayer bedPlayer = Platform.getBedPlatform().getBedPlayerController().getAccount(uuid);

        if (bedPlayer == null || bedStats == null || getTeam() == null) return;

        bedStats.setCurrentWinstreak((winner ? (bedStats.getCurrentWinstreak() + 1) : 0));
        bedStats.setKills((kills + bedStats.getKills()));
        bedStats.setFinalKills((finalKills + bedStats.getFinalKills()));
        bedStats.setDestroyedBeds((brokenBeds + bedStats.getDestroyedBeds()));
        bedStats.setGames((bedStats.getGames() + 1));
        bedStats.setWins((winner ? (bedStats.getWins() + 1) : bedStats.getWins()));
        bedStats.setLoses((winner ? bedStats.getLoses() : (bedStats.getLoses() + 1)));
        bedStats.setFinalDeaths((winner ? bedStats.getFinalDeaths() : (bedStats.getFinalDeaths() + 1)));
        bedStats.setLossBeds((getTeam().isBedBroken() ? (bedStats.getLossBeds() + 1) : bedStats.getLossBeds()));

        if (bedStats.getBestWinstreak() < bedStats.getCurrentWinstreak()) {
            bedStats.setBestWinstreak(bedStats.getCurrentWinstreak());
        }
        bedPlayer.setTotalKills(bedPlayer.getTotalKills() + kills);
        bedPlayer.setTotalWins(bedPlayer.getTotalWins() + (winner ? 1 : 0));

        if (booster != null) {
            bedPlayer.setCoins((int) (bedPlayer.getCoins() + ((getCoins() + (winner ? 30 : 10)) * (booster.getType() == BoosterType.COINS ? booster.getMultiplier() : 1))));
            bedPlayer.setXp((int) (bedPlayer.getXp() + (getXp() + (winner ? 20 : 5) * (booster.getType() == BoosterType.XP ? booster.getMultiplier() : 1))));
        } else {
            bedPlayer.setCoins(bedPlayer.getCoins() + (getCoins() + (winner ? 30 : 10)));
            bedPlayer.setXp(bedPlayer.getXp() + (getCoins() + (winner ? 20 : 5)));
        }

        if (Bedwars.getInstance().getGameManager().getArena().getGameInfo().getRoomType() == RoomType.RANQUEADO) {
            if (booster != null) {
                bedPlayer.setPoints((int) Math.max(bedPlayer.getPoints() + (getPoints() * (booster.getType() == BoosterType.POINTS ? booster.getMultiplier() : 1)), 0));
            } else {
                bedPlayer.setPoints(Math.max(bedPlayer.getPoints() + getPoints(), 0));
            }
        }

        if (bedPlayer.getQuickbuys() != null) {
            bedPlayer.getQuickbuys().clear();
        }

        quickbuy.forEach(bedPlayer::addQuickBuy);

        Platform.getDataStats().update(dataType, bedStats);
        Platform.getDataPlayer().update(DataType.BED_WARS_ACCOUNT, bedPlayer);

        System.out.println("stats updated for " + name);
    }

    public void giveItems() {
        ItemStack itemStack;

        if (!team.getTeamUpgrades().isSharpness()) {
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
        if (getPlayer() != null) {
            if (team.getTeamUpgrades().getHaste() > 0) {
                getPlayer().addPotionEffect(new PotionEffect(PotionEffectType.FAST_DIGGING, Integer.MAX_VALUE, (team.getTeamUpgrades().getHaste() - 1)));
            }
            getPlayer().getInventory().addItem(itemStack);
            giveArmor();
            givePickaxe();
            giveAxe();

            if (userUpgrades.isShears()) {
                ItemStack shears = new ItemStack(Material.SHEARS);
                ItemMeta itemMeta = shears.getItemMeta();
                itemMeta.spigot().setUnbreakable(true);
                shears.setItemMeta(itemMeta);

                getPlayer().getInventory().addItem(shears);
            }
        }
    }

    public void giveArmor() {
        Player player = getPlayer();

        Bedwars.getInstance().getGameManager().setLeatherArmor(player, team.getTeamColor().getLeather());

        switch (userUpgrades.getArmorLevel()) {
            case 1:
                player.getInventory().setBoots(new ItemStack(Material.CHAINMAIL_BOOTS));
                player.getInventory().setLeggings(new ItemStack(Material.CHAINMAIL_LEGGINGS));
                break;
            case 2:
                player.getInventory().setBoots(new ItemStack(Material.IRON_BOOTS));
                player.getInventory().setLeggings(new ItemStack(Material.IRON_LEGGINGS));
                break;
            case 3:
                player.getInventory().setBoots(new ItemStack(Material.DIAMOND_BOOTS));
                player.getInventory().setLeggings(new ItemStack(Material.DIAMOND_LEGGINGS));
                break;
        }
        if (team.getTeamUpgrades().getProtection() > 0) {
            ItemStack helmet = player.getInventory().getHelmet();
            ItemMeta helmetItemMeta = helmet.getItemMeta();
            helmetItemMeta.spigot().setUnbreakable(true);
            helmetItemMeta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, team.getTeamUpgrades().getProtection(), true);
            helmet.setItemMeta(helmetItemMeta);

            ItemStack chestplate = player.getInventory().getChestplate();
            ItemMeta chestplateItemMeta = chestplate.getItemMeta();
            chestplateItemMeta.spigot().setUnbreakable(true);
            chestplateItemMeta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, team.getTeamUpgrades().getProtection(), true);
            chestplate.setItemMeta(chestplateItemMeta);

            ItemStack leggings = player.getInventory().getLeggings();
            ItemMeta leggingItemMeta = leggings.getItemMeta();
            leggingItemMeta.spigot().setUnbreakable(true);
            leggingItemMeta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, team.getTeamUpgrades().getProtection(), true);
            leggings.setItemMeta(leggingItemMeta);

            ItemStack boots = player.getInventory().getBoots();
            ItemMeta bootItemMeta = boots.getItemMeta();
            bootItemMeta.spigot().setUnbreakable(true);
            bootItemMeta.addEnchant(Enchantment.PROTECTION_ENVIRONMENTAL, team.getTeamUpgrades().getProtection(), true);
            boots.setItemMeta(bootItemMeta);

            player.getInventory().setHelmet(helmet);
            player.getInventory().setChestplate(chestplate);
            player.getInventory().setLeggings(leggings);
            player.getInventory().setBoots(boots);
            player.updateInventory();
        } else {
            ItemStack helmet = player.getInventory().getHelmet();
            ItemMeta helmetItemMeta = helmet.getItemMeta();
            helmetItemMeta.spigot().setUnbreakable(true);
            helmet.setItemMeta(helmetItemMeta);

            ItemStack chestplate = player.getInventory().getChestplate();
            ItemMeta chestplateItemMeta = chestplate.getItemMeta();
            chestplateItemMeta.spigot().setUnbreakable(true);
            chestplate.setItemMeta(chestplateItemMeta);

            ItemStack leggings = player.getInventory().getLeggings();
            ItemMeta leggingItemMeta = leggings.getItemMeta();
            leggingItemMeta.spigot().setUnbreakable(true);
            leggings.setItemMeta(leggingItemMeta);

            ItemStack boots = player.getInventory().getBoots();
            ItemMeta bootItemMeta = boots.getItemMeta();
            bootItemMeta.spigot().setUnbreakable(true);
            boots.setItemMeta(bootItemMeta);

            player.getInventory().setHelmet(helmet);
            player.getInventory().setChestplate(chestplate);
            player.getInventory().setLeggings(leggings);
            player.getInventory().setBoots(boots);
            player.updateInventory();
        }
    }

    public void givePickaxe() {
        Player player = getPlayer();

        switch (userUpgrades.getPickLevel()) {
            case 1:
                ItemStack wood = new ItemCreator(Material.WOOD_PICKAXE).addEnchant(Enchantment.DIG_SPEED, 1).build();
                ItemMeta woodItemMeta = wood.getItemMeta();
                woodItemMeta.spigot().setUnbreakable(true);
                wood.setItemMeta(woodItemMeta);

                player.getInventory().addItem(wood);
                break;
            case 2:
                ItemStack stone = new ItemCreator(Material.STONE_PICKAXE).addEnchant(Enchantment.DIG_SPEED, 1).build();
                ItemMeta stoneItemMeta = stone.getItemMeta();
                stoneItemMeta.spigot().setUnbreakable(true);
                stone.setItemMeta(stoneItemMeta);

                player.getInventory().addItem(stone);
                break;
            case 3:
                ItemStack iron = new ItemCreator(Material.IRON_PICKAXE).addEnchant(Enchantment.DIG_SPEED, 1).build();
                ItemMeta ironItemMeta = iron.getItemMeta();
                ironItemMeta.spigot().setUnbreakable(true);
                iron.setItemMeta(ironItemMeta);

                player.getInventory().addItem(iron);
                break;
            case 4:
                ItemStack gold = new ItemCreator(Material.GOLD_PICKAXE).addEnchant(Enchantment.DIG_SPEED, 1).build();
                ItemMeta goldItemMeta = gold.getItemMeta();
                goldItemMeta.spigot().setUnbreakable(true);
                gold.setItemMeta(goldItemMeta);

                player.getInventory().addItem(gold);
                break;
            case 5:
                ItemStack diamond = new ItemCreator(Material.DIAMOND_PICKAXE).addEnchant(Enchantment.DIG_SPEED, 1).build();
                ItemMeta diamondItemMeta = diamond.getItemMeta();
                diamondItemMeta.spigot().setUnbreakable(true);
                diamond.setItemMeta(diamondItemMeta);

                player.getInventory().addItem(diamond);
                break;
        }
    }

    public void giveAxe() {
        Player player = getPlayer();

        switch (userUpgrades.getAxeLevel()) {
            case 1:
                ItemStack wood = new ItemCreator(Material.WOOD_AXE).addEnchant(Enchantment.DIG_SPEED, 1).build();
                ItemMeta woodItemMeta = wood.getItemMeta();
                woodItemMeta.spigot().setUnbreakable(true);
                wood.setItemMeta(woodItemMeta);

                player.getInventory().addItem(wood);
                break;
            case 2:
                ItemStack stone = new ItemCreator(Material.STONE_AXE).addEnchant(Enchantment.DIG_SPEED, 1).build();
                ItemMeta stoneItemMeta = stone.getItemMeta();
                stoneItemMeta.spigot().setUnbreakable(true);
                stone.setItemMeta(stoneItemMeta);

                player.getInventory().addItem(stone);
                break;
            case 3:
                ItemStack iron = new ItemCreator(Material.IRON_AXE).addEnchant(Enchantment.DIG_SPEED, 1).build();
                ItemMeta ironItemMeta = iron.getItemMeta();
                ironItemMeta.spigot().setUnbreakable(true);
                iron.setItemMeta(ironItemMeta);

                player.getInventory().addItem(iron);
                break;
            case 4:
                ItemStack gold = new ItemCreator(Material.GOLD_AXE).addEnchant(Enchantment.DIG_SPEED, 1).build();
                ItemMeta goldItemMeta = gold.getItemMeta();
                goldItemMeta.spigot().setUnbreakable(true);
                gold.setItemMeta(goldItemMeta);

                player.getInventory().addItem(gold);
                break;
            case 5:
                ItemStack diamond = new ItemCreator(Material.DIAMOND_AXE).addEnchant(Enchantment.DIG_SPEED, 1).build();
                ItemMeta diamondItemMeta = diamond.getItemMeta();
                diamondItemMeta.spigot().setUnbreakable(true);
                diamond.setItemMeta(diamondItemMeta);

                player.getInventory().addItem(diamond);
                break;
        }
    }

    public User track(Player player, Team team) {
        AtomicReference<User> target = new AtomicReference<>();

        if (team.getPlayers().size() > 1) {
            team.getPlayers().stream().filter(user -> !user.isDead()).min((o1, o2) -> (int) (player.getLocation().distanceSquared(o1.getPlayer().getLocation()) - player.getLocation().distanceSquared(o2.getPlayer().getLocation()))).
                    ifPresent(target::set);
        } else {
            target.set(team.getPlayers().get(0));
        }
        return target.get();
    }

}
