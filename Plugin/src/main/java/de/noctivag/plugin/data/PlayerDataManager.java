package de.noctivag.plugin.data;

import de.noctivag.plugin.Plugin;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.logging.Level;

/**
 * Verwaltet persistente Spielerdaten (Prefix, Suffix, Nickname)
 */
public class PlayerDataManager {
    private final Plugin plugin;
    private File playerDataFile;
    private FileConfiguration playerData;
    
    // In-Memory Cache für schnellen Zugriff
    private final Map<String, String> prefixes = new HashMap<>();
    private final Map<String, String> suffixes = new HashMap<>();
    private final Map<String, String> nicknames = new HashMap<>();
    
    // Auto-Save Task ID
    private int autoSaveTaskId = -1;
    
    public PlayerDataManager(Plugin plugin) {
        this.plugin = plugin;
        loadPlayerData();
        startAutoSave();
    }
    
    /**
     * Lädt Spielerdaten aus der YAML-Datei
     */
    public void loadPlayerData() {
        playerDataFile = new File(plugin.getDataFolder(), "playerdata.yml");
        
        if (!playerDataFile.exists()) {
            try {
                plugin.getDataFolder().mkdirs();
                playerDataFile.createNewFile();
                plugin.getLogger().info("playerdata.yml wurde erstellt.");
            } catch (IOException e) {
                plugin.getLogger().severe("Konnte playerdata.yml nicht erstellen!");
                e.printStackTrace();
                return;
            }
        }
        
        playerData = YamlConfiguration.loadConfiguration(playerDataFile);
        
        // Lade alle Daten in den Cache
        if (playerData.contains("players")) {
            for (String playerName : playerData.getConfigurationSection("players").getKeys(false)) {
                String path = "players." + playerName;
                
                if (playerData.contains(path + ".prefix")) {
                    prefixes.put(playerName, playerData.getString(path + ".prefix"));
                }
                if (playerData.contains(path + ".suffix")) {
                    suffixes.put(playerName, playerData.getString(path + ".suffix"));
                }
                if (playerData.contains(path + ".nickname")) {
                    nicknames.put(playerName, playerData.getString(path + ".nickname"));
                }
            }
        }
        
        plugin.getLogger().info("Spielerdaten geladen: " + prefixes.size() + " Prefixes, " + 
                                suffixes.size() + " Suffixes, " + nicknames.size() + " Nicknames");
    }
    
    /**
     * Speichert alle Spielerdaten in die YAML-Datei
     */
    public void savePlayerData() {
        if (playerData == null || playerDataFile == null) {
            plugin.getLogger().warning("PlayerData nicht initialisiert - Speichern übersprungen");
            return;
        }
        
        try {
            // Lösche alte Daten
            playerData.set("players", null);
            
            // Speichere Prefixes
            for (Map.Entry<String, String> entry : prefixes.entrySet()) {
                playerData.set("players." + entry.getKey() + ".prefix", entry.getValue());
            }
            
            // Speichere Suffixes
            for (Map.Entry<String, String> entry : suffixes.entrySet()) {
                playerData.set("players." + entry.getKey() + ".suffix", entry.getValue());
            }
            
            // Speichere Nicknames
            for (Map.Entry<String, String> entry : nicknames.entrySet()) {
                playerData.set("players." + entry.getKey() + ".nickname", entry.getValue());
            }
            
            playerData.save(playerDataFile);
            plugin.getLogger().info("Spielerdaten gespeichert.");
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Konnte playerdata.yml nicht speichern!", e);
        }
    }
    
    // ==================== PREFIX METHODS ====================
    
    public void setPrefix(String playerUuid, String prefix) {
        if (prefix == null || prefix.isEmpty()) {
            prefixes.remove(playerUuid);
        } else {
            prefixes.put(playerUuid, prefix);
        }
    }
    
    public String getPrefix(String playerUuid) {
        return prefixes.get(playerUuid);
    }
    
    public void removePrefix(String playerUuid) {
        prefixes.remove(playerUuid);
    }
    
    public boolean hasPrefix(String playerUuid) {
        return prefixes.containsKey(playerUuid);
    }
    
    // ==================== SUFFIX METHODS ====================
    
    public void setSuffix(String playerUuid, String suffix) {
        if (suffix == null || suffix.isEmpty()) {
            suffixes.remove(playerUuid);
        } else {
            suffixes.put(playerUuid, suffix);
        }
    }
    
    public String getSuffix(String playerUuid) {
        return suffixes.get(playerUuid);
    }
    
    public void removeSuffix(String playerUuid) {
        suffixes.remove(playerUuid);
    }
    
    public boolean hasSuffix(String playerUuid) {
        return suffixes.containsKey(playerUuid);
    }
    
    // ==================== NICKNAME METHODS ====================
    
    public void setNickname(String playerUuid, String nickname) {
        if (nickname == null || nickname.isEmpty()) {
            nicknames.remove(playerUuid);
        } else {
            nicknames.put(playerUuid, nickname);
        }
    }
    
    public String getNickname(String playerUuid) {
        return nicknames.get(playerUuid);
    }
    
    public void removeNickname(String playerUuid) {
        nicknames.remove(playerUuid);
    }
    
    public boolean hasNickname(String playerUuid) {
        return nicknames.containsKey(playerUuid);
    }
    
    // ==================== UTILITY METHODS ====================
    
    /**
     * Gibt alle gespeicherten Prefixes zurück
     */
    public Map<String, String> getAllPrefixes() {
        return new HashMap<>(prefixes);
    }
    
    /**
     * Gibt alle gespeicherten Suffixes zurück
     */
    public Map<String, String> getAllSuffixes() {
        return new HashMap<>(suffixes);
    }
    
    /**
     * Gibt alle gespeicherten Nicknames zurück
     */
    public Map<String, String> getAllNicknames() {
        return new HashMap<>(nicknames);
    }
    
    /**
     * Löscht alle Daten eines Spielers
     */
    public void clearPlayerData(String playerName) {
        prefixes.remove(playerName);
        suffixes.remove(playerName);
        nicknames.remove(playerName);
    }
    
    /**
     * Gibt die Datei-Konfiguration zurück
     */
    public FileConfiguration getConfig() {
        return playerData;
    }
    
    /**
     * Startet den Auto-Save Timer
     */
    private void startAutoSave() {
        // Speichere alle 5 Minuten (6000 Ticks)
        int saveInterval = plugin.getConfigManager().getConfig().getInt("settings.auto-save-interval", 300) * 20; // Konvertiere Sekunden zu Ticks
        
        autoSaveTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            savePlayerData();
            plugin.getLogger().info("Auto-Save: Spielerdaten wurden gespeichert.");
        }, saveInterval, saveInterval);
    }
    
    /**
     * Stoppt den Auto-Save Timer
     */
    public void stopAutoSave() {
        if (autoSaveTaskId != -1) {
            Bukkit.getScheduler().cancelTask(autoSaveTaskId);
            autoSaveTaskId = -1;
        }
    }
}
