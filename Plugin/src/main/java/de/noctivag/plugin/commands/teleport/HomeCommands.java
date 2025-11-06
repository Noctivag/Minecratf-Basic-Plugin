package de.noctivag.plugin.commands.teleport;

import de.noctivag.plugin.managers.HomeManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class HomeCommands implements CommandExecutor {
    private final HomeManager homeManager;

    public HomeCommands(HomeManager homeManager) {
        this.homeManager = homeManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only players can use this command.").color(NamedTextColor.RED));
            return true;
        }

        String cmd = command.getName().toLowerCase();

        switch (cmd) {
            case "sethome" -> handleSetHome(player, args);
            case "home" -> handleHome(player, args);
            case "delhome" -> handleDelHome(player, args);
            case "homes" -> handleHomes(player);
        }

        return true;
    }

    private void handleSetHome(Player player, String[] args) {
        if (!player.hasPermission("essentials.sethome")) {
            player.sendMessage(Component.text("You don't have permission!").color(NamedTextColor.RED));
            return;
        }

        String homeName = args.length > 0 ? args[0] : "home";
        
        if (homeManager.setHome(player, homeName)) {
            player.sendMessage(Component.text("Home '" + homeName + "' set!").color(NamedTextColor.GREEN));
        } else {
            player.sendMessage(Component.text("You've reached the maximum number of homes (" + homeManager.getMaxHomes() + ")!").color(NamedTextColor.RED));
        }
    }

    private void handleHome(Player player, String[] args) {
        if (!player.hasPermission("essentials.home")) {
            player.sendMessage(Component.text("You don't have permission!").color(NamedTextColor.RED));
            return;
        }

        String homeName = args.length > 0 ? args[0] : "home";
        Location home = homeManager.getHome(player.getUniqueId(), homeName);

        if (home == null) {
            player.sendMessage(Component.text("Home '" + homeName + "' not found!").color(NamedTextColor.RED));
            return;
        }

        player.teleport(home);
        player.sendMessage(Component.text("Teleported to home '" + homeName + "'!").color(NamedTextColor.GREEN));
    }

    private void handleDelHome(Player player, String[] args) {
        if (!player.hasPermission("essentials.delhome")) {
            player.sendMessage(Component.text("You don't have permission!").color(NamedTextColor.RED));
            return;
        }

        if (args.length == 0) {
            player.sendMessage(Component.text("Usage: /delhome <name>").color(NamedTextColor.RED));
            return;
        }

        String homeName = args[0];
        if (homeManager.deleteHome(player.getUniqueId(), homeName)) {
            player.sendMessage(Component.text("Home '" + homeName + "' deleted!").color(NamedTextColor.GREEN));
        } else {
            player.sendMessage(Component.text("Home '" + homeName + "' not found!").color(NamedTextColor.RED));
        }
    }

    private void handleHomes(Player player) {
        if (!player.hasPermission("essentials.homes")) {
            player.sendMessage(Component.text("You don't have permission!").color(NamedTextColor.RED));
            return;
        }

        Set<String> homes = homeManager.getHomeNames(player.getUniqueId());
        
        if (homes.isEmpty()) {
            player.sendMessage(Component.text("You have no homes set!").color(NamedTextColor.YELLOW));
            return;
        }

        player.sendMessage(Component.text("Your homes (" + homes.size() + "/" + homeManager.getMaxHomes() + "):").color(NamedTextColor.YELLOW));
        for (String home : homes) {
            player.sendMessage(Component.text("- " + home).color(NamedTextColor.GRAY));
        }
    }
}
