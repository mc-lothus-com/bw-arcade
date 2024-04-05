package com.lothus.engines.bedwars.listener.server.upgrade;

import com.lothus.core.api.actionbar.ActionBar;
import com.lothus.engines.bedwars.Bedwars;
import com.lothus.engines.bedwars.arena.Arena;
import com.lothus.engines.bedwars.event.server.TimeSecondEvent;
import com.lothus.engines.bedwars.listener.server.potion.PotionListener;
import com.lothus.engines.bedwars.user.User;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.DisplaySlot;
import org.bukkit.scoreboard.Objective;
import org.github.paperspigot.Title;

public class UpgradeListener implements Listener {

    private final Arena arena;
    private final int trap;

    public UpgradeListener() {
        arena = Bedwars.getInstance().getGameManager().getArena();
        trap = Bedwars.getInstance().getConfig().getInt("protection.trap");
    }

    @EventHandler
    public void onTimeSecond(TimeSecondEvent event) {
        Bukkit.getOnlinePlayers().forEach(player -> {
            User user = arena.getUserService().get(player.getUniqueId());

            if (user.getTeam() == null || !user.getTeam().isAlive()) return;

            if (user.getTeam().getTeamUpgrades().isRegen()) {
                if (user.getTeam().getBedLocation().getLocation().distance(player.getLocation()) <= trap) {
                    if (player.getActivePotionEffects().stream().noneMatch(potionEffect -> potionEffect.getType() == PotionEffectType.REGENERATION)) {
                        player.addPotionEffect(new PotionEffect(PotionEffectType.REGENERATION, (20 * 5), 0));
                    }
                } else {
                    if (player.getActivePotionEffects().stream().anyMatch(potionEffect -> potionEffect.getType() == PotionEffectType.REGENERATION)) {
                        player.removePotionEffect(PotionEffectType.REGENERATION);
                    }
                }
            }
            arena.getTeams().stream().filter(team -> team.isAlive() && player.getLocation().distance(team.getBedLocation().getLocation()) <= trap && user.getTeam().getTeamColor() != team.getTeamColor()).findAny().ifPresent(team -> {
                if (team.getTeamUpgrades().isTrap()) {
                    if (player.getActivePotionEffects().stream().noneMatch(potionEffect -> potionEffect.getType() == PotionEffectType.SLOW)) {
                        player.removePotionEffect(PotionEffectType.SLOW);
                        player.removePotionEffect(PotionEffectType.BLINDNESS);
                    }
                    player.addPotionEffect(new PotionEffect(PotionEffectType.SLOW, (20 * 8), 0));
                    player.addPotionEffect(new PotionEffect(PotionEffectType.BLINDNESS, (20 * 8), 0));
                    player.sendTitle(new Title("§c§lARMADILHA", "§7Você ativou a armadilha do time " + team.getTeamColor().getColoredName().substring(0, 2) + team.getTeamColor().getNormalName() + "§7.", 0, 30, 0));
                    player.sendMessage("§c§lARMADILHA §f» §7Você ativou a armadilha do time " + team.getTeamColor().getColoredName().substring(0, 2) + team.getTeamColor().getNormalName() + "§7.");

                    team.getTeamUpgrades().setTrap(false);
                    team.getPlayers().forEach(u -> {
                        u.getPlayer().playSound(u.getPlayer().getLocation(), Sound.ENDERDRAGON_WINGS, 1F, 1F);
                        u.getPlayer().sendTitle(new Title("§c§lARMADILHA ATIVADA", "§7Invasor na ilha, a armadilha foi ativada!", 0, 30, 0));
                        u.getPlayer().sendMessage("§c§lARMADILHA §f» §7Invasor em sua ilha! A sua armadilha foi ativada.");
                    });
                }
            });
            if (PotionListener.getInvisibility().containsKey(player)) {
                if (PotionListener.getInvisibility().get(player) < System.currentTimeMillis()) {
                    PotionListener.getInvisibility().remove(player);
                    PotionListener.showArmor(player);
                }
            }
            if (user.getTracking() != null) {
                User track = user.track(player, user.getTracking());

                if (track == null || track.getPlayer() == null || !track.isPlaying()) {
                    user.setTracking(null);
                    return;
                }
                String s = String.valueOf((player.getLocation().distance(track.getPlayer().getLocation())));

                player.setCompassTarget(track.getPlayer().getLocation());

                ActionBar.sendActionBar(player, "§fRastreando: " + track.getTeam().getTeamColor().getColoredName().substring(0, 2) + track.getPlayer().getName() +
                        " §7- §fDistância: §b" + s.substring(0, Math.min(s.length(), 4)) + "m");
            }
            updateHealthBar(player);
        });
    }

    private void updateHealthBar(Player player) {
        Objective objective = player.getScoreboard().getObjective(DisplaySlot.BELOW_NAME);

        if (objective == null) {
            objective = player.getScoreboard().registerNewObjective("showhealth", "health");
            objective.setDisplayName("§c❤");
            objective.setDisplaySlot(DisplaySlot.BELOW_NAME);
        }
    }

}
