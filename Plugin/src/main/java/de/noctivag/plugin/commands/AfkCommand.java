package de.noctivag.plugin.commands;

import de.noctivag.plugin.managers.AfkManager;
import de.noctivag.plugin.messages.MessageManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AfkCommand implements CommandExecutor {
    
    private final AfkManager afkManager;
    private final MessageManager messageManager;

    public AfkCommand(AfkManager afkManager, MessageManager messageManager) {
        this.afkManager = afkManager;
        this.messageManager = messageManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(messageManager.getMessage("general.player-only"));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("plugin.afk")) {
            player.sendMessage(messageManager.getMessage("general.no-permission"));
            return true;
        }

        // Toggle AFK status
        afkManager.setAfk(player, !afkManager.isAfk(player));

        return true;
    }
}
