package de.noctivag.plugin.managers;

import de.noctivag.plugin.events.PlayerSitEvent;
import de.noctivag.plugin.events.PlayerUnsitEvent;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.Sound;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;

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

        // Base at top of the block the player stands on (centered)
        Location playerLocation = player.getLocation();
        Location base = playerLocation.getBlock().getLocation().add(0.5, 1.2, 0.5);

        // Configurable offset (vertical). Positive raises the seat, negative lowers it.
        // Raise default by another +0.2 blocks as requested.
        double yOffset = 0.2;
        if (plugin instanceof JavaPlugin jp) {
            yOffset = jp.getConfig().getDouble("modules.cosmetics.sit.y-offset", yOffset);
        }

        Location seatLocation = base.add(0.0, yOffset, 0.0);

        ArmorStand armorStand = (ArmorStand) player.getWorld().spawnEntity(seatLocation, EntityType.ARMOR_STAND);
        armorStand.setVisible(false);
        armorStand.setInvisible(true);
        armorStand.setGravity(false);
        armorStand.setInvulnerable(true);
        armorStand.setMarker(true);
        armorStand.setSmall(true);
        armorStand.setCollidable(false);
        armorStand.setCanPickupItems(false);
        armorStand.setRotation(playerLocation.getYaw(), 0.0f);
        
        // Tag with PDC for other plugin compatibility (modern API)
        NamespacedKey seatKey = new NamespacedKey(plugin, "sit_seat_owner");
        armorStand.getPersistentDataContainer().set(seatKey, PersistentDataType.STRING, player.getUniqueId().toString());
        armorStand.customName(net.kyori.adventure.text.Component.text("BasicPlugin_Seat"));
        armorStand.setCustomNameVisible(false);
        
        // Fire custom event for other plugins
        PlayerSitEvent sitEvent = new PlayerSitEvent(player, armorStand);
        Bukkit.getPluginManager().callEvent(sitEvent);
        
        if (sitEvent.isCancelled()) {
            armorStand.remove();
            return false;
        }
        
        armorStand.addPassenger(player);

        sittingPlayers.put(player.getUniqueId(), armorStand);

        // Optional sound feedback on sit
        playConfiguredSound(player, true);
        return true;
    }

    /**
     * Lässt einen Spieler aufstehen
     * @param player Der Spieler
     * @return true wenn erfolgreich, false wenn der Spieler nicht saß
     */
    public boolean unsitPlayer(Player player) {
        ArmorStand armorStand = sittingPlayers.remove(player.getUniqueId());
        if (armorStand != null && !armorStand.isDead()) {
            // Fire unsit event for other plugins
            Bukkit.getPluginManager().callEvent(new PlayerUnsitEvent(player));

            // Get the location of the seat itself
            Location dismountLocation = armorStand.getLocation().clone();
            dismountLocation.setPitch(player.getLocation().getPitch());
            dismountLocation.setYaw(player.getLocation().getYaw());

            // Remove passenger before stand removal for clean dismount
            if (!armorStand.getPassengers().isEmpty()) {
                armorStand.removePassenger(player);
            }

            // Remove ArmorStand
            armorStand.remove();

            // Teleport player to the seat's location if they are still in a vehicle
            if (player.isOnline() && player.isInsideVehicle()) {
                player.leaveVehicle();
            }

            // Teleport player safely to the dismount location
            if (player.isOnline() && dismountLocation.getWorld() != null) {
                Bukkit.getScheduler().runTask(plugin, () -> {
                    player.teleport(dismountLocation);
                    player.setFallDistance(0.0f);
                });
            }

            // Optional sound feedback on stand
            playConfiguredSound(player, false);

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

    @SuppressWarnings({"deprecation", "removal"})
    private void playConfiguredSound(Player player, boolean sitting) {
        if (!(plugin instanceof JavaPlugin jp)) return;
        if (!jp.getConfig().getBoolean("modules.cosmetics.sit.sound.enabled", true)) return;

        String key = sitting ? "modules.cosmetics.sit.sound.sit" : "modules.cosmetics.sit.sound.stand";
        String def = sitting ? "ENTITY_ARMOR_STAND_PLACE" : "ENTITY_ARMOR_STAND_BREAK";
        String soundName = jp.getConfig().getString(key, def);
        float volume = (float) jp.getConfig().getDouble("modules.cosmetics.sit.sound.volume", 0.6D);
        float pitch = (float) jp.getConfig().getDouble("modules.cosmetics.sit.sound.pitch", 1.0D);

        // Paper 1.21+: prefer Sound.valueOf but guard deprecation with fallback
        Sound s = null;
        try {
            s = Sound.valueOf(soundName.toUpperCase());
        } catch (Throwable ignored) { }
        if (s != null) {
            player.playSound(player.getLocation(), s, volume, pitch);
        }
    }
}
