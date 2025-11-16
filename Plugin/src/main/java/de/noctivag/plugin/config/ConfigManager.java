package de.noctivag.plugin.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;

public class ConfigManager {
    private final JavaPlugin plugin;
    private final File configFile;

    public ConfigManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.configFile = new File(plugin.getDataFolder(), "config.yml");
        loadConfig();
    }

    public void loadConfig() {
        if (!configFile.exists()) {
            plugin.saveResource("config.yml", false);
        }
        // Keep Bukkit's config in sync
        plugin.reloadConfig();
    }

    public void saveConfig() {
        // Delegate to Bukkit's config to avoid stale overwrites
        plugin.saveConfig();
    }

    public FileConfiguration getConfig() {
        // Always return the live Bukkit config
        return plugin.getConfig();
    }

    // Hilfsmethoden für häufig genutzte Konfigurationswerte
    public int getCommandCooldown(String command) {
        return plugin.getConfig().getInt("commands." + command + ".cooldown", 0);
    }

    public int getCommandCost(String command) {
        return plugin.getConfig().getInt("commands." + command + ".cost", 0);
    }

    public String getMessage(String key) {
        return plugin.getConfig().getString("messages." + key, "Message not found: " + key)
            .replace("&", "§");
    }

    public boolean isDebugMode() {
        return plugin.getConfig().getBoolean("settings.debug-mode", false);
    }
}
