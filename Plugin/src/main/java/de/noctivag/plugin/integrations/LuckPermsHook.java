package de.noctivag.plugin.integrations;

import de.noctivag.plugin.Plugin;
import net.luckperms.api.LuckPerms;
import net.luckperms.api.LuckPermsProvider;
import net.luckperms.api.model.user.User;
import net.luckperms.api.cacheddata.CachedMetaData;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Integration hook for LuckPerms
 * Provides prefix/suffix and permission support through LuckPerms
 */
public class LuckPermsHook {
    private final Plugin plugin;
    private LuckPerms luckPerms;
    private boolean enabled;
    private boolean useForPermissions;
    private boolean syncDisplayNames;
    private boolean primaryGroupOnly;

    public LuckPermsHook(Plugin plugin) {
        this.plugin = plugin;
        this.enabled = false;
    }

    /**
     * Initialize LuckPerms hook
     * @return true if successfully hooked, false otherwise
     */
    public boolean hook() {
        try {
            // Check if LuckPerms integration is enabled in config
            if (!plugin.getConfig().getBoolean("integrations.luckperms.enabled", false)) {
                plugin.getLogger().info("LuckPerms integration is disabled in config");
                return false;
            }

            // Check if LuckPerms is loaded
            if (!plugin.getServer().getPluginManager().isPluginEnabled("LuckPerms")) {
                plugin.getLogger().info("LuckPerms is not installed, integration disabled");
                return false;
            }

            // Get LuckPerms API
            this.luckPerms = LuckPermsProvider.get();

            // Load configuration
            this.useForPermissions = plugin.getConfig().getBoolean("integrations.luckperms.use-for-permissions", true);
            this.syncDisplayNames = plugin.getConfig().getBoolean("integrations.luckperms.sync-display-names", true);
            this.primaryGroupOnly = plugin.getConfig().getBoolean("integrations.luckperms.primary-group-only", true);

            this.enabled = true;
            plugin.getLogger().info("Successfully hooked into LuckPerms!");
            plugin.getLogger().info("  - Use for permissions: " + useForPermissions);
            plugin.getLogger().info("  - Sync display names: " + syncDisplayNames);

            return true;
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to hook into LuckPerms: " + e.getMessage());
            this.enabled = false;
            return false;
        }
    }

    /**
     * Check if LuckPerms is hooked and enabled
     */
    public boolean isEnabled() {
        return enabled && luckPerms != null;
    }

    /**
     * Get player's prefix from LuckPerms
     */
    public String getPrefix(Player player) {
        if (!isEnabled() || !syncDisplayNames) {
            return null;
        }

        try {
            User user = luckPerms.getUserManager().getUser(player.getUniqueId());
            if (user == null) {
                return null;
            }

            CachedMetaData metaData = user.getCachedData().getMetaData();
            return metaData.getPrefix();
        } catch (Exception e) {
            plugin.getLogger().warning("Error getting prefix from LuckPerms for " + player.getName() + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Get player's suffix from LuckPerms
     */
    public String getSuffix(Player player) {
        if (!isEnabled() || !syncDisplayNames) {
            return null;
        }

        try {
            User user = luckPerms.getUserManager().getUser(player.getUniqueId());
            if (user == null) {
                return null;
            }

            CachedMetaData metaData = user.getCachedData().getMetaData();
            return metaData.getSuffix();
        } catch (Exception e) {
            plugin.getLogger().warning("Error getting suffix from LuckPerms for " + player.getName() + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Get player's primary group from LuckPerms
     */
    public String getPrimaryGroup(Player player) {
        if (!isEnabled()) {
            return null;
        }

        try {
            User user = luckPerms.getUserManager().getUser(player.getUniqueId());
            if (user == null) {
                return null;
            }

            return user.getPrimaryGroup();
        } catch (Exception e) {
            plugin.getLogger().warning("Error getting primary group from LuckPerms for " + player.getName() + ": " + e.getMessage());
            return null;
        }
    }

    /**
     * Check if player has permission through LuckPerms
     */
    public boolean hasPermission(UUID uuid, String permission) {
        if (!isEnabled() || !useForPermissions) {
            return false;
        }

        try {
            User user = luckPerms.getUserManager().getUser(uuid);
            if (user == null) {
                return false;
            }

            return user.getCachedData().getPermissionData().checkPermission(permission).asBoolean();
        } catch (Exception e) {
            plugin.getLogger().warning("Error checking permission from LuckPerms: " + e.getMessage());
            return false;
        }
    }

    /**
     * Check if we should use LuckPerms for permissions
     */
    public boolean useForPermissions() {
        return isEnabled() && useForPermissions;
    }

    /**
     * Check if we should sync display names from LuckPerms
     */
    public boolean shouldSyncDisplayNames() {
        return isEnabled() && syncDisplayNames;
    }
}
