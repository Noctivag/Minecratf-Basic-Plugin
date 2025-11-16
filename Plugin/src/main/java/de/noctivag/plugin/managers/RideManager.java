package de.noctivag.plugin.managers;

import de.noctivag.plugin.Plugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class RideManager {
    
    private final Plugin plugin;
    private final Map<UUID, UUID> ridingPlayers = new HashMap<>(); // rider -> vehicle

    public RideManager(Plugin plugin) {
        this.plugin = plugin;
    }

    public boolean isRiding(Player player) {
        return ridingPlayers.containsKey(player.getUniqueId());
    }

    public boolean startRiding(Player rider, Entity vehicle) {
        if (isRiding(rider)) {
            return false;
        }

        if (vehicle == null || vehicle.isDead()) {
            return false;
        }

        // Prevent riding yourself
        if (vehicle.equals(rider)) {
            return false;
        }

        // Add rider as passenger
        vehicle.addPassenger(rider);
        ridingPlayers.put(rider.getUniqueId(), vehicle.getUniqueId());
        
        return true;
    }

    public void stopRiding(Player rider) {
        UUID riderUuid = rider.getUniqueId();
        
        if (!ridingPlayers.containsKey(riderUuid)) {
            return;
        }

        UUID vehicleUuid = ridingPlayers.remove(riderUuid);
        
        // Find the vehicle entity
        for (Entity entity : rider.getWorld().getEntities()) {
            if (entity.getUniqueId().equals(vehicleUuid)) {
                entity.removePassenger(rider);
                break;
            }
        }
    }

    public void removePlayer(Player player) {
        stopRiding(player);
    }

    public void cleanup() {
        for (UUID riderUuid : ridingPlayers.keySet()) {
            Player rider = Bukkit.getPlayer(riderUuid);
            if (rider != null) {
                stopRiding(rider);
            }
        }
        ridingPlayers.clear();
    }
}
