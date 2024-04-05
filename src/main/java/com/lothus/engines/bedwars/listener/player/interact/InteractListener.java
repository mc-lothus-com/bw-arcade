package com.lothus.engines.bedwars.listener.player.interact;

import com.lothus.core.Core;
import com.lothus.core.event.update.UpdateEvent;
import com.lothus.core.player.LothPlayer;
import com.lothus.core.player.group.rank.Rank;
import com.lothus.core.servers.type.ServerType;
import com.lothus.engines.bedwars.Bedwars;
import com.lothus.engines.bedwars.arena.Arena;
import com.lothus.engines.bedwars.event.player.PlayerSummonEvent;
import com.lothus.engines.bedwars.game.GameManager;
import com.lothus.engines.bedwars.user.User;
import com.lothus.engines.bedwars.utils.PlayerUtil;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;

import static com.lothus.core.games.room.RoomType.*;

public class InteractListener implements Listener {

    private final GameManager gameManager;
    private final Arena arena;

    public InteractListener() {
        gameManager = Bedwars.getInstance().getGameManager();
        arena = gameManager.getArena();
    }

    @EventHandler
    public void interact(PlayerInteractEvent event) {
        Player player = event.getPlayer();
        User user = arena.getUserService().get(player.getUniqueId());

        if (player.getGameMode() != GameMode.ADVENTURE) {
            if (player.getItemInHand().getType() == Material.COMPASS) {
                gameManager.getMenus().getTrackMenu().open(player);
                return;
            }
            if (player.getItemInHand().getType() == Material.BED) {
                PlayerUtil.send(player, ServerType.LOBBY_BEDWARS);
                return;
            }
            if (event.getClickedBlock() != null && event.getClickedBlock().getType() != null && event.getClickedBlock().getType() == Material.CHEST) {
                arena.getTeams().stream().filter(team -> team.getIronGen().getLocation().distance(event.getClickedBlock().getLocation()) <= 20).findAny().ifPresent(team -> {
                    if (team.isAlive()) {
                        for (User u : team.getPlayers()) {
                            if (u == null)return;
                            if (u.getPlayer() == null)return;

                            if (user.getTeam().getTeamColor() != u.getTeam().getTeamColor()) {
                                event.setCancelled(true);
                                player.sendMessage("§cVocê não pode abrir esse baú, pois o time " + team.getTeamColor().getColoredName().substring(0, 2) +
                                        team.getTeamColor().getNormalName() + "§c está vivo.");
                                return;
                            }
                        }
                    }
                });
                return;
            }
            if (event.getClickedBlock() != null && event.getClickedBlock().getType().name().contains("BED") && !event.getClickedBlock().getType().name().endsWith("ROCK") && event.getAction().name().contains("RIGHT")) {
                if (!event.getPlayer().isSneaking()) {
                    event.setCancelled(true);
                }
                return;
            }
            if (player.getItemInHand().getType() == Material.MONSTER_EGG) {
                if (player.getItemInHand().getAmount() > 1) {
                    player.getItemInHand().setAmount((player.getItemInHand().getAmount() - 1));
                } else {
                    player.setItemInHand(null);
                }
                Bukkit.getPluginManager().callEvent(new PlayerSummonEvent(player, user, EntityType.IRON_GOLEM, null));
            }
        } else {
            if (player.getItemInHand().getType() == Material.COMPASS) {
                gameManager.getMenus().getCompassMenu().open(player);
            } else if (player.getItemInHand().getType() == Material.PAPER) {
                player.chat("/play " + (arena.getGameInfo().getRoomType() == SOLO ? "bwsolo" : arena.getGameInfo().getRoomType() == DUPLAS ? "bwteam" : arena.getGameInfo().getRoomType() == TRIOS ?  "bwtrio" : "bwquarteto"));

                if (user == null) return;

                user.update();
            } else if (player.getItemInHand().getType() == Material.BED) {
                if (player.getItemInHand().hasItemMeta() && player.getItemInHand().getItemMeta().getDisplayName().equalsIgnoreCase("§cVoltar para o lobby")) {
                    PlayerUtil.send(player, ServerType.LOBBY_BEDWARS);

                    if (user == null) return;

                    user.update();
                }
                event.setCancelled(true);
            } else {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void onTime(UpdateEvent event) {
        for (Player o : Bukkit.getOnlinePlayers()) {
            LothPlayer on = Core.getPlayerController().get(o.getUniqueId());
            if (on.getPrefs().isVanish()) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    if (o == p) continue;

                    LothPlayer target = Core.getPlayerController().get(p.getUniqueId());

                    if (!target.hasPermission(Rank.TRIAL)) {
                        try {
                            p.hidePlayer(o);
                        } catch (Exception ignored) {}
                    }
                }
            }
        }
    }
}
