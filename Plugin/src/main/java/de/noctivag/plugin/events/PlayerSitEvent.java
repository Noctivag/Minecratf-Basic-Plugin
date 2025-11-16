package de.noctivag.plugin.events;

import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.Cancellable;
import org.bukkit.event.HandlerList;
import org.bukkit.event.player.PlayerEvent;
import org.jetbrains.annotations.NotNull;

/**
 * Called when a player sits down using the sit feature.
 * Other plugins can listen to this event and cancel it if needed.
 */
public class PlayerSitEvent extends PlayerEvent implements Cancellable {
    private static final HandlerList handlers = new HandlerList();
    private boolean cancelled = false;
    private final ArmorStand seat;

    public PlayerSitEvent(@NotNull Player player, @NotNull ArmorStand seat) {
        super(player);
        this.seat = seat;
    }

    /**
     * Get the armor stand that serves as the seat
     * @return The seat armor stand
     */
    @NotNull
    public ArmorStand getSeat() {
        return seat;
    }

    @Override
    public boolean isCancelled() {
        return cancelled;
    }

    @Override
    public void setCancelled(boolean cancel) {
        this.cancelled = cancel;
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
