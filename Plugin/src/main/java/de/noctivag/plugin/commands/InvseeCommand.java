package de.noctivag.plugin.commands;

import de.noctivag.plugin.messages.MessageManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class InvseeCommand implements CommandExecutor {

    private final MessageManager messageManager;

    public InvseeCommand(MessageManager messageManager) {
        this.messageManager = messageManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(messageManager.getError("error.players_only"));
            return true;
        }

        if (!player.hasPermission("plugin.invsee")) {
            player.sendMessage(messageManager.getError("error.no_permission"));
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(messageManager.getError("invsee.usage"));
            return true;
        }

        String targetName = args[0];
        Player target = Bukkit.getPlayer(targetName);

        if (target == null) {
            player.sendMessage(messageManager.getError("invsee.player_not_online", targetName));
            return true;
        }

        // Öffne das Inventar des Zielspielers
        player.openInventory(target.getInventory());
        player.sendMessage(messageManager.getMessage("invsee.success", target.getName()));

        return true;
    }
}
