package de.noctivag.plugin.integrations;

import de.noctivag.plugin.Plugin;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

/**
 * PlaceholderAPI integration
 * Provides placeholders for other plugins to use
 */
public class PlaceholderAPIHook extends PlaceholderExpansion {
    private final Plugin plugin;
    private boolean enabled;

    public PlaceholderAPIHook(Plugin plugin) {
        this.plugin = plugin;
        this.enabled = false;
    }

    /**
     * Initialize PlaceholderAPI hook
     * @return true if successfully hooked, false otherwise
     */
    public boolean hook() {
        try {
            // Check if PlaceholderAPI integration is enabled in config
            if (!plugin.getConfig().getBoolean("integrations.placeholderapi.enabled", false)) {
                plugin.getLogger().info("PlaceholderAPI integration is disabled in config");
                return false;
            }

            // Check if PlaceholderAPI is loaded
            if (!plugin.getServer().getPluginManager().isPluginEnabled("PlaceholderAPI")) {
                plugin.getLogger().info("PlaceholderAPI is not installed, integration disabled");
                return false;
            }

            // Register expansion
            if (plugin.getConfig().getBoolean("integrations.placeholderapi.register-expansions", true)) {
                if (this.register()) {
                    this.enabled = true;
                    plugin.getLogger().info("Successfully registered PlaceholderAPI expansion!");
                    return true;
                }
            }

            return false;
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to hook into PlaceholderAPI: " + e.getMessage());
            this.enabled = false;
            return false;
        }
    }

    @Override
    public @NotNull String getIdentifier() {
        return "noctivagplugin";
    }

    @Override
    public @NotNull String getAuthor() {
        return plugin.getDescription().getAuthors().toString();
    }

    @Override
    public @NotNull String getVersion() {
        return plugin.getDescription().getVersion();
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String identifier) {
        if (player == null) {
            return "";
        }

        // Player data placeholders
        switch (identifier.toLowerCase()) {
            case "prefix":
                String prefix = plugin.getPlayerDataManager().getPrefix(player.getName());
                return prefix != null ? prefix : "";

            case "suffix":
                String suffix = plugin.getPlayerDataManager().getSuffix(player.getName());
                return suffix != null ? suffix : "";

            case "nickname":
            case "nick":
                String nick = plugin.getPlayerDataManager().getNickname(player.getName());
                return nick != null ? nick : player.getName();

            case "displayname":
                String displayPrefix = plugin.getPlayerDataManager().getPrefix(player.getName());
                String displayNick = plugin.getPlayerDataManager().getNickname(player.getName());
                String displaySuffix = plugin.getPlayerDataManager().getSuffix(player.getName());

                StringBuilder displayName = new StringBuilder();
                if (displayPrefix != null && !displayPrefix.isEmpty()) {
                    displayName.append(displayPrefix);
                }
                displayName.append(displayNick != null ? displayNick : player.getName());
                if (displaySuffix != null && !displaySuffix.isEmpty()) {
                    displayName.append(displaySuffix);
                }
                return displayName.toString();

            case "rank":
                if (plugin.getRankManager() != null) {
                    String rank = plugin.getRankManager().getHighestRank(player.getUniqueId()).getName();
                    return rank != null ? rank : "default";
                }
                return "default";

            case "rank_prefix":
                if (plugin.getRankManager() != null) {
                    String rankPrefix = plugin.getRankManager().getHighestRank(player.getUniqueId()).getPrefix();
                    return rankPrefix != null ? rankPrefix : "";
                }
                return "";

            case "rank_suffix":
                if (plugin.getRankManager() != null) {
                    String rankSuffix = plugin.getRankManager().getHighestRank(player.getUniqueId()).getSuffix();
                    return rankSuffix != null ? rankSuffix : "";
                }
                return "";

            case "homes_count":
                if (plugin.getHomeManager() != null) {
                    return String.valueOf(plugin.getHomeManager().getHomeCount(player.getUniqueId()));
                }
                return "0";

            case "homes_max":
                if (player.hasPermission("essentials.home.vip")) {
                    return String.valueOf(plugin.getConfig().getInt("modules.homes.max-homes-vip", 10));
                }
                return String.valueOf(plugin.getConfig().getInt("modules.homes.max-homes", 5));

            case "vanished":
                if (plugin.getCommand("vanish") != null) {
                    // This would need access to VanishCommand's vanished players
                    return "false"; // Placeholder
                }
                return "false";

            case "flying":
                return String.valueOf(player.isFlying());

            default:
                return null;
        }
    }

    public boolean isEnabled() {
        return enabled;
    }
}
