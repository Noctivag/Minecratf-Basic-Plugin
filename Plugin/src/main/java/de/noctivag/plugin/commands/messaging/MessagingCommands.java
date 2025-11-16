package de.noctivag.plugin.commands.messaging;

import de.noctivag.plugin.Plugin;
import de.noctivag.plugin.messaging.MessagingManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Arrays;

/**
 * Commands for private messaging: /msg, /reply, /ignore
 */
public class MessagingCommands implements CommandExecutor {
    private final Plugin plugin;
    private final MessagingManager messagingManager;

    public MessagingCommands(Plugin plugin, MessagingManager messagingManager) {
        this.plugin = plugin;
        this.messagingManager = messagingManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String cmd = command.getName().toLowerCase();

        switch (cmd) {
            case "msg":
            case "tell":
            case "whisper":
                return handleMessage(sender, args);
            case "reply":
            case "r":
                return handleReply(sender, args);
            case "ignore":
                return handleIgnore(sender, args);
            default:
                return false;
        }
    }

    private boolean handleMessage(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use this command!");
            return true;
        }

        if (!sender.hasPermission("plugin.messaging.msg")) {
            sender.sendMessage(plugin.getMessageManager().getMessage("no_permission"));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("§cUsage: /msg <player> <message>");
            return true;
        }

        Player player = (Player) sender;
        Player target = Bukkit.getPlayer(args[0]);

        if (target == null) {
            sender.sendMessage("§cPlayer not found!");
            return true;
        }

        if (target.equals(player)) {
            sender.sendMessage("§cYou cannot message yourself!");
            return true;
        }

        String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        messagingManager.sendMessage(player, target, message);

        return true;
    }

    private boolean handleReply(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use this command!");
            return true;
        }

        if (!sender.hasPermission("plugin.messaging.reply")) {
            sender.sendMessage(plugin.getMessageManager().getMessage("no_permission"));
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage("§cUsage: /reply <message>");
            return true;
        }

        Player player = (Player) sender;
        Player target = messagingManager.getReplyTarget(player);

        if (target == null || !target.isOnline()) {
            sender.sendMessage("§cNo one to reply to!");
            return true;
        }

        String message = String.join(" ", args);
        messagingManager.sendMessage(player, target, message);

        return true;
    }

    private boolean handleIgnore(CommandSender sender, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use this command!");
            return true;
        }

        if (!sender.hasPermission("plugin.messaging.ignore")) {
            sender.sendMessage(plugin.getMessageManager().getMessage("no_permission"));
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage("§cUsage: /ignore <player>");
            return true;
        }

        Player player = (Player) sender;
        Player target = Bukkit.getPlayer(args[0]);

        if (target == null) {
            sender.sendMessage("§cPlayer not found!");
            return true;
        }

        if (target.equals(player)) {
            sender.sendMessage("§cYou cannot ignore yourself!");
            return true;
        }

        messagingManager.toggleIgnore(player, target);
        return true;
    }
}
