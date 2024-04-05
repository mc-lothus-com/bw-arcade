package com.lothus.engines.bedwars.task;

import com.lothus.core.games.room.RoomType;
import com.lothus.engines.bedwars.Bedwars;
import com.lothus.engines.bedwars.arena.Arena;
import com.lothus.engines.bedwars.user.User;
import com.lothus.sync.stats.data.type.DataType;
import com.lothus.sync.stats.platform.Platform;
import com.lothus.sync.stats.player.games.bedwars.BedPlayer;
import com.lothus.sync.stats.player.games.bedwars.stats.BedStats;
import org.bukkit.Bukkit;

import java.util.concurrent.TimeUnit;

public class ReJoinTask implements Runnable {

    private final Arena arena;
    private final User user;

    public ReJoinTask(User user, Arena arena, int rejoin) {
        this.user = user;
        this.arena = arena;

        Bukkit.getScheduler().runTaskLater(Bedwars.getInstance(), this, (rejoin * 20L));
    }

    @Override
    public void run() {
        if (user.getPlayer() == null) {
            Bukkit.broadcastMessage(user.getTeam().getTeamColor().getColoredName().substring(0, 2) + user.getName() + " §7desconectou e não retornou." + (user.getTeam().isBedBroken() ? " §b§lELIMINADO!" : ""));

            RoomType roomType = arena.getGameInfo().getRoomType();

            if (roomType == RoomType.SOLO) {
                user.getTeam().breakBed(null);
            }
            user.setSpectator(true);
            user.getTeam().eliminateTeam();

            Bedwars.getInstance().getGameManager().checkWinner();

            Platform.getBedPlatform().getBedPlayerController().load(user.getUuid());

            DataType dataType = roomType == RoomType.SOLO ? DataType.BED_WARS_SOLO : (roomType == RoomType.DUPLAS ? DataType.BED_WARS_TEAM :
                    (roomType == RoomType.TRIOS ? DataType.BED_WARS_TRIO : roomType == RoomType.RANQUEADO ? DataType.BED_WARS_RANKED : DataType.BED_WARS_QUARTETO));
            BedStats bedStats = Platform.getBedPlatform().getBedPlayerController().get(dataType, user.getUuid());
            BedPlayer bedPlayer = Platform.getDataPlayer().getBed(dataType, user.getUuid());

            bedStats.setCurrentWinstreak((user.isWinner() ? (bedStats.getCurrentWinstreak() + 1) : 0));
            bedStats.setKills((user.getKills() + bedStats.getKills()));
            bedStats.setFinalKills((user.getFinalKills() + bedStats.getFinalKills()));
            bedStats.setDestroyedBeds((user.getBrokenBeds() + bedStats.getDestroyedBeds()));
            bedStats.setGames((bedStats.getGames() + 1));
            bedStats.setWins((user.isWinner() ? (bedStats.getWins() + 1) : bedStats.getWins()));
            bedStats.setLoses((user.isWinner() ? bedStats.getLossBeds() : (bedStats.getLoses() + 1)));
            bedStats.setFinalDeaths((user.isWinner() ? bedStats.getFinalDeaths() : (bedStats.getFinalDeaths() + 1)));
            bedStats.setLossBeds((user.getTeam().isBedBroken() ? (bedStats.getLossBeds() + 1) : bedStats.getLossBeds()));

            if (bedStats.getBestWinstreak() < bedStats.getCurrentWinstreak()) {
                bedStats.setBestWinstreak(bedStats.getCurrentWinstreak());
            }
            bedPlayer.setCoins((bedPlayer.getCoins() + user.getCoins()));

            Platform.getDataStats().update(dataType, bedStats);
            Platform.getDataPlayer().update(dataType, bedPlayer);
            Platform.getBedPlatform().getBedPlayerController().unload(user.getUuid());
        }
    }
}
