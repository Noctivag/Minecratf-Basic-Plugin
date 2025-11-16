package de.noctivag.plugin;

import de.noctivag.plugin.permissions.Rank;
import de.noctivag.plugin.permissions.RankManager;
import de.noctivag.plugin.data.PlayerDataManager;
import de.noctivag.plugin.utils.ColorUtils;
import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;
import de.noctivag.plugin.messages.MessageManager;

public class PrefixListener implements Listener {
    private final Plugin plugin;
    private final PlayerDataManager playerDataManager;
    private final MessageManager messageManager;

    public PrefixListener(Plugin plugin, PlayerDataManager playerDataManager) {
        this.plugin = plugin;
        this.playerDataManager = playerDataManager;
        this.messageManager = plugin.getMessageManager();
    }

    @EventHandler
    public void onPlayerChat(@NotNull AsyncChatEvent event) {
        String playerUuid = event.getPlayer().getUniqueId().toString();
        String storedPrefix = null;
        String storedNick = null;

        if (playerDataManager != null) {
            storedPrefix = playerDataManager.getPrefix(playerUuid);
            storedNick = playerDataManager.getNickname(playerUuid);
        }

        final String customPrefix = storedPrefix != null ? storedPrefix : "";
        final String nick = (storedNick != null && !storedNick.isEmpty()) ? storedNick : event.getPlayer().getName();

        // Get rank prefix if available
        String rankPrefix = "";
        RankManager rankManager = plugin.getRankManager();
        if (rankManager != null) {
            Rank rank = rankManager.getHighestRank(event.getPlayer().getUniqueId());
            if (rank != null) {
                rankPrefix = rank.getPrefix();
            }
        }

        // Combine rank prefix with custom prefix
        String finalPrefix = ColorUtils.combinePrefix(rankPrefix, customPrefix);

        event.renderer(ChatRenderer.viewerUnaware((source, sourceDisplayName, message) ->
            Component.empty()
                .append(ColorUtils.parseColor(finalPrefix))
                .append(Component.space())
                .append(ColorUtils.parseColor(nick))
                .append(messageManager.getMessage("chat.separator"))
                .append(message)
        ));
    }
}
