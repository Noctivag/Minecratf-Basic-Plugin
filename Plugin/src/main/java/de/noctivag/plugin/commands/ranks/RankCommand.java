package de.noctivag.plugin.commands.ranks;

import de.noctivag.plugin.Plugin;
import de.noctivag.plugin.permissions.Rank;
import de.noctivag.plugin.permissions.RankManager;
import de.noctivag.plugin.utils.ColorUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.UUID;

public class RankCommand implements CommandExecutor {
    private final RankManager rankManager;

    public RankCommand(Plugin plugin) {
        this.rankManager = plugin.getRankManager();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        // /rank - show your rank
        // /rank <player> - show player's rank
        // /rank list - list all ranks
        // /rank create <name> <prefix> <priority> - create new rank
        // /rank delete <name> - delete rank
        // /rank setprefix <name> <prefix> - set rank prefix
        // /rank setsuffix <name> <suffix> - set rank suffix
        // /rank setpriority <name> <priority> - set rank priority
        // /rank addperm <name> <permission> - add permission to rank
        // /rank removeperm <name> <permission> - remove permission from rank

        if (args.length == 0) {
            if (!(sender instanceof Player player)) {
                sender.sendMessage(Component.text("Only players can use this command.").color(NamedTextColor.RED));
                return true;
            }
            showPlayerRank(sender, player);
            return true;
        }

        String subCommand = args[0].toLowerCase();

        switch (subCommand) {
            case "list" -> {
                if (!sender.hasPermission("rank.list")) {
                    sender.sendMessage(Component.text("You don't have permission!").color(NamedTextColor.RED));
                    return true;
                }
                listRanks(sender);
            }
            case "create" -> {
                if (!sender.hasPermission("rank.admin")) {
                    sender.sendMessage(Component.text("You don't have permission!").color(NamedTextColor.RED));
                    return true;
                }
                if (args.length < 4) {
                    sender.sendMessage(Component.text("Usage: /rank create <name> <prefix> <priority>").color(NamedTextColor.RED));
                    return true;
                }
                createRank(sender, args[1], args[2], args[3]);
            }
            case "delete" -> {
                if (!sender.hasPermission("rank.admin")) {
                    sender.sendMessage(Component.text("You don't have permission!").color(NamedTextColor.RED));
                    return true;
                }
                if (args.length < 2) {
                    sender.sendMessage(Component.text("Usage: /rank delete <name>").color(NamedTextColor.RED));
                    return true;
                }
                deleteRank(sender, args[1]);
            }
            case "setprefix" -> {
                if (!sender.hasPermission("rank.admin")) {
                    sender.sendMessage(Component.text("You don't have permission!").color(NamedTextColor.RED));
                    return true;
                }
                if (args.length < 3) {
                    sender.sendMessage(Component.text("Usage: /rank setprefix <name> <prefix>").color(NamedTextColor.RED));
                    return true;
                }
                setRankPrefix(sender, args[1], String.join(" ", java.util.Arrays.copyOfRange(args, 2, args.length)));
            }
            case "setsuffix" -> {
                if (!sender.hasPermission("rank.admin")) {
                    sender.sendMessage(Component.text("You don't have permission!").color(NamedTextColor.RED));
                    return true;
                }
                if (args.length < 3) {
                    sender.sendMessage(Component.text("Usage: /rank setsuffix <name> <suffix>").color(NamedTextColor.RED));
                    return true;
                }
                setRankSuffix(sender, args[1], String.join(" ", java.util.Arrays.copyOfRange(args, 2, args.length)));
            }
            case "setpriority" -> {
                if (!sender.hasPermission("rank.admin")) {
                    sender.sendMessage(Component.text("You don't have permission!").color(NamedTextColor.RED));
                    return true;
                }
                if (args.length < 3) {
                    sender.sendMessage(Component.text("Usage: /rank setpriority <name> <priority>").color(NamedTextColor.RED));
                    return true;
                }
                setRankPriority(sender, args[1], args[2]);
            }
            case "addperm" -> {
                if (!sender.hasPermission("rank.admin")) {
                    sender.sendMessage(Component.text("You don't have permission!").color(NamedTextColor.RED));
                    return true;
                }
                if (args.length < 3) {
                    sender.sendMessage(Component.text("Usage: /rank addperm <name> <permission>").color(NamedTextColor.RED));
                    return true;
                }
                addPermission(sender, args[1], args[2]);
            }
            case "removeperm" -> {
                if (!sender.hasPermission("rank.admin")) {
                    sender.sendMessage(Component.text("You don't have permission!").color(NamedTextColor.RED));
                    return true;
                }
                if (args.length < 3) {
                    sender.sendMessage(Component.text("Usage: /rank removeperm <name> <permission>").color(NamedTextColor.RED));
                    return true;
                }
                removePermission(sender, args[1], args[2]);
            }
            default -> {
                // Show player rank for specified player
                Player target = Bukkit.getPlayer(args[0]);
                if (target == null) {
                    sender.sendMessage(Component.text("Player not found!").color(NamedTextColor.RED));
                    return true;
                }
                showPlayerRank(sender, target);
            }
        }

        return true;
    }

