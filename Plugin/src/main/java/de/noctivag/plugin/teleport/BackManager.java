package de.noctivag.plugin.teleport;

import de.noctivag.plugin.Plugin;
import org.bukkit.Location;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manages /back command - returns players to their last location
 */
public class BackManager {
    private final Plugin plugin;
    private final Map<UUID, Location> lastLocations = new HashMap<>();

    public BackManager(Plugin plugin) {
        this.plugin = plugin;
    }

    public void saveLocation(Player player) {
        if (!plugin.getConfig().getBoolean("modules.teleportation.back.enabled", true)) {
            return;
        }
        lastLocations.put(player.getUniqueId(), player.getLocation().clone());
    }

    public Location getLastLocation(Player player) {
        return lastLocations.get(player.getUniqueId());
    }

    public boolean hasLastLocation(Player player) {
        return lastLocations.containsKey(player.getUniqueId());
    }

    public void clearLastLocation(Player player) {
        lastLocations.remove(player.getUniqueId());
    }

    public void teleportBack(Player player) {
        Location lastLoc = getLastLocation(player);
        if (lastLoc == null) {
            player.sendMessage("§cYou don't have a previous location!");
            return;
        }

        // Save current location before teleporting
        Location currentLoc = player.getLocation().clone();
        player.teleport(lastLoc);
        
        // Update last location to current (so they can toggle back and forth)
        saveLocation(player);
        lastLocations.put(player.getUniqueId(), currentLoc);
        
        player.sendMessage("§aTeleported to your previous location!");
    }
}
