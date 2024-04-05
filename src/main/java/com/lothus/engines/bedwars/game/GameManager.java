package com.lothus.engines.bedwars.game;

import com.google.common.collect.Lists;
import com.lothus.core.Core;
import com.lothus.core.api.tag.TagManager;
import com.lothus.core.games.GameInfo;
import com.lothus.core.games.room.RoomType;
import com.lothus.core.games.state.GameState;
import com.lothus.core.games.type.GameType;
import com.lothus.core.player.party.Party;
import com.lothus.core.player.rejoin.Rejoin;
import com.lothus.core.servers.type.ServerType;
import com.lothus.core.storage.redis.channels.RedisChannel;
import com.lothus.core.utils.bukkit.ItemCreator;
import com.lothus.engines.bedwars.Bedwars;
import com.lothus.engines.bedwars.arena.Arena;
import com.lothus.engines.bedwars.entity.Despawnable;
import com.lothus.engines.bedwars.game.task.TimerTask;
import com.lothus.engines.bedwars.generator.server.ServerGenerators;
import com.lothus.engines.bedwars.generator.team.TeamGenerators;
import com.lothus.engines.bedwars.location.Locations;
import com.lothus.engines.bedwars.location.manager.LocationManager;
import com.lothus.engines.bedwars.location.type.LocationType;
import com.lothus.engines.bedwars.menu.Menus;
import com.lothus.engines.bedwars.service.user.UserService;
import com.lothus.engines.bedwars.task.ReJoinTask;
import com.lothus.engines.bedwars.team.Team;
import com.lothus.engines.bedwars.team.color.TeamColor;
import com.lothus.engines.bedwars.team.manager.TeamManager;
import com.lothus.engines.bedwars.user.User;
import com.lothus.engines.bedwars.utils.PlayerUtil;
import com.lothus.engines.bedwars.utils.StringUtils;
import lombok.Getter;
import net.jitse.npclib.NPCLib;
import net.jitse.npclib.api.NPC;
import net.jitse.npclib.api.skin.Skin;
import net.jitse.npclib.api.state.NPCSlot;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.*;
import org.bukkit.Color;
import org.bukkit.block.Block;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.*;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.FireworkMeta;
import org.bukkit.inventory.meta.LeatherArmorMeta;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scheduler.BukkitRunnable;
import org.github.paperspigot.Title;

import java.awt.*;
import java.util.*;
import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.lothus.core.games.room.RoomType.*;
import static com.lothus.core.storage.redis.channels.RedisChannel.REJOIN;

@Getter
public class GameManager {

    private final Arena arena;
    private final LocationManager locationManager;
    private final TeamManager teamManager;
    private final UserService userService;
    private final NPCLib npcLib;
    private final int rejoin;
    private final Map<UUID, Despawnable> despawnables;
    private ServerGenerators serverGenerators;
    private TeamGenerators teamGenerators;
    private Menus menus;

    public GameManager() {
        FileConfiguration config = Bedwars.getInstance().getConfig();

        arena = new Arena(config.getInt("room.minPlayers"), new GameInfo(config.getString("room.id"), config.getString("room.name"),
                Core.getServerInfo().getName(), GameType.BED_WARS, RoomType.getRoomType(config.getString("room.type")),
                config.getInt("room.maxPlayers")));
        locationManager = new LocationManager();
        teamManager = new TeamManager(locationManager);
        userService = arena.getUserService();
        npcLib = new NPCLib(Bedwars.getInstance());
        rejoin = config.getInt("room.rejoin");
        despawnables = new HashMap<>();
    }

    public void enable() {
        locationManager.load();
        teamManager.load();
        serverGenerators = new ServerGenerators(this);
        teamGenerators = new TeamGenerators(this);
        menus = new Menus(arena);

        Locations lobby = locationManager.get(LocationType.LOBBY, null);

        if (lobby != null) {
            arena.setLobby(lobby.getLocation());
        }
        arena.setTimer(new TimerTask(arena, 30));
        Core.getRedis().message(RedisChannel.GAME_START.name(), Core.getGson().toJson(arena.getGameInfo()));
    }

