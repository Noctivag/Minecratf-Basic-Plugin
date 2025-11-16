package de.noctivag.plugin.managers;

import de.noctivag.plugin.Plugin;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class AfkManager {
    
    private final Plugin plugin;
    private final Map<UUID, Long> afkPlayers = new HashMap<>();
    private final Map<UUID, Integer> afkTasks = new HashMap<>();

    public AfkManager(Plugin plugin) {
        this.plugin = plugin;
    }

    public boolean isAfk(Player player) {
        return afkPlayers.containsKey(player.getUniqueId());
    }

    public void setAfk(Player player, boolean afk) {
        UUID uuid = player.getUniqueId();
        
        if (afk) {
            if (!isAfk(player)) {
                afkPlayers.put(uuid, System.currentTimeMillis());
                
                // Broadcast AFK status
                if (plugin.getConfig().getBoolean("modules.cosmetics.afk.broadcast", true)) {
                    Bukkit.broadcast(plugin.getMessageManager()
                        .getMessage("afk.enabled", player.getName()));
                }
            }
        } else {
            if (isAfk(player)) {
                long afkTime = System.currentTimeMillis() - afkPlayers.remove(uuid);
                
                // Cancel auto-AFK task if exists
                if (afkTasks.containsKey(uuid)) {
                    Bukkit.getScheduler().cancelTask(afkTasks.remove(uuid));
                }
                
                // Broadcast return
                if (plugin.getConfig().getBoolean("modules.cosmetics.afk.broadcast", true)) {
                    Bukkit.broadcast(plugin.getMessageManager()
                        .getMessage("afk.disabled", player.getName(), formatTime(afkTime / 1000)));
                }
            }
        }
    }

    public void startAutoAfkTimer(Player player) {
        UUID uuid = player.getUniqueId();
        
        // Cancel existing task
        if (afkTasks.containsKey(uuid)) {
            Bukkit.getScheduler().cancelTask(afkTasks.get(uuid));
        }
        
        // Get auto-AFK time from config (in seconds)
        int autoAfkTime = plugin.getConfig().getInt("modules.cosmetics.afk.auto-afk-time", 300);
        
        if (autoAfkTime <= 0) {
            return; // Auto-AFK disabled
        }
        
        // Schedule auto-AFK
        int taskId = Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline() && !isAfk(player)) {
                setAfk(player, true);
            }
        }, autoAfkTime * 20L).getTaskId();
        
        afkTasks.put(uuid, taskId);
    }

    public void removePlayer(Player player) {
        UUID uuid = player.getUniqueId();
        afkPlayers.remove(uuid);
        
        if (afkTasks.containsKey(uuid)) {
            Bukkit.getScheduler().cancelTask(afkTasks.remove(uuid));
        }
    }

    public long getAfkTime(Player player) {
        if (!isAfk(player)) {
            return 0;
        }
        return (System.currentTimeMillis() - afkPlayers.get(player.getUniqueId())) / 1000;
    }

    private String formatTime(long seconds) {
        if (seconds < 60) {
            return seconds + "s";
        } else if (seconds < 3600) {
            return (seconds / 60) + "m";
        } else {
            return (seconds / 3600) + "h " + ((seconds % 3600) / 60) + "m";
        }
    }

    public void cleanup() {
        afkPlayers.clear();
        for (int taskId : afkTasks.values()) {
            Bukkit.getScheduler().cancelTask(taskId);
        }
        afkTasks.clear();
    }
}
