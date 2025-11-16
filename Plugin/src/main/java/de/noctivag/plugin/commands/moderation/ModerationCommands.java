package de.noctivag.plugin.commands.moderation;

import de.noctivag.plugin.Plugin;
import de.noctivag.plugin.moderation.ModerationManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.List;

/**
 * Moderation commands: ban, mute, warn
 */
public class ModerationCommands implements CommandExecutor {
    private final Plugin plugin;
    private final ModerationManager moderationManager;

    public ModerationCommands(Plugin plugin, ModerationManager moderationManager) {
        this.plugin = plugin;
        this.moderationManager = moderationManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String cmd = command.getName().toLowerCase();

        switch (cmd) {
            case "ban":
                return handleBan(sender, args, false);
            case "tempban":
                return handleBan(sender, args, true);
            case "unban":
                return handleUnban(sender, args);
            case "mute":
                return handleMute(sender, args);
            case "unmute":
                return handleUnmute(sender, args);
            case "warn":
                return handleWarn(sender, args);
            case "warnings":
                return handleWarnings(sender, args);
            default:
                return false;
        }
    }

    private boolean handleBan(CommandSender sender, String[] args, boolean temporary) {
        if (!sender.hasPermission("plugin.moderation.ban")) {
            sender.sendMessage(plugin.getMessageManager().getMessage("no_permission"));
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage("§cUsage: /" + (temporary ? "tempban" : "ban") + 
                    " <player> " + (temporary ? "<duration> " : "") + "[reason]");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage("§cPlayer not found!");
            return true;
        }

        String reason = null;
        long duration = 0;

        if (temporary) {
            if (args.length < 2) {
                sender.sendMessage("§cUsage: /tempban <player> <duration> [reason]");
                return true;
            }
            duration = parseDuration(args[1]);
            if (duration <= 0) {
                sender.sendMessage("§cInvalid duration! Use format like: 1d, 2h, 30m");
                return true;
            }
            if (args.length > 2) {
                reason = String.join(" ", java.util.Arrays.copyOfRange(args, 2, args.length));
            }
        } else {
            if (args.length > 1) {
                reason = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));
            }
        }

        if (temporary) {
            moderationManager.tempBanPlayer(target.getUniqueId(), reason, duration, sender.getName());
            sender.sendMessage("§a" + target.getName() + " has been temp-banned for " + args[1]);
        } else {
            moderationManager.banPlayer(target.getUniqueId(), reason, sender.getName());
            sender.sendMessage("§a" + target.getName() + " has been banned");
        }

        return true;
    }

    private boolean handleUnban(CommandSender sender, String[] args) {
        if (!sender.hasPermission("plugin.moderation.unban")) {
            sender.sendMessage(plugin.getMessageManager().getMessage("no_permission"));
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage("§cUsage: /unban <player>");
            return true;
        }

        moderationManager.unbanPlayer(args[0]);
        sender.sendMessage("§a" + args[0] + " has been unbanned");

        return true;
    }

    private boolean handleMute(CommandSender sender, String[] args) {
        if (!sender.hasPermission("plugin.moderation.mute")) {
            sender.sendMessage(plugin.getMessageManager().getMessage("no_permission"));
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage("§cUsage: /mute <player> [duration]");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage("§cPlayer not found!");
            return true;
        }

        long duration = 3600000; // Default 1 hour
        if (args.length > 1) {
            duration = parseDuration(args[1]);
            if (duration <= 0) {
                sender.sendMessage("§cInvalid duration! Use format like: 1d, 2h, 30m");
                return true;
            }
        }

        moderationManager.mutePlayer(target.getUniqueId(), duration);
        sender.sendMessage("§a" + target.getName() + " has been muted");
        target.sendMessage("§cYou have been muted!");

        return true;
    }

    private boolean handleUnmute(CommandSender sender, String[] args) {
        if (!sender.hasPermission("plugin.moderation.unmute")) {
            sender.sendMessage(plugin.getMessageManager().getMessage("no_permission"));
            return true;
        }

        if (args.length < 1) {
            sender.sendMessage("§cUsage: /unmute <player>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage("§cPlayer not found!");
            return true;
        }

        moderationManager.unmutePlayer(target.getUniqueId());
        sender.sendMessage("§a" + target.getName() + " has been unmuted");
        target.sendMessage("§aYou have been unmuted!");

        return true;
    }

    private boolean handleWarn(CommandSender sender, String[] args) {
        if (!sender.hasPermission("plugin.moderation.warn")) {
            sender.sendMessage(plugin.getMessageManager().getMessage("no_permission"));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("§cUsage: /warn <player> <reason>");
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage("§cPlayer not found!");
            return true;
        }

        String reason = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));
        
        moderationManager.warnPlayer(target.getUniqueId(), reason);
        sender.sendMessage("§a" + target.getName() + " has been warned");
        target.sendMessage("§c§lYou have been warned!\n§7Reason: §f" + reason);

        List<String> warnings = moderationManager.getWarnings(target.getUniqueId());
        target.sendMessage("§7Total warnings: §e" + warnings.size());

        return true;
    }

    private boolean handleWarnings(CommandSender sender, String[] args) {
        if (!sender.hasPermission("plugin.moderation.warnings")) {
            sender.sendMessage(plugin.getMessageManager().getMessage("no_permission"));
            return true;
        }

        Player target;
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("§cYou must specify a player!");
                return true;
            }
            target = (Player) sender;
        } else {
            target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage("§cPlayer not found!");
                return true;
            }
        }

        List<String> warnings = moderationManager.getWarnings(target.getUniqueId());
        
        if (warnings.isEmpty()) {
            sender.sendMessage("§e" + target.getName() + " has no warnings");
        } else {
            sender.sendMessage("§6§l=== Warnings for " + target.getName() + " ===");
            for (int i = 0; i < warnings.size(); i++) {
                sender.sendMessage("§e" + (i + 1) + ". §7" + warnings.get(i));
            }
        }

        return true;
    }

    private long parseDuration(String duration) {
        if (duration == null || duration.isEmpty()) return 0;

        char unit = duration.charAt(duration.length() - 1);
        String numberStr = duration.substring(0, duration.length() - 1);

        try {
            int number = Integer.parseInt(numberStr);
            switch (Character.toLowerCase(unit)) {
                case 's': return number * 1000L;
                case 'm': return number * 60000L;
                case 'h': return number * 3600000L;
                case 'd': return number * 86400000L;
                case 'w': return number * 604800000L;
                default: return 0;
            }
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