    private void showPlayerRank(CommandSender sender, Player player) {
        Rank rank = rankManager.getHighestRank(player.getUniqueId());
        if (rank != null) {
            String prefix = ColorUtils.translateColorCodes(rank.getPrefix());
            sender.sendMessage(Component.text(player.getName() + "'s rank: " + rank.getName()).color(NamedTextColor.YELLOW));
            sender.sendMessage(Component.text("Prefix: " + prefix).color(NamedTextColor.GRAY));
            sender.sendMessage(Component.text("Priority: " + rank.getPriority()).color(NamedTextColor.GRAY));
        } else {
            sender.sendMessage(Component.text("Player has no rank!").color(NamedTextColor.RED));
        }
    }

    private void listRanks(CommandSender sender) {
        Set<String> rankNames = rankManager.getAllRankNames();
        if (rankNames.isEmpty()) {
            sender.sendMessage(Component.text("No ranks found!").color(NamedTextColor.RED));
            return;
        }

        sender.sendMessage(Component.text("Available ranks:").color(NamedTextColor.YELLOW));
        for (String rankName : rankNames) {
            Rank rank = rankManager.getRank(rankName);
            if (rank != null) {
                String prefix = ColorUtils.translateColorCodes(rank.getPrefix());
                sender.sendMessage(Component.text("- " + rankName + " (Priority: " + rank.getPriority() + ", Prefix: " + prefix + ")").color(NamedTextColor.GRAY));
            }
        }
    }

    private void createRank(CommandSender sender, String name, String prefix, String priorityStr) {
        try {
            int priority = Integer.parseInt(priorityStr);
            rankManager.createRank(name, prefix, "", priority);
            sender.sendMessage(Component.text("Rank created: " + name).color(NamedTextColor.GREEN));
        } catch (NumberFormatException e) {
            sender.sendMessage(Component.text("Invalid priority number!").color(NamedTextColor.RED));
        }
    }

    private void deleteRank(CommandSender sender, String name) {
        Rank rank = rankManager.getRank(name);
        if (rank == null) {
            sender.sendMessage(Component.text("Rank not found!").color(NamedTextColor.RED));
            return;
        }
        rankManager.deleteRank(name);
        sender.sendMessage(Component.text("Rank deleted: " + name).color(NamedTextColor.GREEN));
    }

    private void setRankPrefix(CommandSender sender, String name, String prefix) {
        Rank rank = rankManager.getRank(name);
        if (rank == null) {
            sender.sendMessage(Component.text("Rank not found!").color(NamedTextColor.RED));
            return;
        }
        rank.setPrefix(prefix);
        rankManager.saveRanks();
        sender.sendMessage(Component.text("Rank prefix updated!").color(NamedTextColor.GREEN));
    }

    private void setRankSuffix(CommandSender sender, String name, String suffix) {
        Rank rank = rankManager.getRank(name);
        if (rank == null) {
            sender.sendMessage(Component.text("Rank not found!").color(NamedTextColor.RED));
            return;
        }
        rank.setSuffix(suffix);
        rankManager.saveRanks();
        sender.sendMessage(Component.text("Rank suffix updated!").color(NamedTextColor.GREEN));
    }

    private void setRankPriority(CommandSender sender, String name, String priorityStr) {
        Rank rank = rankManager.getRank(name);
        if (rank == null) {
            sender.sendMessage(Component.text("Rank not found!").color(NamedTextColor.RED));
            return;
        }
        try {
            int priority = Integer.parseInt(priorityStr);
            rank.setPriority(priority);
            rankManager.saveRanks();
            sender.sendMessage(Component.text("Rank priority updated!").color(NamedTextColor.GREEN));
        } catch (NumberFormatException e) {
            sender.sendMessage(Component.text("Invalid priority number!").color(NamedTextColor.RED));
        }
    }

    private void addPermission(CommandSender sender, String name, String permission) {
        Rank rank = rankManager.getRank(name);
        if (rank == null) {
            sender.sendMessage(Component.text("Rank not found!").color(NamedTextColor.RED));
            return;
        }
        rank.addPermission(permission);
        rankManager.saveRanks();
        sender.sendMessage(Component.text("Permission added to rank!").color(NamedTextColor.GREEN));
    }

    private void removePermission(CommandSender sender, String name, String permission) {
        Rank rank = rankManager.getRank(name);
        if (rank == null) {
            sender.sendMessage(Component.text("Rank not found!").color(NamedTextColor.RED));
            return;
        }
        rank.removePermission(permission);
        rankManager.saveRanks();
        sender.sendMessage(Component.text("Permission removed from rank!").color(NamedTextColor.GREEN));
    }
}
