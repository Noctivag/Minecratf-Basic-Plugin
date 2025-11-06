package de.noctivag.plugin.commands.admin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class AdminCommands implements CommandExecutor {

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
        if (!sender.hasPermission("admin.kick")) {
            sender.sendMessage(Component.text("You don't have permission!").color(NamedTextColor.RED));
            return;
        }

        if (args.length == 0) {
            sender.sendMessage(Component.text("Usage: /kick <player> [reason]").color(NamedTextColor.RED));
            return;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(Component.text("Player not found!").color(NamedTextColor.RED));
            return;
        }

        String reason = args.length > 1 ? String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length)) : "Kicked by an admin";
        target.kick(Component.text(reason));
        sender.sendMessage(Component.text("Kicked " + target.getName() + " for: " + reason).color(NamedTextColor.GREEN));
    }

    private void handleInvsee(CommandSender sender, String[] args) {
        if (!sender.hasPermission("admin.invsee")) {
            sender.sendMessage(Component.text("You don't have permission!").color(NamedTextColor.RED));
            return;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only players can use this command.").color(NamedTextColor.RED));
            return;
        }

        if (args.length == 0) {
            sender.sendMessage(Component.text("Usage: /invsee <player>").color(NamedTextColor.RED));
            return;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(Component.text("Player not found!").color(NamedTextColor.RED));
            return;
        }

        player.openInventory(target.getInventory());
        sender.sendMessage(Component.text("Opening " + target.getName() + "'s inventory").color(NamedTextColor.GREEN));
    }

    private void handleDay(CommandSender sender) {
        if (!sender.hasPermission("admin.time")) {
            sender.sendMessage(Component.text("You don't have permission!").color(NamedTextColor.RED));
            return;
        }

        if (sender instanceof Player player) {
            player.getWorld().setTime(1000);
            sender.sendMessage(Component.text("Time set to day!").color(NamedTextColor.GREEN));
        } else {
            Bukkit.getWorlds().get(0).setTime(1000);
            sender.sendMessage(Component.text("Time set to day in main world!").color(NamedTextColor.GREEN));
        }
    }

    private void handleNight(CommandSender sender) {
        if (!sender.hasPermission("admin.time")) {
            sender.sendMessage(Component.text("You don't have permission!").color(NamedTextColor.RED));
            return;
        }

        if (sender instanceof Player player) {
            player.getWorld().setTime(13000);
            sender.sendMessage(Component.text("Time set to night!").color(NamedTextColor.GREEN));
        } else {
            Bukkit.getWorlds().get(0).setTime(13000);
            sender.sendMessage(Component.text("Time set to night in main world!").color(NamedTextColor.GREEN));
        }
    }

    private void handleSun(CommandSender sender) {
        if (!sender.hasPermission("admin.weather")) {
            sender.sendMessage(Component.text("You don't have permission!").color(NamedTextColor.RED));
            return;
        }

        if (sender instanceof Player player) {
            player.getWorld().setStorm(false);
            player.getWorld().setThundering(false);
            sender.sendMessage(Component.text("Weather set to clear!").color(NamedTextColor.GREEN));
        } else {
            Bukkit.getWorlds().get(0).setStorm(false);
            Bukkit.getWorlds().get(0).setThundering(false);
            sender.sendMessage(Component.text("Weather set to clear in main world!").color(NamedTextColor.GREEN));
        }
    }

    private void handleRain(CommandSender sender) {
        if (!sender.hasPermission("admin.weather")) {
            sender.sendMessage(Component.text("You don't have permission!").color(NamedTextColor.RED));
            return;
        }

        if (sender instanceof Player player) {
            player.getWorld().setStorm(true);
            sender.sendMessage(Component.text("Weather set to rain!").color(NamedTextColor.GREEN));
        } else {
            Bukkit.getWorlds().get(0).setStorm(true);
            sender.sendMessage(Component.text("Weather set to rain in main world!").color(NamedTextColor.GREEN));
        }
    }
}
