package de.noctivag.plugin.managers;

import de.noctivag.plugin.Plugin;
import de.noctivag.plugin.utils.SafeTeleportUtil;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitTask;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Supplier;

public class TeleportWarmupManager {
    private final Plugin plugin;
    private final Map<UUID, Warmup> warmups = new ConcurrentHashMap<>();

    public TeleportWarmupManager(Plugin plugin) {
        this.plugin = plugin;
    }

    public boolean isWarmingUp(UUID playerId) {
        return warmups.containsKey(playerId);
    }

    public void cancelWarmup(Player player, String reasonMessage) {
        Warmup w = warmups.remove(player.getUniqueId());
        if (w != null && w.task != null) {
            w.task.cancel();
        }
        if (reasonMessage != null && !reasonMessage.isEmpty()) {
            player.sendMessage(reasonMessage);
        }
    }

    public void startTeleport(Player player, Supplier<Location> targetSupplier) {
        performTeleport(player, targetSupplier);
    }

    private void performTeleport(Player player, Supplier<Location> targetSupplier) {
        if (player == null || !player.isOnline()) return;

        Location target = targetSupplier.get();
        if (target == null || target.getWorld() == null) return;

        boolean safeTeleport = plugin.getConfig().getBoolean("modules.teleportation.safe-teleport", true);
        Location dest = target;
        if (safeTeleport) {
            Location safe = SafeTeleportUtil.findSafeLocationNear(target);
            if (safe != null) {
                dest = safe;
            }
        }

        player.teleport(dest);
        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);
    }

    public boolean handleMove(Player player, Location from, Location to) {
        Warmup w = warmups.get(player.getUniqueId());
        if (w == null) return false;
        if (!w.cancelOnMove) return false;

        // Cancel only when block position changes
        if (from.getBlockX() == to.getBlockX() && from.getBlockY() == to.getBlockY() && from.getBlockZ() == to.getBlockZ()) {
            return false;
        }

        String cancelMsg = plugin.getConfig().getString("messages.teleport-cancelled", "§cTeleportation cancelled!");
        cancelWarmup(player, cancelMsg);
        return true;
    }

    public void handleQuit(UUID playerId) {
        Warmup w = warmups.remove(playerId);
        if (w != null && w.task != null) {
            w.task.cancel();
        }
    }

    private static class Warmup {
        final Location startLocation;
        final boolean cancelOnMove;
        final BukkitTask task;

        Warmup(Location startLocation, boolean cancelOnMove, BukkitTask task) {
            this.startLocation = startLocation;
            this.cancelOnMove = cancelOnMove;
            this.task = task;
        }
    }
}
