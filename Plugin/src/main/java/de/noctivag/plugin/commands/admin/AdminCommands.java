package de.noctivag.plugin.commands.admin;

import de.noctivag.plugin.messages.MessageManager;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class AdminCommands implements CommandExecutor {

    private final MessageManager messageManager;

    public AdminCommands(MessageManager messageManager) {
        this.messageManager = messageManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        String cmd = command.getName().toLowerCase();

        switch (cmd) {
            case "kick" -> handleKick(sender, args);
            case "invsee" -> handleInvsee(sender, args);
            case "day" -> handleDay(sender);
            case "night" -> handleNight(sender);
            case "sun" -> handleSun(sender);
            case "rain" -> handleRain(sender);
        }

        return true;
    }

    private void handleKick(CommandSender sender, String[] args) {
        if (!sender.hasPermission("plugin.admin")) {
            sender.sendMessage(messageManager.getError("admin.kick.no_permission"));
            return;
        }

        if (args.length == 0) {
            sender.sendMessage(messageManager.getError("admin.kick.usage"));
            return;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(messageManager.getError("admin.kick.player_not_found"));
            return;
        }

        String reason = args.length > 1 ? String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length)) : "Kicked by an admin";
        target.kick(Component.text(reason));
        sender.sendMessage(messageManager.getMessage("admin.kick.success", target.getName(), reason));
    }

    private void handleInvsee(CommandSender sender, String[] args) {
        if (!sender.hasPermission("admin.invsee")) {
            sender.sendMessage(messageManager.getError("admin.invsee.no_permission"));
            return;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(messageManager.getError("admin.invsee.players_only"));
            return;
        }

        if (args.length == 0) {
            sender.sendMessage(messageManager.getError("admin.invsee.usage"));
            return;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(messageManager.getError("admin.invsee.player_not_found"));
            return;
        }

        player.openInventory(target.getInventory());
        sender.sendMessage(messageManager.getMessage("admin.invsee.success", target.getName()));
    }

    private void handleDay(CommandSender sender) {
        if (!sender.hasPermission("admin.time")) {
            sender.sendMessage(messageManager.getError("admin.time.no_permission"));
            return;
        }

        if (sender instanceof Player player) {
            player.getWorld().setTime(1000);
            sender.sendMessage(messageManager.getMessage("admin.time.day"));
        } else {
            Bukkit.getWorlds().get(0).setTime(1000);
            sender.sendMessage(messageManager.getMessage("admin.time.day_main_world"));
        }
    }

    private void handleNight(CommandSender sender) {
        if (!sender.hasPermission("admin.time")) {
            sender.sendMessage(messageManager.getError("admin.time.no_permission"));
            return;
        }

        if (sender instanceof Player player) {
            player.getWorld().setTime(13000);
            sender.sendMessage(messageManager.getMessage("admin.time.night"));
        } else {
            Bukkit.getWorlds().get(0).setTime(13000);
            sender.sendMessage(messageManager.getMessage("admin.time.night_main_world"));
        }
    }

    private void handleSun(CommandSender sender) {
        if (!sender.hasPermission("admin.weather")) {
            sender.sendMessage(messageManager.getError("admin.weather.no_permission"));
            return;
        }

        if (sender instanceof Player player) {
            player.getWorld().setStorm(false);
            player.getWorld().setThundering(false);
            sender.sendMessage(messageManager.getMessage("admin.weather.sun"));
        } else {
            Bukkit.getWorlds().get(0).setStorm(false);
            Bukkit.getWorlds().get(0).setThundering(false);
            sender.sendMessage(messageManager.getMessage("admin.weather.sun_main_world"));
        }
    }

    private void handleRain(CommandSender sender) {
        if (!sender.hasPermission("admin.weather")) {
            sender.sendMessage(messageManager.getError("admin.weather.no_permission"));
            return;
        }

        if (sender instanceof Player player) {
            player.getWorld().setStorm(true);
            sender.sendMessage(messageManager.getMessage("admin.weather.rain"));
        } else {
            Bukkit.getWorlds().get(0).setStorm(true);
            sender.sendMessage(messageManager.getMessage("admin.weather.rain_main_world"));
        }
    }
}
