package de.noctivag.plugin.api;

import de.noctivag.plugin.Plugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Public API for other plugins to interact with BasicPlugin features.
 * 
 * Usage example:
 * <pre>
 * BasicPluginAPI api = BasicPluginAPI.getInstance();
 * if (api != null) {
 *     api.makeSit(player);
 * }
 * </pre>
 */
public class BasicPluginAPI {
    private static BasicPluginAPI instance;
    private final Plugin plugin;

    private BasicPluginAPI(Plugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Initialize the API. Called internally by the plugin.
     * @param plugin The main plugin instance
     */
    public static void init(Plugin plugin) {
        instance = new BasicPluginAPI(plugin);
    }

    /**
     * Get the API instance. Returns null if plugin is not loaded.
     * @return The API instance or null
     */
    @Nullable
    public static BasicPluginAPI getInstance() {
        if (instance == null) {
            org.bukkit.plugin.Plugin pluginInstance = Bukkit.getPluginManager().getPlugin("BasicPlugin");
            if (pluginInstance instanceof Plugin basicPlugin) {
                instance = new BasicPluginAPI(basicPlugin);
            }
        }
        return instance;
    }

    /**
     * Make a player sit at their current location.
     * @param player The player to sit
     * @return true if successful, false if player is already sitting or sit feature is disabled
     */
    public boolean makeSit(@NotNull Player player) {
        if (plugin.getSitManager() != null) {
            return plugin.getSitManager().sitPlayer(player);
        }
        return false;
    }

    /**
     * Make a player stand up if they are sitting.
     * @param player The player to unsit
     * @return true if successful, false if player was not sitting
     */
    public boolean makeUnsit(@NotNull Player player) {
        if (plugin.getSitManager() != null) {
            return plugin.getSitManager().unsitPlayer(player);
        }
        return false;
    }

    /**
     * Check if a player is currently sitting.
     * @param player The player to check
     * @return true if player is sitting, false otherwise
     */
    public boolean isSitting(@NotNull Player player) {
        if (plugin.getSitManager() != null) {
            return plugin.getSitManager().isSitting(player);
        }
        return false;
    }

    /**
     * Get the plugin version.
     * @return The plugin version string
     */
    @NotNull
    public String getVersion() {
        return plugin.getPluginMeta().getVersion();
    }

    /**
     * Check if a feature module is enabled.
     * @param modulePath The module path (e.g., "modules.cosmetics.sit")
     * @return true if enabled, false otherwise
     */
    public boolean isModuleEnabled(@NotNull String modulePath) {
        if (plugin.getModuleManager() != null) {
            return plugin.getModuleManager().isModuleEnabled(modulePath);
        }
        return false;
    }
}
