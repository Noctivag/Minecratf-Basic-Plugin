package de.noctivag.plugin.config;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import java.io.File;
import java.io.IOException;

public class ConfigManager {
    private final JavaPlugin plugin;
    private FileConfiguration config;
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
        config = YamlConfiguration.loadConfiguration(configFile);
    }

    public void saveConfig() {
        try {
            config.save(configFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Konnte Konfiguration nicht speichern: " + e.getMessage());
        }
    }

    public FileConfiguration getConfig() {
        return config;
    }

    // Hilfsmethoden für häufig genutzte Konfigurationswerte
    public int getCommandCooldown(String command) {
        return config.getInt("commands." + command + ".cooldown", 0);
    }

    public int getCommandCost(String command) {
        return config.getInt("commands." + command + ".cost", 0);
    }

    public String getMessage(String key) {
        return config.getString("messages." + key, "Message not found: " + key)
            .replace("&", "§");
    }

    public boolean isDebugMode() {
        return config.getBoolean("settings.debug-mode", false);
    }
}
