package de.noctivag.plugin;

import de.noctivag.plugin.permissions.Rank;
import de.noctivag.plugin.permissions.RankManager;
import de.noctivag.plugin.utils.ColorUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

public class TabListListener implements Listener {
    private final Map<String, String> prefixMap;
    private final Map<String, String> nickMap;
    private final JoinMessageManager joinMessageManager;
    private final Plugin plugin;
    private final RankManager rankManager;

    public TabListListener(@NotNull Plugin plugin, @NotNull Map<String, String> prefixMap,
                           @NotNull Map<String, String> nickMap, @NotNull JoinMessageManager joinMessageManager) {
        this.plugin = Objects.requireNonNull(plugin, "Plugin cannot be null");
        this.prefixMap = new ConcurrentHashMap<>(prefixMap);
        this.nickMap = new ConcurrentHashMap<>(nickMap);
        this.joinMessageManager = Objects.requireNonNull(joinMessageManager, "JoinMessageManager cannot be null");
        this.rankManager = plugin.getRankManager();
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(@NotNull PlayerJoinEvent event) {
        try {
            Player player = event.getPlayer();
            if (player == null) return;

            String playerName = player.getName();
            String customPrefix = prefixMap.getOrDefault(playerName, "");
            String nick = nickMap.getOrDefault(playerName, playerName);

            // Get rank prefix if available
            String rankPrefix = "";
            if (rankManager != null) {
                Rank rank = rankManager.getHighestRank(player.getUniqueId());
                if (rank != null) {
                    rankPrefix = rank.getPrefix();
                }
            }

            // Combine rank prefix with custom prefix
            String finalPrefix = ColorUtils.combinePrefix(rankPrefix, customPrefix);

            // Erstelle den DisplayName nur einmal
            Component displayName = Component.empty()
                    .append(ColorUtils.parseColor(finalPrefix))
                    .append(Component.space())
                    .append(ColorUtils.parseColor(nick));

            // Setze den DisplayName und TabList-Namen synchron
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                try {
                    if (player.isOnline()) {
                        player.displayName(displayName);
                        player.playerListName(displayName);
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("Fehler beim Setzen des Displaynamens für " + playerName);
                }
            });

            // Setze die Join-Nachricht
            Component joinMessage = joinMessageManager.getJoinMessage(playerName);
            if (joinMessage != null) {
                event.joinMessage(joinMessage);
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Fehler im TabListListener: " + e.getMessage());
            if (plugin.getConfigManager().isDebugMode()) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Aktualisiert den Tab-Listen-Eintrag für einen Spieler
     *
     * @param player Der Spieler, dessen Anzeige aktualisiert werden soll
     */
    public void updatePlayerDisplay(@NotNull Player player) {
        try {
            String playerName = player.getName();
            String customPrefix = prefixMap.getOrDefault(playerName, "");
            String nick = nickMap.getOrDefault(playerName, playerName);

            // Get rank prefix if available
            String rankPrefix = "";
            if (rankManager != null) {
                Rank rank = rankManager.getHighestRank(player.getUniqueId());
                if (rank != null) {
                    rankPrefix = rank.getPrefix();
                }
            }

            // Combine rank prefix with custom prefix
            String finalPrefix = ColorUtils.combinePrefix(rankPrefix, customPrefix);

            Component displayName = Component.empty()
                    .append(ColorUtils.parseColor(finalPrefix))
                    .append(Component.space())
                    .append(ColorUtils.parseColor(nick));

            plugin.getServer().getScheduler().runTask(plugin, () -> {
                try {
                    if (player.isOnline()) {
                        player.displayName(displayName);
                        player.playerListName(displayName);
                    }
                } catch (Exception e) {
                    plugin.getLogger().warning("Fehler beim Aktualisieren des Displaynamens für " + playerName);
                }
            });
        } catch (Exception e) {
            plugin.getLogger().severe("Fehler beim Aktualisieren der Spieleranzeige: " + e.getMessage());
        }
    }
}
