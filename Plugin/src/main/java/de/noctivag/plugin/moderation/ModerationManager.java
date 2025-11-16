package de.noctivag.plugin.moderation;

import de.noctivag.plugin.Plugin;
import org.bukkit.BanList;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

/**
 * Manages moderation - bans, mutes, warns
 */
public class ModerationManager {
    private final Plugin plugin;
    private final Map<UUID, Long> mutedPlayers = new HashMap<>();
    private final Map<UUID, List<String>> warnings = new HashMap<>();
    private File moderationFile;
    private FileConfiguration moderationConfig;

    public ModerationManager(Plugin plugin) {
        this.plugin = plugin;
        loadModeration();
    }

    private void loadModeration() {
        moderationFile = new File(plugin.getDataFolder(), "moderation.yml");
        if (!moderationFile.exists()) {
            try {
                moderationFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not create moderation.yml", e);
            }
        }

        moderationConfig = YamlConfiguration.loadConfiguration(moderationFile);

        // Load mutes
        if (moderationConfig.contains("mutes")) {
            for (String uuidStr : moderationConfig.getConfigurationSection("mutes").getKeys(false)) {
                UUID uuid = UUID.fromString(uuidStr);
                long expiry = moderationConfig.getLong("mutes." + uuidStr);
                if (expiry > System.currentTimeMillis()) {
                    mutedPlayers.put(uuid, expiry);
                }
            }
        }

        // Load warnings
        if (moderationConfig.contains("warnings")) {
            for (String uuidStr : moderationConfig.getConfigurationSection("warnings").getKeys(false)) {
                UUID uuid = UUID.fromString(uuidStr);
                List<String> warns = moderationConfig.getStringList("warnings." + uuidStr);
                warnings.put(uuid, new ArrayList<>(warns));
            }
        }
    }

    public void save() {
        // Save mutes
        for (Map.Entry<UUID, Long> entry : mutedPlayers.entrySet()) {
            moderationConfig.set("mutes." + entry.getKey().toString(), entry.getValue());
        }

        // Save warnings
        for (Map.Entry<UUID, List<String>> entry : warnings.entrySet()) {
            moderationConfig.set("warnings." + entry.getKey().toString(), entry.getValue());
        }

        try {
            moderationConfig.save(moderationFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save moderation.yml", e);
        }
    }

    // Ban management
    public void banPlayer(UUID uuid, String reason, String source) {
        if (!plugin.getConfig().getBoolean("modules.moderation.ban.enabled", true)) {
            return;
        }

        Player player = Bukkit.getPlayer(uuid);
        if (player != null && player.isOnline()) {
            player.kick(net.kyori.adventure.text.Component.text("§cYou have been banned!\n§7Reason: §f" + 
                (reason != null ? reason : "No reason")));
        }
    }

    public void tempBanPlayer(UUID uuid, String reason, long duration, String source) {
        if (!plugin.getConfig().getBoolean("modules.moderation.ban.enabled", true)) {
            return;
        }

        Player player = Bukkit.getPlayer(uuid);
        if (player != null && player.isOnline()) {
            player.kick(net.kyori.adventure.text.Component.text("§cYou have been temporarily banned!\n§7Reason: §f" + 
                (reason != null ? reason : "No reason") + "\n§7Expires: §f" + formatTime(duration)));
        }
    }

    public void unbanPlayer(String playerName) {
        // Note: Modern ban system requires direct server access
        // This is a simplified version - server owners should use /pardon or similar
    }

    // Mute management
    public void mutePlayer(UUID uuid, long duration) {
        if (!plugin.getConfig().getBoolean("modules.moderation.mute.enabled", true)) {
            return;
        }

        mutedPlayers.put(uuid, System.currentTimeMillis() + duration);
    }

    public void unmutePlayer(UUID uuid) {
        mutedPlayers.remove(uuid);
    }

    public boolean isMuted(UUID uuid) {
        Long muteExpiry = mutedPlayers.get(uuid);
        if (muteExpiry == null) return false;

        if (muteExpiry < System.currentTimeMillis()) {
            mutedPlayers.remove(uuid);
            return false;
        }

        return true;
    }

    public long getMuteRemaining(UUID uuid) {
        Long muteExpiry = mutedPlayers.get(uuid);
        if (muteExpiry == null) return 0;

        long remaining = muteExpiry - System.currentTimeMillis();
        return remaining > 0 ? remaining : 0;
    }

    // Warning management
    public void warnPlayer(UUID uuid, String reason) {
        if (!plugin.getConfig().getBoolean("modules.moderation.warn.enabled", true)) {
            return;
        }

        List<String> playerWarnings = warnings.computeIfAbsent(uuid, k -> new ArrayList<>());
        playerWarnings.add(reason + " [" + new Date() + "]");

        int maxWarnings = plugin.getConfig().getInt("modules.moderation.warn.max-warnings", 3);
        boolean autoBan = plugin.getConfig().getBoolean("modules.moderation.warn.auto-ban", true);

        if (autoBan && playerWarnings.size() >= maxWarnings) {
            banPlayer(uuid, "Exceeded maximum warnings (" + maxWarnings + ")", "AutoMod");
        }
    }

    public List<String> getWarnings(UUID uuid) {
        return warnings.getOrDefault(uuid, Collections.emptyList());
    }

    public void clearWarnings(UUID uuid) {
        warnings.remove(uuid);
    }

    private String formatTime(long milliseconds) {
        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;
        long days = hours / 24;

        if (days > 0) {
            return days + "d " + (hours % 24) + "h";
        } else if (hours > 0) {
            return hours + "h " + (minutes % 60) + "m";
        } else if (minutes > 0) {
            return minutes + "m " + (seconds % 60) + "s";
        } else {
            return seconds + "s";
        }
    }

    public void reload() {
        loadModeration();
    }
}
