package de.noctivag.plugin.listeners;

import de.noctivag.plugin.Plugin;
import de.noctivag.plugin.config.ConfigManager;
import de.noctivag.plugin.utils.ColorUtils;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import net.kyori.adventure.text.Component;

public class PlayerListener implements Listener {
    private final Plugin plugin;
    private final ConfigManager config;

    public PlayerListener(Plugin plugin) {
        this.plugin = plugin;
        this.config = plugin.getConfigManager();
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();
        String joinMessage = config.getMessage("join-message")
            .replace("%player%", player.getName());
        event.joinMessage(ColorUtils.parseColor(joinMessage));

        // Setze gespeicherte Daten
        if (plugin.getPrefixMap().containsKey(player.getName())) {
            updatePlayerDisplay(player);
        }

        // Debug-Modus Info
        if (config.isDebugMode() && player.hasPermission("plugin.debug")) {
            player.sendMessage("§7[Debug] Spielerdaten geladen");
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        String quitMessage = config.getMessage("quit-message")
            .replace("%player%", player.getName());
        event.quitMessage(ColorUtils.parseColor(quitMessage));

        // Speichere Daten beim Verlassen
        plugin.getParticleManager().stopEffect(player);
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

    private void updatePlayerDisplay(Player player) {
        String prefix = plugin.getPrefixMap().getOrDefault(player.getName(), "");
        String nick = plugin.getNickMap().getOrDefault(player.getName(), player.getName());
        Component displayName = Component.empty()
            .append(ColorUtils.parseColor(prefix))
            .append(Component.space())
            .append(ColorUtils.parseColor(nick));
        player.displayName(displayName);
        player.playerListName(displayName);
    }
}
