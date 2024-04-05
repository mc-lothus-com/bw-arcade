package com.lothus.engines.bedwars.listener.server.potion;

import com.lothus.core.api.tag.TagManager;
import com.lothus.engines.bedwars.Bedwars;
import com.lothus.engines.bedwars.arena.Arena;
import com.lothus.engines.bedwars.user.User;
import net.minecraft.server.v1_8_R3.EntityPlayer;
import net.minecraft.server.v1_8_R3.PacketPlayOutEntityEquipment;
import net.minecraft.server.v1_8_R3.PlayerConnection;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.craftbukkit.v1_8_R3.entity.CraftPlayer;
import org.bukkit.craftbukkit.v1_8_R3.inventory.CraftItemStack;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.NameTagVisibility;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class PotionListener implements Listener {

    private final static Arena arena = Bedwars.getInstance().getGameManager().getArena();

    private static final Map<Player, Long> invisibility = new HashMap<>();

    @EventHandler
    public void onDrink(PlayerItemConsumeEvent event) {
        Player player = event.getPlayer();

        if (player.getItemInHand().getDurability() == 8238) {
            event.setCancelled(true);

            Bukkit.getScheduler().scheduleSyncDelayedTask(Bedwars.getInstance(), () -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, (20 * 30), 0));
                player.getInventory().setItemInHand(null);

                hideArmor(player);
            }, 1L);
            invisibility.put(player, (System.currentTimeMillis() + TimeUnit.SECONDS.toMillis(30)));
        } else if (player.getItemInHand().getDurability() == 8194) {
            event.setCancelled(true);

            Bukkit.getScheduler().scheduleSyncDelayedTask(Bedwars.getInstance(), () -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, (20 * 45), 1));
                player.getInventory().setItemInHand(null);
            }, 1L);
        } else if (player.getItemInHand().getDurability() == 8203) {
            event.setCancelled(true);

            Bukkit.getScheduler().scheduleSyncDelayedTask(Bedwars.getInstance(), () -> {
                player.addPotionEffect(new PotionEffect(PotionEffectType.JUMP, (20 * 45), 4));
                player.getInventory().setItemInHand(null);
            }, 1L);
        } else if (player.getItemInHand().getType() == Material.MILK_BUCKET) {
            event.setCancelled(true);

            player.getActivePotionEffects().forEach(potionEffect -> player.removePotionEffect(potionEffect.getType()));
            player.getInventory().setItemInHand(null);

            invisibility.remove(player);
            showArmor(player);
        }
    }

    @EventHandler
    public void onHit(EntityDamageByEntityEvent event) {
        if (event.getEntity() instanceof Player && event.getDamager() instanceof Player) {
            Player player = (Player) event.getEntity();

            if (invisibility.containsKey(player)) {
                invisibility.remove(player);
                player.removePotionEffect(PotionEffectType.INVISIBILITY);
                showArmor(player);
            }
        }
        if (event.getEntity() instanceof Player) {
            User user = arena.getUserService().get(event.getEntity().getUniqueId());

            user.setLastDamage(event.getDamager());
            user.setLastDamageTick(System.currentTimeMillis());
        }
    }

    public void hideArmor(Player victim) {
        Bukkit.getScheduler().scheduleAsyncDelayedTask(Bedwars.getInstance(), () -> {
            PacketPlayOutEntityEquipment hand = new PacketPlayOutEntityEquipment(victim.getEntityId(), 0, CraftItemStack.asNMSCopy(new ItemStack(Material.AIR)));
            PacketPlayOutEntityEquipment helmet = new PacketPlayOutEntityEquipment(victim.getEntityId(), 1, CraftItemStack.asNMSCopy(new ItemStack(Material.AIR)));
            PacketPlayOutEntityEquipment chest = new PacketPlayOutEntityEquipment(victim.getEntityId(), 2, CraftItemStack.asNMSCopy(new ItemStack(Material.AIR)));
            PacketPlayOutEntityEquipment pants = new PacketPlayOutEntityEquipment(victim.getEntityId(), 3, CraftItemStack.asNMSCopy(new ItemStack(Material.AIR)));
            PacketPlayOutEntityEquipment boots = new PacketPlayOutEntityEquipment(victim.getEntityId(), 4, CraftItemStack.asNMSCopy(new ItemStack(Material.AIR)));

            Bukkit.getOnlinePlayers().forEach(receiver -> {
                if (victim.equals(receiver)) return;

                hideNick(receiver.getScoreboard(), victim, "I");

                PlayerConnection boundTo = ((CraftPlayer) receiver).getHandle().playerConnection;
                boundTo.sendPacket(hand);
                boundTo.sendPacket(helmet);
                boundTo.sendPacket(chest);
                boundTo.sendPacket(pants);
                boundTo.sendPacket(boots);
            });
        });
    }

    public static void showArmor(Player victim) {
        Bukkit.getScheduler().scheduleAsyncDelayedTask(Bedwars.getInstance(), () -> {
            EntityPlayer entityPlayer = ((CraftPlayer) victim).getHandle();
            PacketPlayOutEntityEquipment hand = new PacketPlayOutEntityEquipment(entityPlayer.getId(), 0, entityPlayer.inventory.getItemInHand());
            PacketPlayOutEntityEquipment helmet = new PacketPlayOutEntityEquipment(entityPlayer.getId(), 1, entityPlayer.inventory.getArmorContents()[1]);
            PacketPlayOutEntityEquipment chest = new PacketPlayOutEntityEquipment(entityPlayer.getId(), 2, entityPlayer.inventory.getArmorContents()[2]);
            PacketPlayOutEntityEquipment pants = new PacketPlayOutEntityEquipment(entityPlayer.getId(), 3, entityPlayer.inventory.getArmorContents()[3]);
            PacketPlayOutEntityEquipment boots = new PacketPlayOutEntityEquipment(entityPlayer.getId(), 4, entityPlayer.inventory.getArmorContents()[0]);

            User user = arena.getUserService().get(victim.getUniqueId());

            Bukkit.getOnlinePlayers().forEach(o -> {
                if (victim.equals(o)) return;

                clear(user.getPlayer(), o.getScoreboard());
            });
            TagManager.setTag(victim, user.getTeam().getTeamColor().getColoredName().substring(0, 5) +
                    user.getTeam().getTeamColor().getColoredName().substring(0, 2) + " ", "", user.getTeam().getTeamColor().getPosition());

            Bukkit.getOnlinePlayers().forEach(receiver -> {
                EntityPlayer boundTo = ((CraftPlayer) receiver).getHandle();

                if (victim != receiver) {
                    boundTo.playerConnection.sendPacket(hand);
                }
                boundTo.playerConnection.sendPacket(helmet);
                boundTo.playerConnection.sendPacket(chest);
                boundTo.playerConnection.sendPacket(pants);
                boundTo.playerConnection.sendPacket(boots);
            });
        });
    }

    private static void hideNick(Scoreboard scoreboard, Player player, String position) {
        Bukkit.getOnlinePlayers().forEach(o -> clear(player, o.getScoreboard()));

        Team team = scoreboard.getTeam(position);

        if (team == null) {
            team = scoreboard.registerNewTeam(position);
        }
        if (!team.hasPlayer(player)) {
            team.addPlayer(player);
        }
        team.setPrefix("");
        team.setSuffix("");
        team.setNameTagVisibility(NameTagVisibility.NEVER);
    }

    private static void clear(Player player, Scoreboard scoreboard) {
        for (Team team : scoreboard.getTeams()) {
            if (team.getPlayers().contains(player)) {
                team.removePlayer(player);
            }
            if (team.getEntries().contains(player.getName())) {
                team.removeEntry(player.getName());
            }
        }
    }

    public static Map<Player, Long> getInvisibility() {
        return invisibility;
    }
}
