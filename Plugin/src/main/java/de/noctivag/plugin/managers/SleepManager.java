package de.noctivag.plugin.managers;

import de.noctivag.plugin.Plugin;
import de.noctivag.plugin.messages.MessageManager;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerBedEnterEvent;
import org.bukkit.event.player.PlayerBedLeaveEvent;
import org.bukkit.event.player.PlayerQuitEvent;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Manager für das Sleep-System
 * Ermöglicht das Überspringen der Nacht wenn ca. 30% der Spieler schlafen
 */
public class SleepManager implements Listener {
    private final Plugin plugin;
    private final MessageManager messageManager;
    private final Map<UUID, Boolean> sleepingPlayers;
    private double sleepPercentage;

    public SleepManager(Plugin plugin) {
        this.plugin = plugin;
        this.messageManager = plugin.getMessageManager();
        this.sleepingPlayers = new HashMap<>();
        loadConfig();
    }

    /**
     * Lädt die Konfiguration
     */
    public void loadConfig() {
        this.sleepPercentage = plugin.getConfigManager().getConfig().getDouble("sleep.percentage", 0.3);
        // Stelle sicher, dass der Wert zwischen 0 und 1 liegt
        if (sleepPercentage < 0.0) sleepPercentage = 0.0;
        if (sleepPercentage > 1.0) sleepPercentage = 1.0;
    }

    @EventHandler
    public void onPlayerBedEnter(PlayerBedEnterEvent event) {
        if (event.getBedEnterResult() != PlayerBedEnterEvent.BedEnterResult.OK) {
            return;
        }

        Player player = event.getPlayer();
        World world = player.getWorld();

        // Nur in der Oberwelt und nachts
        if (world.getEnvironment() != World.Environment.NORMAL) {
            return;
        }

        if (!isNight(world)) {
            return;
        }

        sleepingPlayers.put(player.getUniqueId(), true);

        // Berechne Spieler die schlafen müssen
        int playersInWorld = getPlayersInWorld(world);
        int sleepingCount = getSleepingCount(world);
        int requiredSleepers = getRequiredSleepers(playersInWorld);

        if (sleepingCount >= requiredSleepers) {
            // Genug Spieler schlafen - Überspringe die Nacht
            skipNight(world);
        } else {
            // Informiere die Spieler
            String message = messageManager.getMessage("sleep.player-sleeping")
                    .replace("%player%", player.getName())
                    .replace("%sleeping%", String.valueOf(sleepingCount))
                    .replace("%required%", String.valueOf(requiredSleepers))
                    .replace("%total%", String.valueOf(playersInWorld));
            
            broadcastToWorld(world, message);
        }
    }

    @EventHandler
    public void onPlayerBedLeave(PlayerBedLeaveEvent event) {
        sleepingPlayers.remove(event.getPlayer().getUniqueId());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        sleepingPlayers.remove(event.getPlayer().getUniqueId());
    }

    /**
     * Prüft ob es Nacht ist
     */
    private boolean isNight(World world) {
        long time = world.getTime();
        return time >= 12541 && time <= 23458;
    }

    /**
     * Überspringt die Nacht
     */
    private void skipNight(World world) {
        world.setTime(0);
        world.setStorm(false);
        world.setThundering(false);

        String message = messageManager.getMessage("sleep.night-skipped");
        broadcastToWorld(world, message);

        // Räume die schlafenden Spieler auf
        sleepingPlayers.clear();
    }

    /**
     * Zählt die Spieler in einer Welt
     */
    private int getPlayersInWorld(World world) {
        int count = 0;
        for (Player player : world.getPlayers()) {
            // Zähle nur nicht-AFK und nicht-vanish Spieler
            if (!player.isSleepingIgnored()) {
                count++;
            }
        }
        return count;
    }

    /**
     * Zählt die schlafenden Spieler in einer Welt
     */
    private int getSleepingCount(World world) {
        int count = 0;
        for (Player player : world.getPlayers()) {
            if (sleepingPlayers.getOrDefault(player.getUniqueId(), false)) {
                count++;
            }
        }
        return count;
    }

    /**
     * Berechnet wie viele Spieler schlafen müssen
     */
    private int getRequiredSleepers(int totalPlayers) {
        if (totalPlayers == 0) return 0;
        int required = (int) Math.ceil(totalPlayers * sleepPercentage);
        return Math.max(1, required); // Mindestens 1 Spieler
    }

    /**
     * Sendet eine Nachricht an alle Spieler in einer Welt
     */
    private void broadcastToWorld(World world, String message) {
        for (Player player : world.getPlayers()) {
            player.sendMessage(message);
        }
    }

    /**
     * Gibt den Sleep-Prozentsatz zurück
     */
    public double getSleepPercentage() {
        return sleepPercentage;
    }

    /**
     * Setzt den Sleep-Prozentsatz
     */
    public void setSleepPercentage(double percentage) {
        if (percentage < 0.0) percentage = 0.0;
        if (percentage > 1.0) percentage = 1.0;
        this.sleepPercentage = percentage;
    }
}
