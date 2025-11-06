package de.noctivag.plugin.managers;

import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class HomeManager {
    private final JavaPlugin plugin;
    private final File homesFile;
    private final Map<UUID, Map<String, Location>> homes;
    private final int maxHomes;

    public HomeManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.homesFile = new File(plugin.getDataFolder(), "homes.yml");
        this.homes = new HashMap<>();
        this.maxHomes = plugin.getConfig().getInt("commands.home.max-homes", 5);
        loadHomes();
    }

    public void loadHomes() {
        if (!homesFile.exists()) {
            return;
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(homesFile);
        ConfigurationSection playersSection = config.getConfigurationSection("homes");
        
        if (playersSection != null) {
            for (String uuidStr : playersSection.getKeys(false)) {
                UUID uuid = UUID.fromString(uuidStr);
                ConfigurationSection playerHomes = playersSection.getConfigurationSection(uuidStr);
                
                if (playerHomes != null) {
                    Map<String, Location> playerHomesMap = new HashMap<>();
                    for (String homeName : playerHomes.getKeys(false)) {
                        Location loc = playerHomes.getLocation(homeName);
                        if (loc != null) {
                            playerHomesMap.put(homeName, loc);
                        }
                    }
                    homes.put(uuid, playerHomesMap);
                }
            }
        }
    }

    public void saveHomes() {
        FileConfiguration config = new YamlConfiguration();

        for (Map.Entry<UUID, Map<String, Location>> entry : homes.entrySet()) {
            String uuidStr = entry.getKey().toString();
            for (Map.Entry<String, Location> homeEntry : entry.getValue().entrySet()) {
                config.set("homes." + uuidStr + "." + homeEntry.getKey(), homeEntry.getValue());
            }
        }

        try {
            config.save(homesFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save homes.yml: " + e.getMessage());
        }
    }

    public boolean setHome(Player player, String name) {
        UUID uuid = player.getUniqueId();
        Map<String, Location> playerHomes = homes.computeIfAbsent(uuid, k -> new HashMap<>());
        
        if (playerHomes.size() >= maxHomes && !playerHomes.containsKey(name)) {
            return false; // Max homes reached
        }
        
        playerHomes.put(name, player.getLocation());
        saveHomes();
        return true;
    }

    public Location getHome(UUID playerId, String name) {
        Map<String, Location> playerHomes = homes.get(playerId);
        if (playerHomes != null) {
            return playerHomes.get(name);
        }
        return null;
    }

    public boolean deleteHome(UUID playerId, String name) {
        Map<String, Location> playerHomes = homes.get(playerId);
        if (playerHomes != null && playerHomes.containsKey(name)) {
            playerHomes.remove(name);
            if (playerHomes.isEmpty()) {
                homes.remove(playerId);
            }
            saveHomes();
            return true;
        }
        return false;
    }

    public Set<String> getHomeNames(UUID playerId) {
        Map<String, Location> playerHomes = homes.get(playerId);
        if (playerHomes != null) {
            return new HashSet<>(playerHomes.keySet());
        }
        return new HashSet<>();
    }

    public int getHomeCount(UUID playerId) {
        Map<String, Location> playerHomes = homes.get(playerId);
        return playerHomes != null ? playerHomes.size() : 0;
    }

    public int getMaxHomes() {
        return maxHomes;
    }
}
