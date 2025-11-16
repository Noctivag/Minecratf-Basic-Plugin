package de.noctivag.plugin.listeners;

import de.noctivag.plugin.Plugin;
import de.noctivag.plugin.managers.AfkManager;
import de.noctivag.plugin.managers.CrawlManager;
import de.noctivag.plugin.managers.RideManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.player.*;

public class CosmeticListener implements Listener {
    
    private final Plugin plugin;
    private final CrawlManager crawlManager;
    private final AfkManager afkManager;
    private final RideManager rideManager;

    public CosmeticListener(Plugin plugin, CrawlManager crawlManager,
                            AfkManager afkManager, RideManager rideManager) {
        this.plugin = plugin;
        this.crawlManager = crawlManager;
        this.afkManager = afkManager;
        this.rideManager = rideManager;
    }

    // Stop crawling on damage
    @EventHandler
    public void onPlayerDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player)) {
            return;
        }
        
        Player player = (Player) event.getEntity();
        
        if (crawlManager != null && crawlManager.isCrawling(player)) {
            crawlManager.stopCrawling(player);
        }
    }

    // (Lay feature removed; teleport no longer needs special handling)

    // Stop crawling when player jumps
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        
        // Only check if player actually moved (not just head rotation)
        if (event.getFrom().getBlockX() == event.getTo().getBlockX() &&
            event.getFrom().getBlockY() == event.getTo().getBlockY() &&
            event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }
        
        // Remove AFK status when player moves
        if (afkManager != null && afkManager.isAfk(player)) {
            afkManager.setAfk(player, false);
        }
        
        // Restart auto-AFK timer
        if (afkManager != null && plugin.getConfig().getBoolean("modules.cosmetics.afk.auto-afk-enabled", true)) {
            afkManager.startAutoAfkTimer(player);
        }
    }

    // Remove AFK on chat
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        
        if (afkManager != null && afkManager.isAfk(player)) {
            afkManager.setAfk(player, false);
        }
        
        // Restart auto-AFK timer
        if (afkManager != null && plugin.getConfig().getBoolean("modules.cosmetics.afk.auto-afk-enabled", true)) {
            afkManager.startAutoAfkTimer(player);
        }
    }

    // Remove AFK on command
    @EventHandler(priority = EventPriority.MONITOR)
    public void onPlayerCommand(PlayerCommandPreprocessEvent event) {
        Player player = event.getPlayer();
        
        // Don't remove AFK if using /afk command
        if (event.getMessage().toLowerCase().startsWith("/afk")) {
            return;
        }
        
        if (afkManager != null && afkManager.isAfk(player)) {
            afkManager.setAfk(player, false);
        }
        
        // Restart auto-AFK timer
        if (afkManager != null && plugin.getConfig().getBoolean("modules.cosmetics.afk.auto-afk-enabled", true)) {
            afkManager.startAutoAfkTimer(player);
        }
    }

    // Cleanup on player quit
    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        if (crawlManager != null && crawlManager.isCrawling(player)) {
            crawlManager.stopCrawling(player);
        }
        
        
        if (afkManager != null) {
            afkManager.removePlayer(player);
        }
        
        if (rideManager != null) {
            rideManager.removePlayer(player);
        }
    }

    // Start auto-AFK timer on join
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        if (afkManager != null && plugin.getConfig().getBoolean("modules.cosmetics.afk.auto-afk-enabled", true)) {
            afkManager.startAutoAfkTimer(player);
        }
    }
}
