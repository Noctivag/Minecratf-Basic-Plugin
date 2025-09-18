package de.noctivag.plugin.data;

import de.noctivag.plugin.Plugin;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;

public class DataManager {
    private final Plugin plugin;
    private File file;
    private FileConfiguration config;

    public DataManager(Plugin plugin) {
        this.plugin = plugin;
        loadData();
    }

    public void loadData() {
        file = new File(plugin.getDataFolder(), "prefixes.yml");

        if (!file.exists()) {
            try {
                plugin.getDataFolder().mkdirs();
                file.createNewFile();
                plugin.getLogger().info("prefixes.yml wurde neu erstellt.");
            } catch (IOException e) {
                plugin.getLogger().severe("Konnte prefixes.yml nicht erstellen!");
                e.printStackTrace();
            }
        }

        config = YamlConfiguration.loadConfiguration(file);
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public void saveData() {
        if (config == null || file == null) return;
        try {
            config.save(file);
            plugin.getLogger().info("prefixes.yml gespeichert.");
        } catch (IOException e) {
            plugin.getLogger().severe("Konnte prefixes.yml nicht speichern!");
            e.printStackTrace();
        }
    }
}
