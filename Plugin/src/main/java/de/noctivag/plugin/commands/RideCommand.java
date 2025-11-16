package de.noctivag.plugin.commands;

import de.noctivag.plugin.managers.RideManager;
import de.noctivag.plugin.messages.MessageManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RideCommand implements CommandExecutor {
    
    private final RideManager rideManager;
    private final MessageManager messageManager;

    public RideCommand(RideManager rideManager, MessageManager messageManager) {
        this.rideManager = rideManager;
        this.messageManager = messageManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(messageManager.getMessage("general.player-only"));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("plugin.ride")) {
            player.sendMessage(messageManager.getMessage("general.no-permission"));
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(messageManager.getMessage("ride.usage"));
            return true;
        }

        // Check if stopping ride
        if (args[0].equalsIgnoreCase("stop") || args[0].equalsIgnoreCase("off")) {
            if (rideManager.isRiding(player)) {
                rideManager.stopRiding(player);
                player.sendMessage(messageManager.getMessage("ride.stopped"));
            } else {
                player.sendMessage(messageManager.getMessage("ride.not-riding"));
            }
            return true;
        }

        // Try to ride a player
        Player target = Bukkit.getPlayer(args[0]);
        
        if (target == null || !target.isOnline()) {
            player.sendMessage(messageManager.getMessage("general.player-not-found"));
            return true;
        }

        if (target.equals(player)) {
            player.sendMessage(messageManager.getMessage("ride.self"));
            return true;
        }

        // Check permission to ride players
        if (!player.hasPermission("plugin.ride.players")) {
            player.sendMessage(messageManager.getMessage("general.no-permission"));
            return true;
        }

        if (rideManager.startRiding(player, target)) {
            player.sendMessage(messageManager.getMessage("ride.started", target.getName()));
            target.sendMessage(messageManager.getMessage("ride.being-ridden", player.getName()));
        } else {
            player.sendMessage(messageManager.getMessage("ride.failed"));
        }

        return true;
    }
}
