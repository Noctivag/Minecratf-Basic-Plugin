package de.noctivag.plugin;

import de.noctivag.plugin.permissions.Rank;
import de.noctivag.plugin.permissions.RankManager;
import de.noctivag.plugin.utils.ColorUtils;
import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class PrefixListener implements Listener {
    private final HashMap<String, String> prefixMap;
    private final HashMap<String, String> nickMap;
    private final Plugin plugin;

    public PrefixListener(Plugin plugin, HashMap<String, String> prefixMap, HashMap<String, String> nickMap) {
        this.plugin = plugin;
        this.prefixMap = prefixMap;
        this.nickMap = nickMap;
    }

    @EventHandler
    public void onPlayerChat(@NotNull AsyncChatEvent event) {
        String playerName = event.getPlayer().getName();
        String customPrefix = prefixMap.getOrDefault(playerName, "");
        String nick = nickMap.getOrDefault(playerName, event.getPlayer().getName());

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
                .append(Component.text(": "))
                .append(message)
        ));
    }
}
