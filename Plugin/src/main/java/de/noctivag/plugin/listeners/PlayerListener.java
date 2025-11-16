package de.noctivag.plugin.listeners;

import de.noctivag.plugin.Plugin;
import de.noctivag.plugin.config.ConfigManager;
import de.noctivag.plugin.utils.ColorUtils;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.event.entity.EntityDismountEvent;
import org.bukkit.persistence.PersistentDataType;

public class PlayerListener implements Listener {
    private final Plugin plugin;
    private final ConfigManager config;

    public PlayerListener(Plugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfigManager();
    }

    @EventHandler(priority = org.bukkit.event.EventPriority.NORMAL)
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Hole die Join-Message aus der Config
        String joinMessage = config.getConfig().getString("join-messages.default-message", "&7[&a+&7] &e%player% &7hat den Server betreten");
        joinMessage = joinMessage.replace("%player%", player.getName());
        
        // Konvertiere Minecraft-Farbcodes (&) zu Legacy-Format
        joinMessage = joinMessage.replace('&', '§');
        
        // Setze die Join-Message als Legacy-Text-Component
        event.joinMessage(net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().deserialize(joinMessage));

        // Lade und setze gespeicherte Prefix/Suffix/Nick Daten
        if (plugin.getPlayerDataManager() != null && plugin.getNametagManager() != null) {
            plugin.getNametagManager().loadNametag(player);
        }

        // Debug-Modus Info
        if (config.isDebugMode() && player.hasPermission("plugin.debug")) {
            player.sendMessage("§7[Debug] Spielerdaten geladen");
        }
    }

    @EventHandler(priority = org.bukkit.event.EventPriority.MONITOR)
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        
        // Hole die Quit-Message aus der Config (falls vorhanden)
        String quitMessage = config.getConfig().getString("quit-messages.default-message", "&7[&c-&7] &e%player% &7hat den Server verlassen");
        quitMessage = quitMessage.replace("%player%", player.getName());
        
        // Konvertiere Minecraft-Farbcodes (&) zu Legacy-Format
        quitMessage = quitMessage.replace('&', '§');
        
        // Setze die Quit-Message als Legacy-Text-Component
        event.quitMessage(net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacySection().deserialize(quitMessage));

        // Entferne Sitz-ArmorStand wenn vorhanden
        if (plugin.getSitManager() != null) {
            plugin.getSitManager().removePlayerSeat(player.getUniqueId());
        }
        
        // Entferne Kamera-Modus wenn aktiv
        if (plugin.getTriggerCamCommand() != null) {
            plugin.getTriggerCamCommand().removePlayer(player.getUniqueId());
        }
        
        // Cleanup Nametag
        if (plugin.getNametagManager() != null) {
            plugin.getNametagManager().cleanup(player);
        }
    }

    @EventHandler(priority = org.bukkit.event.EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDismount(EntityDismountEvent event) {
        // Wenn ein Spieler von einem ArmorStand absteigt, entferne den ArmorStand
        if (event.getEntity() instanceof Player player) {
            // Check if dismounting from our seat (via PDC)
            NamespacedKey seatKey = new NamespacedKey(plugin, "sit_seat_owner");
            if (event.getDismounted().getPersistentDataContainer().has(seatKey, PersistentDataType.STRING)) {
                if (plugin.getSitManager() != null && plugin.getSitManager().isSitting(player)) {
                    plugin.getSitManager().unsitPlayer(player);
                }
            }
        }
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (config.isDebugMode()) {
            Player player = event.getPlayer();
            plugin.getLogger().info(String.format(
                "Block broken by %s at %s",
                player.getName(),
                event.getBlock().getLocation()
            ));
        }
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity().getKiller() != null) {
            Player killer = event.getEntity().getKiller();
            // Hier können Statistiken oder Belohnungen verarbeitet werden
            if (config.isDebugMode()) {
                plugin.getLogger().info(String.format(
                    "%s killed %s",
                    killer.getName(),
                    event.getEntity().getType()
                ));
            }
        }
    }
}
