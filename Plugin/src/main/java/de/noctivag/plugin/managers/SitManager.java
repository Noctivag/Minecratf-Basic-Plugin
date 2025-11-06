package de.noctivag.plugin.managers;

import org.bukkit.Location;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class SitManager {
    private final Plugin plugin;
    private final Map<UUID, ArmorStand> sittingPlayers;

    public SitManager(Plugin plugin) {
        this.plugin = plugin;
        this.sittingPlayers = new HashMap<>();
    }

    /**
     * Lässt einen Spieler sich hinsetzen
     * @param player Der Spieler
     * @return true wenn erfolgreich, false wenn der Spieler bereits sitzt
     */
    public boolean sitPlayer(Player player) {
        if (isSitting(player)) {
            return false;
        }

        Location loc = player.getLocation().clone();
        
        // Positioniere ArmorStand 0.4 Blöcke höher (tiefer sitzen)
        loc.add(0, 0.4, 0);
        
        ArmorStand armorStand = (ArmorStand) player.getWorld().spawnEntity(loc, EntityType.ARMOR_STAND);
        armorStand.setVisible(false);
        armorStand.setGravity(false);
        armorStand.setInvulnerable(true);
        armorStand.setMarker(true);
        armorStand.setSmall(true);
        armorStand.setCollidable(false);
        armorStand.setCanPickupItems(false);
        armorStand.addPassenger(player);

        sittingPlayers.put(player.getUniqueId(), armorStand);
        return true;
    }

    /**
     * Lässt einen Spieler aufstehen
     * @param player Der Spieler
     * @return true wenn erfolgreich, false wenn der Spieler nicht saß
     */
    public boolean unsitPlayer(Player player) {
        ArmorStand armorStand = sittingPlayers.remove(player.getUniqueId());
        if (armorStand != null) {
            // Speichere Position vor dem Entfernen
            Location standLocation = armorStand.getLocation().clone();
            // Setze Y-Position auf sicheren Boden (leicht über dem ArmorStand)
            standLocation.setY(standLocation.getY() + 0.1);
            
            // Entferne ArmorStand
            armorStand.remove();
            
            // Teleportiere Spieler auf sichere Position
            player.teleport(standLocation);
            
            return true;
        }
        return false;
    }

    /**
     * Prüft ob ein Spieler sitzt
     * @param player Der Spieler
     * @return true wenn der Spieler sitzt
     */
    public boolean isSitting(Player player) {
        return sittingPlayers.containsKey(player.getUniqueId());
    }

    /**
     * Entfernt alle sitzenden Spieler (z.B. beim Plugin-Disable)
     */
    public void removeAllSeats() {
        for (ArmorStand armorStand : sittingPlayers.values()) {
            if (armorStand != null && !armorStand.isDead()) {
                armorStand.remove();
            }
        }
        sittingPlayers.clear();
    }

    /**
     * Entfernt den Sitz eines Spielers beim Verlassen des Servers
     * @param playerUUID Die UUID des Spielers
     */
    public void removePlayerSeat(UUID playerUUID) {
        ArmorStand armorStand = sittingPlayers.remove(playerUUID);
        if (armorStand != null && !armorStand.isDead()) {
            armorStand.remove();
        }
    }
}
