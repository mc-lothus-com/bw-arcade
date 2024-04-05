package com.lothus.engines.bedwars.utils;

import com.lothus.core.Core;
import com.lothus.core.servers.ServerInfo;
import com.lothus.core.servers.type.ServerType;
import org.bukkit.entity.Player;

import java.util.Comparator;
import java.util.List;

public class PlayerUtil {

    public static void send(Player player, ServerType type) {
        Comparator<ServerInfo> comparator = Comparator.comparing(ServerInfo::getPlayers);
        List<ServerInfo> list = Core.getServerController().get(type);
        list.sort(comparator);

        if (list.isEmpty()) {
            player.sendMessage("§cNão foi possível estabelecer conexão com o servidor.");
            return;
        }
        ServerInfo serverInfo = list.get(0);

        if (serverInfo == null) {
            player.sendMessage("§cNão foi possível estabelecer conexão com o servidor.");
            return;
        }

        com.lothus.core.utils.bukkit.player.PlayerUtil.connect(player.getUniqueId(), serverInfo);
    }
}
