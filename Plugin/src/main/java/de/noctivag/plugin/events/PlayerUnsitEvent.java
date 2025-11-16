package de.noctivag.plugin.events;

import org.bukkit.entity.Player;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a player stands up from sitting.
 * This event is not cancellable.
 */
public class PlayerUnsitEvent extends PlayerEvent {
    private static final HandlerList handlers = new HandlerList();

    public PlayerUnsitEvent(@NotNull Player player) {
        super(player);
    }

    @NotNull
    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    @NotNull
    public static HandlerList getHandlerList() {
        return handlers;
    }
}
