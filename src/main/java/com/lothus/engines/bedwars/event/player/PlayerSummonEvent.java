package com.lothus.engines.bedwars.event.player;

import com.lothus.engines.bedwars.user.User;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.bukkit.Location;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

@Getter
@RequiredArgsConstructor
public class PlayerSummonEvent extends Event {

    private static final HandlerList handlerList = new HandlerList();

    private final Player player;
    private final User user;
    private final EntityType entityType;
    private final Location targetLocation;

    @Override
    public HandlerList getHandlers() {
        return handlerList;
    }

    public static HandlerList getHandlerList() {
        return handlerList;
    }

}
