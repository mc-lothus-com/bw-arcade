package com.lothus.engines.bedwars;

import com.lothus.bukkit.commands.loader.BukkitCommandLoader;
import com.lothus.core.api.loaders.ListenerLoader;
import com.lothus.core.games.type.GameType;
import com.lothus.engines.bedwars.event.server.TimeTickEvent;
import com.lothus.engines.bedwars.game.GameManager;
import com.lothus.engines.bedwars.service.team.TeamService;
import com.lothus.engines.bedwars.service.user.UserService;
import com.lothus.engines.bedwars.service.team.impl.TeamServiceImpl;
import com.lothus.engines.bedwars.service.user.impl.UserServiceImpl;
import com.lothus.engines.bedwars.service.Services;
import com.lothus.sync.stats.Sync;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.io.FileUtils;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Entity;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitRunnable;

import java.io.File;
import java.io.IOException;

@Getter
public final class Bedwars extends JavaPlugin {

    @Setter
    @Getter
    private static Bedwars instance;

    private GameManager gameManager;

    @Override
    public void onLoad() {
        setInstance(this);

        File file = new File(getDataFolder().getPath());

        if (file.exists()) {
            try {
                FileUtils.deleteDirectory(new File("world"));
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
            paste("world", "arena");
        }
        saveDefaultConfig();

        Services.create(this);
        Services.add(UserService.class, new UserServiceImpl());
        Services.add(TeamService.class, new TeamServiceImpl());
    }

    @Override
    public void onEnable() {
        new Sync(this, GameType.BED_WARS);
        gameManager = new GameManager();
        gameManager.enable();

        World world = Bukkit.getWorld("world");

        world.getEntities().forEach(Entity::remove);
        world.setGameRuleValue("doDaylightCycle", "false");
        world.setGameRuleValue("doMobSpawning", "false");
        world.setGameRuleValue("doWeatherCycle", "false");
        world.setGameRuleValue("randomTickSpeed", "0");
        world.setGameRuleValue("announceAdvancements", "false");
        world.setGameRuleValue("showDeathMessages", "false");
        world.setGameRuleValue("doFireTick", "false");
        world.setGameRuleValue("doEntityDrops", "false");
        world.setTime(0);
        world.setAutoSave(false);

        world.getEntities().forEach(Entity::remove);

        new Sync(this);

        new BukkitRunnable() {
            @Override
            public void run() {
                Bukkit.getPluginManager().callEvent(new TimeTickEvent());
            }
        }.runTaskTimer(this, 2L, 2L);

        BukkitCommandLoader.loadCommands(this, "com.lothus.engines.bedwars.commands");
        ListenerLoader.loadListeners(this, "com.lothus.engines.bedwars.com.redelegit.npc.listener");
        ListenerLoader.loadListeners(this, "com.lothus.engines.bedwars.entity.silverfish");
    }

    @Override
    public void onDisable() {
        gameManager.disable();
    }

    private void paste(String world, String folder) {
        try {
            File dir = new File(getDataFolder().getPath() + "/" + folder + "/");
            File to = new File("/home/container/" + world);

            FileUtils.deleteDirectory(to);

            if (!to.exists()) {
                to.mkdirs();
            }
            FileUtils.copyDirectory(dir, to);
        } catch (Exception ex) {
            ex.printStackTrace();
        }   
    }

}
