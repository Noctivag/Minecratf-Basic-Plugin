package de.noctivag.plugin.commands.teleport;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TeleportCommands implements CommandExecutor {
    private final Map<UUID, UUID> teleportRequests; // target -> requester
    private final Map<UUID, Long> requestExpiry;
    private static final long REQUEST_TIMEOUT = 60000; // 60 seconds

    public TeleportCommands() {
        this.teleportRequests = new HashMap<>();
        this.requestExpiry = new HashMap<>();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        String cmd = command.getName().toLowerCase();

        switch (cmd) {
            case "tp" -> handleTp(sender, args);
            case "tpa" -> handleTpa(sender, args);
            case "tphere" -> handleTphere(sender, args);
            case "tpaccept" -> handleTpAccept(sender);
            case "tpdeny" -> handleTpDeny(sender);
        }

        return true;
    }

    private void handleTp(CommandSender sender, String[] args) {
        if (!sender.hasPermission("essentials.tp")) {
            sender.sendMessage(Component.text("You don't have permission!").color(NamedTextColor.RED));
            return;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only players can use this command.").color(NamedTextColor.RED));
            return;
        }

        if (args.length == 0) {
            sender.sendMessage(Component.text("Usage: /tp <player>").color(NamedTextColor.RED));
            return;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(Component.text("Player not found!").color(NamedTextColor.RED));
            return;
        }

        player.teleport(target);
        sender.sendMessage(Component.text("Teleported to " + target.getName()).color(NamedTextColor.GREEN));
    }

    private void handleTpa(CommandSender sender, String[] args) {
        if (!sender.hasPermission("essentials.tpa")) {
            sender.sendMessage(Component.text("You don't have permission!").color(NamedTextColor.RED));
            return;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only players can use this command.").color(NamedTextColor.RED));
            return;
        }

        if (args.length == 0) {
            sender.sendMessage(Component.text("Usage: /tpa <player>").color(NamedTextColor.RED));
            return;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(Component.text("Player not found!").color(NamedTextColor.RED));
            return;
        }

        if (target.getUniqueId().equals(player.getUniqueId())) {
            sender.sendMessage(Component.text("You cannot teleport to yourself!").color(NamedTextColor.RED));
            return;
        }

        teleportRequests.put(target.getUniqueId(), player.getUniqueId());
        requestExpiry.put(target.getUniqueId(), System.currentTimeMillis() + REQUEST_TIMEOUT);

        sender.sendMessage(Component.text("Teleport request sent to " + target.getName()).color(NamedTextColor.GREEN));
        target.sendMessage(Component.text(player.getName() + " wants to teleport to you. Use /tpaccept or /tpdeny").color(NamedTextColor.YELLOW));
    }

    private void handleTphere(CommandSender sender, String[] args) {
        if (!sender.hasPermission("essentials.tphere")) {
            sender.sendMessage(Component.text("You don't have permission!").color(NamedTextColor.RED));
            return;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only players can use this command.").color(NamedTextColor.RED));
            return;
        }

        if (args.length == 0) {
            sender.sendMessage(Component.text("Usage: /tphere <player>").color(NamedTextColor.RED));
            return;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(Component.text("Player not found!").color(NamedTextColor.RED));
            return;
        }

        target.teleport(player);
        sender.sendMessage(Component.text(target.getName() + " teleported to you").color(NamedTextColor.GREEN));
        target.sendMessage(Component.text("Teleported to " + player.getName()).color(NamedTextColor.GREEN));
    }

    private void handleTpAccept(CommandSender sender) {
        if (!sender.hasPermission("essentials.tpaccept")) {
            sender.sendMessage(Component.text("You don't have permission!").color(NamedTextColor.RED));
            return;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only players can use this command.").color(NamedTextColor.RED));
            return;
        }

        UUID requesterId = teleportRequests.get(player.getUniqueId());
        if (requesterId == null) {
            sender.sendMessage(Component.text("You have no pending teleport requests!").color(NamedTextColor.RED));
            return;
        }

        Long expiryTime = requestExpiry.get(player.getUniqueId());
        if (expiryTime == null || System.currentTimeMillis() > expiryTime) {
            teleportRequests.remove(player.getUniqueId());
            requestExpiry.remove(player.getUniqueId());
            sender.sendMessage(Component.text("Teleport request has expired!").color(NamedTextColor.RED));
            return;
        }

        Player requester = Bukkit.getPlayer(requesterId);
        if (requester == null) {
            teleportRequests.remove(player.getUniqueId());
            requestExpiry.remove(player.getUniqueId());
            sender.sendMessage(Component.text("Player is no longer online!").color(NamedTextColor.RED));
            return;
        }

        requester.teleport(player);
        sender.sendMessage(Component.text("Teleport request accepted!").color(NamedTextColor.GREEN));
        requester.sendMessage(Component.text("Teleport request accepted by " + player.getName()).color(NamedTextColor.GREEN));

        teleportRequests.remove(player.getUniqueId());
        requestExpiry.remove(player.getUniqueId());
    }

    private void handleTpDeny(CommandSender sender) {
        if (!sender.hasPermission("essentials.tpdeny")) {
            sender.sendMessage(Component.text("You don't have permission!").color(NamedTextColor.RED));
            return;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only players can use this command.").color(NamedTextColor.RED));
            return;
        }

        UUID requesterId = teleportRequests.get(player.getUniqueId());
        if (requesterId == null) {
            sender.sendMessage(Component.text("You have no pending teleport requests!").color(NamedTextColor.RED));
            return;
        }

        Player requester = Bukkit.getPlayer(requesterId);
        if (requester != null) {
            requester.sendMessage(Component.text("Teleport request denied by " + player.getName()).color(NamedTextColor.RED));
        }

        sender.sendMessage(Component.text("Teleport request denied!").color(NamedTextColor.GREEN));

        teleportRequests.remove(player.getUniqueId());
        requestExpiry.remove(player.getUniqueId());
    }
}