    public void disable() {
        Core.getRedis().message(RedisChannel.GAME_STOP.name(), Core.getGson().toJson(arena.getGameInfo()));
        Bukkit.getWorld("world").getEntities().forEach(Entity::remove);
    }

    public void selectTeams() {
        Set<Team> teams = (arena.getGameInfo().getRoomType() != RANQUEADO ? teamManager.getTeamService().all()
                : teamManager.getTeamService().all().stream().filter(team -> team.getTeamColor().isRanked()).collect(Collectors.toSet()));

        teams.forEach(team -> {
            if (team.getShopVillager() == null || team.getUpgradeVillager() == null) return;

            Location shopLoc = team.getShopVillager().getLocation();
            Location upgradeLoc = team.getUpgradeVillager().getLocation();

            NPC shop = spawnNPC("ewogICJ0aW1lc3RhbXAiIDogMTY4NTU2NTAwMDQ2MSwKICAicHJvZmlsZUlkIiA6ICI4ZjE5NjJmYzE4NzY0MDU3ODYxMmIxMzNjODE4YmY5OSIsCiAgInByb2ZpbGVOYW1lIiA6ICJOaW9uXzkiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2VkMWEzYzYzMDc0ZmVkNmY4MGY1ODFkZjlkY2Y5NDc5NTM4MTE3MDc1ODViZWFjODZjMjE1ZmI0MGNhNjZiYiIKICAgIH0KICB9Cn0=",
                    "Fu6jJm1JdOvkJ7pWsZ+sl1amnkBQmMUbqO+cCos2xl9uUa4ybMYMTCqqe+ImzLAZ2zQA7pfslkub7wAZuoh75OT/Rm+N3rLwtYOESJu2RMo76eI+oHXvlhCXDDAyVjIhqrVC1cRpFRKmCH3JG3ZLCbMZcYUCnhP4xsGzWjZWjff5Cswg8KlZhJJ9gLQ34Tj5uOCN3ta+l5dv+008z2jdPIkgFL74BfarpIIh3ll7zvbpuKwCjOH1v98zWIDMCa7e01irrQJkqNxxmmRxkqLMhU6c4DMIb6dkQouMTLG9UA9gqCLQa3WrtGnd5vet1utc/ANkdMPNDekOAGoOmGVW/RhYG3X25Wp1Rc4L1+mxhLdlIdOy10X0pD2JtJ0VE6tfc4X0kbO+NkEuwEQZBt8CZB0JQFj4kqEQv0lD05Y8jLPpyFQV0dPpA1eNUiWnKEo4kAFL7Cwuw2Q0Xo4XkJNweRTS4gc+j5BgrVg7oRMsGtYv0AC1Ay64VLFS3GkZPjh5iLMMYEjn/AC0jGviiZVXbQJkINa/Kg0GxYoObc3ydvueiaSk3L71ZhUN1RQ+B2/+CmhWm0J2dxLqUV3RWxeZb1uOf6qQkxgM+KQqcMW/1U2w35qbFKQhtU8pXnW6lEIoFpwEFp4n1FkfK6+Wr0yckRKM81MgLWatSjlHvz/CMsk=",
                    shopLoc, Arrays.asList("§6§lLOJA", "§fClique para ver.")).setItem(NPCSlot.MAINHAND, new ItemStack(Material.DIAMOND_SWORD));
            NPC upgrade = spawnNPC("ewogICJ0aW1lc3RhbXAiIDogMTY4NTU2NTAwMDQ2MSwKICAicHJvZmlsZUlkIiA6ICI4ZjE5NjJmYzE4NzY0MDU3ODYxMmIxMzNjODE4YmY5OSIsCiAgInByb2ZpbGVOYW1lIiA6ICJOaW9uXzkiLAogICJzaWduYXR1cmVSZXF1aXJlZCIgOiB0cnVlLAogICJ0ZXh0dXJlcyIgOiB7CiAgICAiU0tJTiIgOiB7CiAgICAgICJ1cmwiIDogImh0dHA6Ly90ZXh0dXJlcy5taW5lY3JhZnQubmV0L3RleHR1cmUvM2VkMWEzYzYzMDc0ZmVkNmY4MGY1ODFkZjlkY2Y5NDc5NTM4MTE3MDc1ODViZWFjODZjMjE1ZmI0MGNhNjZiYiIKICAgIH0KICB9Cn0=",
                    "Fu6jJm1JdOvkJ7pWsZ+sl1amnkBQmMUbqO+cCos2xl9uUa4ybMYMTCqqe+ImzLAZ2zQA7pfslkub7wAZuoh75OT/Rm+N3rLwtYOESJu2RMo76eI+oHXvlhCXDDAyVjIhqrVC1cRpFRKmCH3JG3ZLCbMZcYUCnhP4xsGzWjZWjff5Cswg8KlZhJJ9gLQ34Tj5uOCN3ta+l5dv+008z2jdPIkgFL74BfarpIIh3ll7zvbpuKwCjOH1v98zWIDMCa7e01irrQJkqNxxmmRxkqLMhU6c4DMIb6dkQouMTLG9UA9gqCLQa3WrtGnd5vet1utc/ANkdMPNDekOAGoOmGVW/RhYG3X25Wp1Rc4L1+mxhLdlIdOy10X0pD2JtJ0VE6tfc4X0kbO+NkEuwEQZBt8CZB0JQFj4kqEQv0lD05Y8jLPpyFQV0dPpA1eNUiWnKEo4kAFL7Cwuw2Q0Xo4XkJNweRTS4gc+j5BgrVg7oRMsGtYv0AC1Ay64VLFS3GkZPjh5iLMMYEjn/AC0jGviiZVXbQJkINa/Kg0GxYoObc3ydvueiaSk3L71ZhUN1RQ+B2/+CmhWm0J2dxLqUV3RWxeZb1uOf6qQkxgM+KQqcMW/1U2w35qbFKQhtU8pXnW6lEIoFpwEFp4n1FkfK6+Wr0yckRKM81MgLWatSjlHvz/CMsk=",
                    upgradeLoc, Arrays.asList("§6§lUPGRADE", "§fClique para ver.")).setItem(NPCSlot.MAINHAND, new ItemStack(Material.DIAMOND));

            Bukkit.getScheduler().scheduleSyncDelayedTask(Bedwars.getInstance(), () -> {
                userService.all().forEach(user -> {
                    shop.show(user.getPlayer());
                    upgrade.show(user.getPlayer());
                });
            }, 5L);
            team.setShop(shop);
            team.setUpgrade(upgrade);
        });
        userService.playing().stream().filter(user -> user.getTeam() == null).forEach(user -> {
            Party party = Core.getPartyController().get(user.getPlayer().getUniqueId());
            Team team = teams.stream().filter(t -> (arena.getGameInfo().getRoomType() == SOLO || arena.getGameInfo().getRoomType() == RANQUEADO ? t.getPlayers().isEmpty() :
                    (arena.getGameInfo().getRoomType() == DUPLAS ? t.getPlayers().size() < 2 :
                            (arena.getGameInfo().getRoomType() == TRIOS ? t.getPlayers().size() < 3 :
                                    t.getPlayers().size() < 4)))).findFirst().get();

            if (arena.getGameInfo().getRoomType() != SOLO && arena.getGameInfo().getRoomType() != RANQUEADO) {
                if (party != null) {
                    if (party.getMembers().size() > 1) {
                        if (!party.isLeader(user.getPlayer().getUniqueId())) {
                            team = teams.stream().filter(t -> t.getPlayers().stream().anyMatch(u -> u.getPlayer().getUniqueId().equals(party.getLeader()))).findFirst().get();

                            if (arena.getGameInfo().getRoomType() == DUPLAS ? team.getPlayers().size() == 2 :
                                    (arena.getGameInfo().getRoomType() == TRIOS ? team.getPlayers().size() == 3 : team.getPlayers().size() == 4)) {
                                team = teams.stream().filter(t -> (arena.getGameInfo().getRoomType() == SOLO ? t.getPlayers().isEmpty() :
                                        (arena.getGameInfo().getRoomType() == DUPLAS ? t.getPlayers().size() < 2 :
                                                (arena.getGameInfo().getRoomType() == TRIOS ? t.getPlayers().size() < 3 :
                                                        t.getPlayers().size() < 4)))).findFirst().get();
                            }
                        }
                    }
                }
            }
            Player player = user.getPlayer();
            TeamColor teamColor = team.getTeamColor();

            player.getInventory().clear();
            player.getInventory().setArmorContents(null);

            user.setTeam(team);
            user.giveItems();

            team.getPlayers().add(user);

            TagManager.setTag(player, teamColor.getColoredName().substring(0, 5) + teamColor.getColoredName().substring(0, 2) + " ", "", teamColor.getPosition());
            TagManager.update(player);

            player.sendMessage("");
            StringUtils.sendCenteredMessage(player, "§eVocê foi selecionado para o time " + teamColor.getColoredName() + "§e.");
            StringUtils.sendCenteredMessage(player, "§eProteja sua ilha e não permita");
            StringUtils.sendCenteredMessage(player, "§eque ninguém quebre sua cama!");
            player.sendMessage("");
            StringUtils.sendCenteredMessage(player, "§eBoa sorte!");

            player.teleport(team.getSpawn().getLocation());
        });
        removeUnusedTeams();
    }

