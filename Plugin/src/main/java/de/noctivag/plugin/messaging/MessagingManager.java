package de.noctivag.plugin.messaging;

import de.noctivag.plugin.Plugin;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.util.*;

/**
 * Manages private messaging between players
 */
public class MessagingManager {
    private final Plugin plugin;
    private final Map<UUID, UUID> replyTargets = new HashMap<>();
    private final Map<UUID, Set<UUID>> ignoredPlayers = new HashMap<>();

    public MessagingManager(Plugin plugin) {
        this.plugin = plugin;
    }

    public void sendMessage(Player sender, Player receiver, String message) {
        if (!plugin.getConfig().getBoolean("modules.messaging.enabled", true)) {
            sender.sendMessage("§cMessaging is currently disabled!");
            return;
        }

        if (isIgnoring(receiver, sender)) {
            sender.sendMessage("§cThat player is ignoring you!");
            return;
        }

        String format = plugin.getConfig().getString("modules.messaging.format",
                "§7[§dPM§7] §f%sender% §7-> §f%receiver%§7: %message%");
        
        String formattedMsg = format
                .replace("%sender%", sender.getName())
                .replace("%receiver%", receiver.getName())
                .replace("%message%", message);

        sender.sendMessage(formattedMsg);
        receiver.sendMessage(formattedMsg);

        // Play sound if enabled
        if (plugin.getConfig().getBoolean("modules.messaging.sound.enabled", true)) {
            receiver.playSound(receiver.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 0.5f, 1.5f);
        }

        // Set reply targets
        replyTargets.put(sender.getUniqueId(), receiver.getUniqueId());
        replyTargets.put(receiver.getUniqueId(), sender.getUniqueId());
    }

    public Player getReplyTarget(Player player) {
        UUID targetUUID = replyTargets.get(player.getUniqueId());
        if (targetUUID == null) return null;
        return Bukkit.getPlayer(targetUUID);
    }

    public void toggleIgnore(Player player, Player target) {
        if (!plugin.getConfig().getBoolean("modules.messaging.ignore.enabled", true)) {
            player.sendMessage("§cIgnore system is currently disabled!");
            return;
        }

        Set<UUID> ignored = ignoredPlayers.computeIfAbsent(player.getUniqueId(), k -> new HashSet<>());

        if (ignored.contains(target.getUniqueId())) {
            ignored.remove(target.getUniqueId());
            player.sendMessage("§aYou are no longer ignoring §e" + target.getName());
        } else {
            ignored.add(target.getUniqueId());
            player.sendMessage("§aYou are now ignoring §e" + target.getName());
        }
    }

    public boolean isIgnoring(Player player, Player target) {
        Set<UUID> ignored = ignoredPlayers.get(player.getUniqueId());
        return ignored != null && ignored.contains(target.getUniqueId());
    }

    public Set<UUID> getIgnoredPlayers(Player player) {
        return ignoredPlayers.getOrDefault(player.getUniqueId(), Collections.emptySet());
    }
}
