package com.lothus.engines.bedwars.listener.player.connect;

import com.lothus.core.Core;
import com.lothus.core.api.tag.TagManager;
import com.lothus.core.games.state.GameState;
import com.lothus.core.player.LothPlayer;
import com.lothus.engines.bedwars.Bedwars;
import com.lothus.engines.bedwars.arena.Arena;
import com.lothus.engines.bedwars.game.GameManager;
import com.lothus.engines.bedwars.location.Locations;
import com.lothus.engines.bedwars.location.manager.LocationManager;
import com.lothus.engines.bedwars.location.type.LocationType;
import com.lothus.engines.bedwars.service.Services;
import com.lothus.engines.bedwars.service.user.UserService;
import com.lothus.engines.bedwars.user.User;
import org.bukkit.GameMode;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerLoginEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class ConnectListener implements Listener {

    private final UserService userService;
    private final GameManager gameManager;
    private final LocationManager locationManager;

    public ConnectListener() {
        userService = Services.get(UserService.class);
        gameManager = Bedwars.getInstance().getGameManager();
        locationManager = gameManager.getLocationManager();
    }

    @EventHandler(priority = EventPriority.MONITOR)
    public void login(PlayerLoginEvent event) {
        userService.create(new User(event.getPlayer().getName(), event.getPlayer().getUniqueId()));
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void join(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        User user = userService.get(player.getUniqueId());
        Locations lobby = locationManager.get(LocationType.LOBBY, null);

        player.getInventory().clear();
        player.getInventory().setArmorContents(null);
        player.getActivePotionEffects().forEach(potionEffect -> player.removePotionEffect(potionEffect.getType()));
        player.setGameMode(GameMode.SURVIVAL);
        player.setFoodLevel(20);
        player.setHealth(20);

        if (lobby != null) {
            player.teleport(lobby.getLocation());
        }
        if (user.getBedScore() == null) {
            user.setup();
            user.getBedScore().create();
        }
        LothPlayer lothPlayer = Core.getPlayerController().get(player.getUniqueId());

        if (lothPlayer.isFake()) {
            TagManager.setTag(player, lothPlayer.getSocial().getFake().getRank());
        } else {
            TagManager.setTag(player, lothPlayer.getGroup().getTag());
        }
        TagManager.update(player);

        userService.all().forEach(u -> u.getBedScore().update());
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void leave(PlayerQuitEvent event) {
        Arena arena = gameManager.getArena();
        User user = userService.get(event.getPlayer().getUniqueId());

        if (user != null) {
            if (arena.getGameInfo().getState() == GameState.EM_JOGO || arena.getGameInfo().getState() == GameState.ENCERRANDO) {
                user.update();
            }
        }
        if (arena.getGameInfo().getState() != GameState.EM_JOGO) {
            userService.remove(event.getPlayer().getUniqueId());
        }
        userService.all().forEach(u -> u.getBedScore().update());
    }

}