    private NPC spawnNPC(String value, String signature, Location location, List<String> text) {
        NPC npc = npcLib.createNPC(text);
        npc.setLocation(location);
        npc.setSkin(new Skin(value, signature));
        npc.create();

        return npc;
    }

    private void removeUnusedTeams() {
        teamManager.getTeamService().all().stream().filter(team -> !team.isAlive()).forEach(team -> {
            System.out.println(team.getTeamColor().getNormalName());
            Location location = team.getBedLocation().getLocation();

            getNearbyBlocks(location, 3).stream().filter(block -> block.getType().name().contains("BED")).forEach(block -> block.setType(Material.AIR));
            location.getWorld().getNearbyEntities(location, 3, 3, 3).stream().filter(entity -> entity instanceof Item).map(entity -> (Item) entity).forEach(item -> {
                if (item.getItemStack().getType() == Material.BED) {
                    item.remove();
                }
            });
        });
    }

    public void removeWaitRoom() {
        getNearbyBlocks(arena.getLobby(), 16).forEach(block -> block.setType(Material.AIR));
    }

    public void spectate(User user) {
        Player player = user.getPlayer();

        user.setLastDamage(null);
        user.setLastDamageTick(0);

        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.updateInventory();
        player.setGameMode(GameMode.ADVENTURE);
        player.setAllowFlight(true);
        player.setFlying(true);
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 2));

        if (user.getTeam() != null) {
            player.sendTitle(new Title("§c§lVOCÊ MORREU", "§7Você foi eliminado da partida."));
        }
        player.getInventory().setItem(0, new ItemCreator(Material.COMPASS).setDisplayName("§aBússola").build());
        player.getInventory().setItem(7, new ItemCreator(Material.PAPER).setDisplayName("§aJogar novamente").build());
        player.getInventory().setItem(8, new ItemCreator(Material.BED).setDisplayName("§cVoltar para o lobby").build());

        TagManager.setTag(player, "§8", "", "Z");
        TagManager.update(player);

        arena.getUserService().playing().forEach(u -> u.getPlayer().hidePlayer(player));
        arena.getUserService().spectators().forEach(u -> player.showPlayer(u.getPlayer()));
    }

    public void respawn(User user) {
        Player player = user.getPlayer();

        user.setLastDamage(null);
        user.setLastDamageTick(0);

        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.updateInventory();
        player.setGameMode(GameMode.ADVENTURE);
        player.setAllowFlight(true);
        player.setFlying(true);
        player.setCanPickupItems(false);
        player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, Integer.MAX_VALUE, 2));

        arena.getUserService().spectators().forEach(u -> player.hidePlayer(u.getPlayer()));
        arena.getUserService().playing().forEach(u -> u.getPlayer().hidePlayer(player));

        new BukkitRunnable() {
            int timer = 6;

            public void run() {
                timer--;

                if (!user.getTeam().isBedBroken()) {
                    player.sendTitle(new Title("§c§lVOCÊ MORREU", "§7Você irá renascer em §e" + timer + " §7segundos!", 0, 20, 0));
                } else {
                    player.sendTitle(new Title("§c§lCAMA QUEBRADA", "§7Você irá renascer em §e" + timer + " §7segundos!", 0, 20, 0));
                }
                if (timer == 0) {
                    cancel();

                    player.sendTitle(new Title("§a§lRENASCEU", "", 0, 40, 0));
                    player.setGameMode(GameMode.SURVIVAL);
                    player.setAllowFlight(false);
                    player.setFlying(false);
                    player.setCanPickupItems(true);
                    player.removePotionEffect(PotionEffectType.INVISIBILITY);
                    player.teleport(user.getTeam().getSpawn().getLocation());

                    user.setDead(false);
                    user.giveItems();

                    arena.getUserService().playing().stream().filter(Objects::nonNull).forEach(u -> u.getPlayer().showPlayer(player));
                }
            }
        }.runTaskTimer(Bedwars.getInstance(), 0L, 20L);
    }

    public void checkWinner() {
        if (arena.getGameInfo().getState() == GameState.EM_JOGO) {
            long count = teamManager.getTeamService().all().stream().filter(team -> (team.isBedBroken() && team.getPlayers().stream().anyMatch(u -> u != null && u.getPlayer() != null && !u.isSpectator())) || team.getPlayers().stream().anyMatch(u -> u != null && u.getPlayer() != null && !u.isSpectator())).count();

            if (count == 1 || (arena.isBedsBroken() && teamManager.getTeamService().all().stream().filter(Team::isAlive).count() == 1)) {
                teamManager.getTeamService().all().stream().filter(Team::isAlive).findAny().ifPresent(team -> {
                    arena.getGameInfo().setState(GameState.ENCERRANDO);
                    arena.getTimer().cancel();

                    Core.getRedis().message(RedisChannel.GAME_STOP.name(), Core.getGson().toJson(arena.getGameInfo()));

                    Bukkit.getScheduler().cancelAllTasks();

                    List<User> finalKills = userService.all().stream().sorted(Comparator.comparingInt(User::getFinalKills).reversed()).filter(user -> user.getFinalKills() > 0).collect(Collectors.toCollection(LinkedList::new));
                    List<User> brokenBeds = userService.all().stream().sorted(Comparator.comparingInt(User::getBrokenBeds).reversed()).filter(user -> user.getBrokenBeds() > 0).collect(Collectors.toCollection(LinkedList::new));

                    Bukkit.broadcastMessage(StringUtils.line);
                    Bukkit.broadcastMessage(StringUtils.makeCenteredMessage("§6§lBED WARS"));
                    Bukkit.broadcastMessage("");
                    Bukkit.broadcastMessage(StringUtils.makeCenteredMessage("§eVencedor: " + team.getTeamColor().getColoredName() + "§7 - " + team.getPlayers().stream().map(s -> "§7, " + s.getName()).collect(Collectors.joining()).replaceFirst("§7, ", "")));
                    Bukkit.broadcastMessage("");
                    Bukkit.broadcastMessage(StringUtils.makeCenteredMessage("§6§lTOP KILLS FINAIS:"));
                    Bukkit.broadcastMessage(StringUtils.makeCenteredMessage("§a1° §7" + (finalKills.size() > 0 ? finalKills.get(0).getName() + " §7- " + finalKills.get(0).getFinalKills() : "Nenhum")));

                    if (finalKills.size() > 1) {
                        Bukkit.broadcastMessage(StringUtils.makeCenteredMessage("§e2° §7" + finalKills.get(1).getName() + " §7- " + finalKills.get(1).getFinalKills()));
                    }
                    if (finalKills.size() > 2) {
                        Bukkit.broadcastMessage(StringUtils.makeCenteredMessage("§c3° §7" + finalKills.get(2).getName() + " §7- " + finalKills.get(2).getFinalKills()));
                    }
                    Bukkit.broadcastMessage("");
                    Bukkit.broadcastMessage(StringUtils.makeCenteredMessage("§6§lTOP CAMAS QUEBRADAS:"));
                    Bukkit.broadcastMessage(StringUtils.makeCenteredMessage("§a1° §7" + (brokenBeds.size() > 0 ? brokenBeds.get(0).getName() + " §7- " + brokenBeds.get(0).getBrokenBeds() : "Nenhum")));

                    if (brokenBeds.size() > 1) {
                        Bukkit.broadcastMessage(StringUtils.makeCenteredMessage("§e2° §7" + brokenBeds.get(1).getName() + " §7- " + brokenBeds.get(1).getBrokenBeds()));
                    }
                    if (brokenBeds.size() > 2) {
                        Bukkit.broadcastMessage(StringUtils.makeCenteredMessage("§c3° §7" + brokenBeds.get(2).getName() + " §7- " + brokenBeds.get(2).getBrokenBeds()));
                    }
                    Bukkit.broadcastMessage("");
                    Bukkit.broadcastMessage(StringUtils.line);

                    TextComponent t = new TextComponent("§eDeseja jogar novamente?");
                    TextComponent click = new TextComponent(" §b§lCLIQUE AQUI!");

                    click.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/play " + (arena.getGameInfo().getRoomType() == SOLO ? "bwsolo" : arena.getGameInfo().getRoomType() == DUPLAS ? "bwteam" : arena.getGameInfo().getRoomType() == TRIOS ?  "bwtrio" : arena.getGameInfo().getRoomType() != RANQUEADO ? "bwranked" : "bwquarteto")));
                    click.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, TextComponent.fromLegacyText("§eClique para jogar!")));

                    t.addExtra(click);

                    Bukkit.broadcast(t);

                    Bukkit.getScheduler().scheduleSyncDelayedTask(Bedwars.getInstance(), () -> {
                        Bukkit.getOnlinePlayers().forEach(player -> PlayerUtil.send(player, ServerType.LOBBY_BEDWARS));
                        Bukkit.getScheduler().scheduleSyncDelayedTask(Bedwars.getInstance(), () -> Bukkit.spigot().restart(), 20L * 2);
                    }, 20L * 11);

                    userService.all().forEach(u -> u.getBedScore().update());

                    team.getPlayers().forEach(user -> {
                        Player player = user.getPlayer();

                        player.setAllowFlight(true);
                        player.setFlying(true);

                        user.setWinner(true);

                        user.addXp(20);

                        if (arena.getGameInfo().getRoomType() == RANQUEADO) {
                            user.addPoints(30);
                        }

                        new BukkitRunnable() {
                            int time = 10;

                            public void run() {
                                time--;

                                spawnFireworks(player.getLocation(), 1);
                                setLeatherArmor(player, getColor(ThreadLocalRandom.current().nextInt(17)));

                                if (time == 0) {
                                    cancel();
                                }
                            }
                        }.runTaskTimer(Bedwars.getInstance(), 20L, 20L);
                    });
                    userService.all().forEach(u -> {
                        if (u != null) {
                            u.update();
                        }
                    });
                });
            } else if (count == 0) {
                Bukkit.getOnlinePlayers().forEach(player -> PlayerUtil.send(player, ServerType.LOBBY_BEDWARS));
                Bukkit.getScheduler().scheduleSyncDelayedTask(Bedwars.getInstance(), () -> Bukkit.spigot().restart(), 20L * 2);
            }
        }
    }

    private void spawnFireworks(Location location, int amount) {
        Firework fw = (Firework) location.getWorld().spawnEntity(location, EntityType.FIREWORK);
        FireworkMeta fwm = fw.getFireworkMeta();

        fwm.setPower(2);
        fwm.addEffect(FireworkEffect.builder().withColor(getColor(ThreadLocalRandom.current().nextInt(17))).flicker(true).build());

        fw.setFireworkMeta(fwm);
        fw.detonate();

        for (int i = 0; i < amount; i++) {
            Firework fw2 = (Firework) location.getWorld().spawnEntity(location, EntityType.FIREWORK);
            fw2.setFireworkMeta(fwm);
        }
    }

    private Color getColor(int i) {
        Color c = Color.LIME;

        if (i == 0) {
            c = Color.AQUA;
        }
        if (i == 1) {
            c = Color.BLACK;
        }
        if (i == 2) {
            c = Color.BLUE;
        }
        if (i == 3) {
            c = Color.FUCHSIA;
        }
        if (i == 4) {
            c = Color.GRAY;
        }
        if (i == 5) {
            c = Color.GREEN;
        }
        if (i == 6) {
            c = Color.LIME;
        }
        if (i == 7) {
            c = Color.MAROON;
        }
        if (i == 8) {
            c = Color.NAVY;
        }
        if (i == 9) {
            c = Color.OLIVE;
        }
        if (i == 10) {
            c = Color.ORANGE;
        }
        if (i == 11) {
            c = Color.PURPLE;
        }
        if (i == 12) {
            c = Color.RED;
        }
        if (i == 13) {
            c = Color.SILVER;
        }
        if (i == 14) {
            c = Color.TEAL;
        }
        if (i == 15) {
            c = Color.WHITE;
        }
        if (i == 16) {
            c = Color.YELLOW;
        }
        return c;
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

    public String formatTime(int time) {
        int minutes = time / 60;
        int seconds = time % 60;

        if (minutes > 0) {
            return minutes + "m" + (seconds > 0 ? " " + seconds + "s" : "");
        } else {
            return seconds + "s";
        }
    }

    public void setLeatherArmor(Player p, Color cor) {
        ItemStack c = new ItemStack(Material.LEATHER_HELMET);
        LeatherArmorMeta cm = (LeatherArmorMeta) c.getItemMeta();
        cm.setColor(cor);
        c.setItemMeta(cm);

        p.getInventory().setHelmet(c);

        ItemStack c1 = new ItemStack(Material.LEATHER_CHESTPLATE);
        LeatherArmorMeta cm1 = (LeatherArmorMeta) c1.getItemMeta();
        cm1.setColor(cor);
        c1.setItemMeta(cm1);
        p.getInventory().setChestplate(c1);

        ItemStack c11 = new ItemStack(Material.LEATHER_LEGGINGS);
        LeatherArmorMeta cm11 = (LeatherArmorMeta) c11.getItemMeta();
        cm11.setColor(cor);
        c11.setItemMeta(cm11);

        p.getInventory().setLeggings(c11);

        ItemStack c111 = new ItemStack(Material.LEATHER_BOOTS);
        LeatherArmorMeta cm111 = (LeatherArmorMeta) c111.getItemMeta();
        cm111.setColor(cor);
        c111.setItemMeta(cm111);

        p.getInventory().setBoots(c111);
    }

    public void generateRejoin(Player player) {
        User user = userService.get(player.getUniqueId());

        if (user == null) return;

        if (!user.isSpectator() && !user.getTeam().isBedBroken()) {
            new ReJoinTask(user, arena, rejoin);
            Core.getRedis().message(REJOIN.name(), Core.getGson().toJson(new Rejoin(player.getUniqueId(), arena.getGameInfo().getName(), arena.getGameInfo().getType(), arena.getGameInfo().getRoomType(), System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(rejoin))));
        } else {
            if (!user.isSpectator()) {
                user.setSpectator(true);

                Bukkit.broadcastMessage(user.getTeam().getTeamColor().getColoredName().substring(0, 2) + user.getName() + " §7morreu." + (user.getTeam().isBedBroken() ? " §b§lKILL FINAL!" : ""));

                user.getTeam().eliminateTeam();

                checkWinner();
            }
        }
    }

}
