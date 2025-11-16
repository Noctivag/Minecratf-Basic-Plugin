package de.noctivag.plugin.commands;

import de.noctivag.plugin.Plugin;
import de.noctivag.plugin.messages.MessageManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class VanishCommand implements CommandExecutor {
    private final Set<UUID> vanishedPlayers = new HashSet<>();
    private final MessageManager messageManager;
    private final Plugin plugin;

    public VanishCommand(Plugin plugin, MessageManager messageManager) {
        this.plugin = plugin;
        this.messageManager = messageManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(messageManager.getError("error.players_only"));
            return true;
        }

        if (!player.hasPermission("plugin.vanish")) {
            player.sendMessage(messageManager.getError("error.no_permission"));
            return true;
        }

        UUID playerId = player.getUniqueId();

        if (vanishedPlayers.contains(playerId)) {
            // Player is vanished -> make visible
            vanishedPlayers.remove(playerId);

            // Make visible to all players
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                onlinePlayer.showPlayer(plugin, player);
            }

            player.sendMessage(messageManager.getMessage("vanish.visible"));
        } else {
            // Player is visible -> make vanished
            vanishedPlayers.add(playerId);

            // Make invisible to all players without permission
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (!onlinePlayer.hasPermission("plugin.vanish.see") && !onlinePlayer.equals(player)) {
                    onlinePlayer.hidePlayer(plugin, player);
                }
            }

            player.sendMessage(messageManager.getMessage("vanish.invisible"));
            player.sendMessage(messageManager.getMessage("vanish.admin_see"));
        }

        return true;
    }

    public boolean isVanished(UUID playerId) {
        return vanishedPlayers.contains(playerId);
    }
    
    public Set<UUID> getVanishedPlayers() {
        return new HashSet<>(vanishedPlayers);
    }
}

