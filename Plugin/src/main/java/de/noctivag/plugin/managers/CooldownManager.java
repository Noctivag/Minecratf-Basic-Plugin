package de.noctivag.plugin.managers;

import de.noctivag.plugin.Plugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages command cooldowns for players
 * Supports persistent cooldowns across server restarts
 */
public class CooldownManager {
    private final Plugin plugin;
    private final Map<UUID, Map<String, Long>> cooldowns;
    private final File cooldownFile;
    private FileConfiguration cooldownConfig;
    private boolean enabled;
    private boolean persistOnRestart;

    public CooldownManager(Plugin plugin) {
        this.plugin = plugin;
        this.cooldowns = new ConcurrentHashMap<>();
        this.cooldownFile = new File(plugin.getDataFolder(), "cooldowns.yml");

        // Load configuration
        this.enabled = plugin.getConfig().getBoolean("cooldowns.enabled", true);
        this.persistOnRestart = plugin.getConfig().getBoolean("cooldowns.persist-on-restart", true);

        if (enabled && persistOnRestart) {
            loadCooldowns();
        }
    }

    /**
     * Check if a player has a cooldown for a command
     * @param player The player to check
     * @param command The command name
     * @return true if player is on cooldown, false otherwise
     */
    public boolean hasCooldown(Player player, String command) {
        if (!enabled) {
            return false;
        }

        // Check bypass permission
        String bypassPermission = plugin.getConfig().getString("cooldowns.bypass-permission", "plugin.cooldown.bypass");
        if (player.hasPermission(bypassPermission)) {
            return false;
        }

        // Check specific command bypass permission
        String commandBypassPerm = "basiccommands." + command.toLowerCase() + ".nocooldown";
        if (player.hasPermission(commandBypassPerm)) {
            return false;
        }

        UUID uuid = player.getUniqueId();
        if (!cooldowns.containsKey(uuid)) {
            return false;
        }

        Map<String, Long> playerCooldowns = cooldowns.get(uuid);
        if (!playerCooldowns.containsKey(command)) {
            return false;
        }

        long cooldownEnd = playerCooldowns.get(command);
        long currentTime = System.currentTimeMillis();

        if (currentTime >= cooldownEnd) {
            // Cooldown expired, remove it
            playerCooldowns.remove(command);
            if (playerCooldowns.isEmpty()) {
                cooldowns.remove(uuid);
            }
            return false;
        }

        return true;
    }

    /**
     * Get remaining cooldown time in seconds
     * @param player The player
     * @param command The command name
     * @return Remaining seconds, or 0 if no cooldown
     */
    public long getRemainingCooldown(Player player, String command) {
        if (!enabled) {
            return 0;
        }

        UUID uuid = player.getUniqueId();
        if (!cooldowns.containsKey(uuid)) {
            return 0;
        }

        Map<String, Long> playerCooldowns = cooldowns.get(uuid);
        if (!playerCooldowns.containsKey(command)) {
            return 0;
        }

        long cooldownEnd = playerCooldowns.get(command);
        long currentTime = System.currentTimeMillis();
        long remaining = (cooldownEnd - currentTime) / 1000;

        return Math.max(0, remaining);
    }

    /**
     * Set a cooldown for a player
     * @param player The player
     * @param command The command name
     * @param seconds The cooldown duration in seconds
     */
    public void setCooldown(Player player, String command, int seconds) {
        if (!enabled || seconds <= 0) {
            return;
        }

        UUID uuid = player.getUniqueId();
        long cooldownEnd = System.currentTimeMillis() + (seconds * 1000L);

        cooldowns.computeIfAbsent(uuid, k -> new ConcurrentHashMap<>())
                 .put(command, cooldownEnd);
    }

    /**
     * Set a cooldown based on config value
     * @param player The player
     * @param command The command name
     */
    public void setCooldownFromConfig(Player player, String command) {
        if (!enabled) {
            return;
        }

        int cooldownSeconds = plugin.getConfig().getInt("cooldowns.commands." + command.toLowerCase(), 0);
        if (cooldownSeconds > 0) {
            setCooldown(player, command, cooldownSeconds);
        }
    }

    /**
     * Remove a cooldown for a player
     * @param player The player
     * @param command The command name
     */
    public void removeCooldown(Player player, String command) {
        UUID uuid = player.getUniqueId();
        if (cooldowns.containsKey(uuid)) {
            Map<String, Long> playerCooldowns = cooldowns.get(uuid);
            playerCooldowns.remove(command);
            if (playerCooldowns.isEmpty()) {
                cooldowns.remove(uuid);
            }
        }
    }

    /**
     * Clear all cooldowns for a player
     * @param uuid The player's UUID
     */
    public void clearCooldowns(UUID uuid) {
        cooldowns.remove(uuid);
    }

    /**
     * Load cooldowns from file
     */
    private void loadCooldowns() {
        if (!cooldownFile.exists()) {
            return;
        }

        try {
            cooldownConfig = YamlConfiguration.loadConfiguration(cooldownFile);

            for (String uuidString : cooldownConfig.getKeys(false)) {
                try {
                    UUID uuid = UUID.fromString(uuidString);
                    Map<String, Long> playerCooldowns = new HashMap<>();

                    for (String command : cooldownConfig.getConfigurationSection(uuidString).getKeys(false)) {
                        long cooldownEnd = cooldownConfig.getLong(uuidString + "." + command);

                        // Only load if cooldown hasn't expired
                        if (cooldownEnd > System.currentTimeMillis()) {
                            playerCooldowns.put(command, cooldownEnd);
                        }
                    }

                    if (!playerCooldowns.isEmpty()) {
                        cooldowns.put(uuid, new ConcurrentHashMap<>(playerCooldowns));
                    }
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("Invalid UUID in cooldowns.yml: " + uuidString);
                }
            }

            plugin.getLogger().info("Loaded cooldowns for " + cooldowns.size() + " players");
        } catch (Exception e) {
            plugin.getLogger().severe("Error loading cooldowns: " + e.getMessage());
        }
    }

    /**
     * Save cooldowns to file
     */
    public void saveCooldowns() {
        if (!enabled || !persistOnRestart) {
            return;
        }

        try {
            cooldownConfig = new YamlConfiguration();

            for (Map.Entry<UUID, Map<String, Long>> entry : cooldowns.entrySet()) {
                String uuid = entry.getKey().toString();

                for (Map.Entry<String, Long> commandEntry : entry.getValue().entrySet()) {
                    String command = commandEntry.getKey();
                    long cooldownEnd = commandEntry.getValue();

                    // Only save if cooldown hasn't expired
                    if (cooldownEnd > System.currentTimeMillis()) {
                        cooldownConfig.set(uuid + "." + command, cooldownEnd);
                    }
                }
            }

            cooldownConfig.save(cooldownFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Error saving cooldowns: " + e.getMessage());
        }
    }

    /**
     * Check if cooldown system is enabled
     */
    public boolean isEnabled() {
        return enabled;
    }
}
