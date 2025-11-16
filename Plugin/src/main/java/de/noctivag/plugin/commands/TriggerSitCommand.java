package de.noctivag.plugin.commands;

import de.noctivag.plugin.managers.SitManager;
import de.noctivag.plugin.messages.MessageManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TriggerSitCommand implements CommandExecutor {
    private final SitManager sitManager;
    private final MessageManager messageManager;

    public TriggerSitCommand(SitManager sitManager, MessageManager messageManager) {
        this.sitManager = sitManager;
        this.messageManager = messageManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(messageManager.getError("error.players_only"));
            return true;
        }

        if (!player.hasPermission("plugin.sit")) {
            player.sendMessage(messageManager.getError("error.no_permission"));
            return true;
        }

        // Wenn der Spieler bereits sitzt, stehe auf
        if (sitManager.isSitting(player)) {
            if (sitManager.unsitPlayer(player)) {
                player.sendMessage(messageManager.getMessage("sit.stand_up"));
            }
        } else {
            // Ansonsten setze dich hin
            if (sitManager.sitPlayer(player)) {
                player.sendMessage(messageManager.getMessage("sit.sit_down"));
            } else {
                player.sendMessage(messageManager.getError("sit.cant_sit_here"));
            }
        }

        return true;
    }
}
