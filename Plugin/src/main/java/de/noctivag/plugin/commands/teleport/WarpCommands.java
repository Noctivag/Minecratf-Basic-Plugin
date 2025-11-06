package de.noctivag.plugin.commands.teleport;

import de.noctivag.plugin.managers.WarpManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Set;

public class WarpCommands implements CommandExecutor {
    private final WarpManager warpManager;

    public WarpCommands(WarpManager warpManager) {
        this.warpManager = warpManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        String cmd = command.getName().toLowerCase();

        switch (cmd) {
            case "setwarp" -> handleSetWarp(sender, args);
            case "warp" -> handleWarp(sender, args);
            case "delwarp" -> handleDelWarp(sender, args);
            case "warps" -> handleWarps(sender);
        }

        return true;
    }

    private void handleSetWarp(CommandSender sender, String[] args) {
        if (!sender.hasPermission("essentials.setwarp")) {
            sender.sendMessage(Component.text("You don't have permission!").color(NamedTextColor.RED));
            return;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only players can use this command.").color(NamedTextColor.RED));
            return;
        }

        if (args.length == 0) {
            sender.sendMessage(Component.text("Usage: /setwarp <name>").color(NamedTextColor.RED));
            return;
        }

        String warpName = args[0];
        warpManager.setWarp(warpName, player.getLocation());
        sender.sendMessage(Component.text("Warp '" + warpName + "' set!").color(NamedTextColor.GREEN));
    }

    private void handleWarp(CommandSender sender, String[] args) {
        if (!sender.hasPermission("essentials.warp")) {
            sender.sendMessage(Component.text("You don't have permission!").color(NamedTextColor.RED));
            return;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only players can use this command.").color(NamedTextColor.RED));
            return;
        }

        if (args.length == 0) {
            sender.sendMessage(Component.text("Usage: /warp <name>").color(NamedTextColor.RED));
            return;
        }

        String warpName = args[0];
        Location warp = warpManager.getWarp(warpName);

        if (warp == null) {
            sender.sendMessage(Component.text("Warp '" + warpName + "' not found!").color(NamedTextColor.RED));
            return;
        }

        player.teleport(warp);
        sender.sendMessage(Component.text("Teleported to warp '" + warpName + "'!").color(NamedTextColor.GREEN));
    }

    private void handleDelWarp(CommandSender sender, String[] args) {
        if (!sender.hasPermission("essentials.delwarp")) {
            sender.sendMessage(Component.text("You don't have permission!").color(NamedTextColor.RED));
            return;
        }

        if (args.length == 0) {
            sender.sendMessage(Component.text("Usage: /delwarp <name>").color(NamedTextColor.RED));
            return;
        }

        String warpName = args[0];
        if (warpManager.deleteWarp(warpName)) {
            sender.sendMessage(Component.text("Warp '" + warpName + "' deleted!").color(NamedTextColor.GREEN));
        } else {
            sender.sendMessage(Component.text("Warp '" + warpName + "' not found!").color(NamedTextColor.RED));
        }
    }

    private void handleWarps(CommandSender sender) {
        if (!sender.hasPermission("essentials.warps")) {
            sender.sendMessage(Component.text("You don't have permission!").color(NamedTextColor.RED));
            return;
        }

        Set<String> warps = warpManager.getWarpNames();
        
        if (warps.isEmpty()) {
            sender.sendMessage(Component.text("No warps available!").color(NamedTextColor.YELLOW));
            return;
        }

        sender.sendMessage(Component.text("Available warps:").color(NamedTextColor.YELLOW));
        for (String warp : warps) {
            sender.sendMessage(Component.text("- " + warp).color(NamedTextColor.GRAY));
        }
    }
}
