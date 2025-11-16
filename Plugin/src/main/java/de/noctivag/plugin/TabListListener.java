package de.noctivag.plugin;

import de.noctivag.plugin.data.PlayerDataManager;
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

import java.util.Objects;

public class TabListListener implements Listener {
    private final PlayerDataManager playerDataManager;
    private final JoinMessageManager joinMessageManager;
    private final Plugin plugin;
    private final RankManager rankManager;

    public TabListListener(@NotNull Plugin plugin,
                           PlayerDataManager playerDataManager,
                           @NotNull JoinMessageManager joinMessageManager) {
        this.plugin = Objects.requireNonNull(plugin, "Plugin cannot be null");
        this.playerDataManager = playerDataManager;
        this.joinMessageManager = Objects.requireNonNull(joinMessageManager, "JoinMessageManager cannot be null");
        this.rankManager = plugin.getRankManager();
        if (this.rankManager == null) {
            plugin.getLogger().warning("RankManager is not initialized!");
        }
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onPlayerJoin(@NotNull PlayerJoinEvent event) {
        Player player = event.getPlayer();
        
        // Delay by one tick to ensure permissions are loaded
        plugin.getServer().getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) return;

            try {
                updatePlayerDisplay(player);

                // Setze die Join-Nachricht
                Component joinMessage = joinMessageManager.getJoinMessage(player.getUniqueId().toString());
                if (joinMessage != null) {
                    event.joinMessage(joinMessage);
                }
            } catch (Exception e) {
                plugin.getLogger().severe("Fehler im TabListListener (delayed task): " + e.getMessage());
                if (plugin.getConfigManager().isDebugMode()) {
                    e.printStackTrace();
                }
            }
        }, 1L);
    }

    /**
     * Aktualisiert den Tab-Listen-Eintrag für einen Spieler
     *
     * @param player Der Spieler, dessen Anzeige aktualisiert werden soll
     */
    public void updatePlayerDisplay(@NotNull Player player) {
        try {
            String playerUuid = player.getUniqueId().toString();
            String customPrefix = "";
            String nick = player.getName();

            if (playerDataManager != null) {
                String storedPrefix = playerDataManager.getPrefix(playerUuid);
                String storedNick = playerDataManager.getNickname(playerUuid);
                if (storedPrefix != null) {
                    customPrefix = storedPrefix;
                }
                if (storedNick != null && !storedNick.isEmpty()) {
                    nick = storedNick;
                }
            }

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
                    plugin.getLogger().warning("Fehler beim Aktualisieren des Displaynamens für " + player.getName());
                }
            });
        } catch (Exception e) {
            plugin.getLogger().severe("Fehler beim Aktualisieren der Spieleranzeige: " + e.getMessage());
        }
    }
}
