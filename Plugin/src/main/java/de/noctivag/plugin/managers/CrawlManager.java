package de.noctivag.plugin.managers;

import de.noctivag.plugin.Plugin;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class CrawlManager {
    
    private final Plugin plugin;
    private final Map<UUID, Integer> crawlingPlayers = new HashMap<>();
    private final Map<UUID, Location> lastFakeHeadBlock = new HashMap<>();

    public CrawlManager(Plugin plugin) {
        this.plugin = plugin;
    }

    public boolean isCrawling(Player player) {
        return crawlingPlayers.containsKey(player.getUniqueId());
    }

    public void startCrawling(Player player) {
        if (isCrawling(player)) {
            return;
        }

        // Encourage crawling look by sneaking
        player.setSneaking(true);

        // Maintain swimming pose while toggled
        int taskId = Bukkit.getScheduler().runTaskTimer(plugin, () -> {
            if (!player.isOnline() || !isCrawling(player)) {
                stopCrawling(player);
                return;
            }

            // Force swimming animation (client shows crawl when head has low clearance)
            player.setSwimming(true); // deprecated but functional on modern Paper

            // Compute the block at player's head level and show a fake barrier there
            Location currentHeadBlock = player.getEyeLocation().toBlockLocation();

            Location last = lastFakeHeadBlock.get(player.getUniqueId());
            if (last != null && !last.equals(currentHeadBlock)) {
                // Revert previous fake block to real state for this player
                BlockData realData = last.getBlock().getBlockData();
                player.sendBlockChange(last, realData);
            }

            // Send fake barrier to enforce low clearance
            player.sendBlockChange(currentHeadBlock, Material.BARRIER.createBlockData());
            lastFakeHeadBlock.put(player.getUniqueId(), currentHeadBlock);

            // Safety: stop if flying
            if (player.isFlying()) {
                stopCrawling(player);
            }
        }, 0L, 1L).getTaskId();

        crawlingPlayers.put(player.getUniqueId(), taskId);
    }

    public void stopCrawling(Player player) {
        UUID uuid = player.getUniqueId();
        
        if (!crawlingPlayers.containsKey(uuid)) {
            return;
        }

        int taskId = crawlingPlayers.remove(uuid);
        Bukkit.getScheduler().cancelTask(taskId);

        player.setSwimming(false);
        player.setSneaking(false);

        // Revert last fake head block if present
        Location last = lastFakeHeadBlock.remove(uuid);
        if (last != null) {
            BlockData realData = last.getBlock().getBlockData();
            player.sendBlockChange(last, realData);
        }
    }

    public void stopAll() {
        for (UUID uuid : crawlingPlayers.keySet()) {
            Player player = Bukkit.getPlayer(uuid);
            if (player != null) {
                stopCrawling(player);
            }
        }
        crawlingPlayers.clear();
    }
}
