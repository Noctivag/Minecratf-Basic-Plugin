package de.noctivag.plugin.listeners;

import de.noctivag.plugin.Plugin;
import de.noctivag.plugin.utils.ColorUtils;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;

/**
 * Handles chat formatting with prefixes, suffixes, and nicknames
 */
public class ChatListener implements Listener {
    private final Plugin plugin;

    public ChatListener(Plugin plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onChat(AsyncChatEvent event) {
        // Check if chat formatting is enabled
        if (!plugin.getConfig().getBoolean("chat.enabled", true)) {
            return;
        }

        Player player = event.getPlayer();

        // Get player data
        String prefix = plugin.getPlayerDataManager().getPrefix(player.getName());
        String suffix = plugin.getPlayerDataManager().getSuffix(player.getName());
        String nickname = plugin.getPlayerDataManager().getNickname(player.getName());

        // Check LuckPerms integration
        if (plugin.getLuckPermsHook() != null && plugin.getLuckPermsHook().shouldSyncDisplayNames()) {
            String lpPrefix = plugin.getLuckPermsHook().getPrefix(player);
            String lpSuffix = plugin.getLuckPermsHook().getSuffix(player);

            if (lpPrefix != null) prefix = lpPrefix;
            if (lpSuffix != null) suffix = lpSuffix;
        }

        // Use nickname or real name
        boolean useDisplayName = plugin.getConfig().getBoolean("chat.use-display-name", true);
        String playerName = (useDisplayName && nickname != null) ? nickname : player.getName();

        // Get chat format
        String format = plugin.getConfig().getString("chat.format", "%prefix%%player%%suffix%&7: &f%message%");

        // Replace placeholders
        format = format.replace("%prefix%", prefix != null ? prefix + " " : "");
        format = format.replace("%suffix%", suffix != null ? " " + suffix : "");
        format = format.replace("%player%", playerName);

        // Get message component
        Component message = event.message();

        // Check if player has permission to use colors in chat
        String colorPermission = plugin.getConfig().getString("chat.color-permission", "plugin.chat.color");
        if (!player.hasPermission(colorPermission)) {
            // Strip colors from message if no permission
            message = event.originalMessage();
        }

        // Build final message by replacing %message% placeholder
        // This is a simplified approach - for full formatting, we'd need to parse the entire format
        String formatString = format.replace("%message%", "");
        Component finalFormat = ColorUtils.parseColor(formatString);
        Component finalMessage = finalFormat.append(message);

        // Set the renderer to display our custom format
        event.renderer((source, sourceDisplayName, messageComponent, viewer) -> finalMessage);
    }
}
