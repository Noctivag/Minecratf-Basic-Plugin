package de.noctivag.plugin.listeners;

import de.noctivag.plugin.Plugin;
import de.noctivag.plugin.commands.TriggerCamCommand;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;

/**
 * Listener für Kamera-Modus Events
 * Deaktiviert Kamera automatisch wenn der zurückgebliebene Dummy Schaden nimmt
 */
public class CameraListener implements Listener {
    private final Plugin plugin;
    private final TriggerCamCommand camCommand;

    public CameraListener(Plugin plugin, TriggerCamCommand camCommand) {
        this.plugin = plugin;
        this.camCommand = camCommand;
    }

    /**
     * Wenn ein ArmorStand (Kamera-Dummy) Schaden nimmt,
     * deaktiviere den Kamera-Modus für den zugehörigen Spieler
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onDummyDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof ArmorStand armorStand)) {
            return;
        }

        // Prüfe ob dies ein Kamera-Dummy ist (hat CustomName mit Spielernamen)
        String customName = armorStand.getCustomName();
        if (customName == null || !customName.startsWith("§e")) {
            return;
        }

        // Extrahiere Spielernamen (entferne §e Präfix)
        String playerName = customName.substring(2);
        Player player = plugin.getServer().getPlayerExact(playerName);

        if (player != null && player.isOnline()) {
            // Prüfe ob Spieler im Kamera-Modus ist
            if (camCommand.isInCameraMode(player.getUniqueId())) {
                // Deaktiviere Kamera-Modus
                player.performCommand("cam");
                
                // Informiere Spieler
                player.sendMessage("§c§lKAMERA DEAKTIVIERT!");
                player.sendMessage("§7Dein Körper hat Schaden genommen!");
                
                // Cancle Schaden für den Dummy (er ist invulnerable, aber sicher ist sicher)
                event.setCancelled(true);
            }
        }
    }

    /**
     * Verhindere dass der Spieler selbst Schaden nimmt während er unsichtbar ist
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerDamageInCam(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) {
            return;
        }

        // Wenn Spieler im Kamera-Modus ist, verhindere Schaden
        if (camCommand.isInCameraMode(player.getUniqueId())) {
            event.setCancelled(true);
        }
    }
}
