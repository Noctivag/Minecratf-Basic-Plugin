package de.noctivag.plugin;

import de.noctivag.plugin.messages.MessageManager;
import de.noctivag.plugin.utils.ColorUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.logging.Level;

public class JoinMessageCommand implements CommandExecutor {
    private final JoinMessageManager joinMessageManager;
    private final MessageManager messageManager;
    private final Plugin plugin;

    public JoinMessageCommand(Plugin plugin, JoinMessageManager joinMessageManager, MessageManager messageManager) {
        this.plugin = plugin;
        this.joinMessageManager = joinMessageManager;
        this.messageManager = messageManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("plugin.joinmessage")) {
            sender.sendMessage(messageManager.getError("error.no_permission"));
            return true;
        }

        if (args.length < 1) {
            sendHelp(sender);
            return true;
        }

        try {
            switch (args[0].toLowerCase()) {
                case "set" -> handleSet(sender, args);
                case "remove" -> handleRemove(sender, args);
                case "toggle", "enable", "disable" -> handleToggle(sender, args);
                case "setdefault" -> handleSetDefault(sender, args);
                case "reload" -> {
                    if (!sender.hasPermission("plugin.joinmessage.reload")) {
                        sender.sendMessage(messageManager.getError("join_message.reload.no_permission"));
                    } else {
                        joinMessageManager.reload();
                        sender.sendMessage(messageManager.getMessage("join_message.reload.success"));
                    }
                }
                default -> sendHelp(sender);
            }
        } catch (Exception e) {
            sender.sendMessage(messageManager.getError("join_message.error.generic", e.getMessage()));
            if (plugin.getConfigManager() != null && plugin.getConfigManager().isDebugMode()) {
                plugin.getLogger().log(Level.SEVERE, "Exception in JoinMessageCommand", e);
            }
        }
        return true;
    }

    private void handleSet(CommandSender sender, String[] args) {
        if (args.length < 3) {
            sender.sendMessage(messageManager.getError("join_message.usage.set"));
            return;
        }

        String playerName = args[1];
        boolean checkExists = plugin.getConfigManager() != null &&
            plugin.getConfigManager().getConfig().getBoolean("settings.check-player-exists", true);

        if (checkExists && Bukkit.getPlayer(playerName) == null &&
            !sender.hasPermission("plugin.joinmessage.bypass")) {
            sender.sendMessage(messageManager.getError("join_message.error.player_not_found", playerName));
            return;
        }

        String message = String.join(" ", Arrays.copyOfRange(args, 2, args.length));
        joinMessageManager.setCustomMessage(playerName, message);

        sender.sendMessage(messageManager.getMessage("join_message.set.success", playerName)
            .append(ColorUtils.parseColor(message)));
    }

    private void handleRemove(CommandSender sender, String[] args) {
        if (args.length != 2) {
            sender.sendMessage(messageManager.getError("join_message.usage.remove"));
            return;
        }

        String playerName = args[1].toLowerCase();
        if (joinMessageManager.hasCustomMessage(playerName)) {
            joinMessageManager.removeCustomMessage(playerName);
            sender.sendMessage(messageManager.getMessage("join_message.remove.success", playerName));
        } else {
            sender.sendMessage(messageManager.getError("join_message.remove.no_message", playerName));
        }
    }

    private void handleToggle(CommandSender sender, String[] args) {
        if (args.length != 2) {
            sender.sendMessage(messageManager.getError("join_message.usage.toggle", args.length > 0 ? args[0] : "toggle"));
            return;
        }

        String playerName = args[1].toLowerCase();
        String action = args[0].toLowerCase();
        boolean shouldEnable = action.equals("enable") ||
            (action.equals("toggle") && joinMessageManager.isMessageDisabled(playerName));

        joinMessageManager.setMessageEnabled(playerName, shouldEnable);

        Component statusComponent = shouldEnable ?
            messageManager.getMessage("join_message.toggle.enabled_status") :
            messageManager.getMessage("join_message.toggle.disabled_status");

        sender.sendMessage(messageManager.getMessage("join_message.toggle.success", playerName)
            .append(statusComponent));
    }

    private void handleSetDefault(CommandSender sender, String[] args) {
        if (args.length < 2) {
            sender.sendMessage(messageManager.getError("join_message.usage.setdefault"));
            return;
        }

        String message = String.join(" ", Arrays.copyOfRange(args, 1, args.length));
        joinMessageManager.setDefaultMessage(message);

        sender.sendMessage(messageManager.getMessage("join_message.setdefault.success")
            .append(ColorUtils.parseColor(message)));
    }

    private void sendHelp(CommandSender sender) {
        messageManager.getMessageList("join_message.help").forEach(sender::sendMessage);
    }
}
