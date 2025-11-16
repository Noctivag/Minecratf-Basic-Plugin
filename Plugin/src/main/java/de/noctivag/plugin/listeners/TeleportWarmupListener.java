package de.noctivag.plugin.listeners;

import de.noctivag.plugin.managers.TeleportWarmupManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.UUID;

public class TeleportWarmupListener implements Listener {
    private final TeleportWarmupManager warmupManager;

    public TeleportWarmupListener(TeleportWarmupManager warmupManager) {
        this.warmupManager = warmupManager;
    }

    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (!warmupManager.isWarmingUp(player.getUniqueId())) return;
        // Handle cancellation if moved
        warmupManager.handleMove(player, event.getFrom(), event.getTo());
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        UUID id = event.getPlayer().getUniqueId();
        warmupManager.handleQuit(id);
    }
}
