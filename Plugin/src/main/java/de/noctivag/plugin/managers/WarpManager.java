package de.noctivag.plugin.managers;

import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class WarpManager {
    private final JavaPlugin plugin;
    private final File warpsFile;
    private final Map<String, Location> warps;

    public WarpManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.warpsFile = new File(plugin.getDataFolder(), "warps.yml");
        this.warps = new HashMap<>();
        loadWarps();
    }

    public void loadWarps() {
        if (!warpsFile.exists()) {
            return;
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(warpsFile);
        
        for (String warpName : config.getKeys(false)) {
            Location loc = config.getLocation(warpName);
            if (loc != null) {
                warps.put(warpName, loc);
            }
        }
    }

    public void saveWarps() {
        FileConfiguration config = new YamlConfiguration();

        for (Map.Entry<String, Location> entry : warps.entrySet()) {
            config.set(entry.getKey(), entry.getValue());
        }

        try {
            config.save(warpsFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save warps.yml: " + e.getMessage());
        }
    }

    public void setWarp(String name, Location location) {
        warps.put(name, location);
        saveWarps();
    }

    public Location getWarp(String name) {
        return warps.get(name);
    }

    public boolean deleteWarp(String name) {
        if (warps.containsKey(name)) {
            warps.remove(name);
            saveWarps();
            return true;
        }
        return false;
    }

    public Set<String> getWarpNames() {
        return warps.keySet();
    }

    public boolean warpExists(String name) {
        return warps.containsKey(name);
    }
}
