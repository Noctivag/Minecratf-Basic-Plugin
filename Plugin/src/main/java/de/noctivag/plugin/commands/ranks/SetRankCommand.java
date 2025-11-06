package de.noctivag.plugin.commands.ranks;

import de.noctivag.plugin.Plugin;
import de.noctivag.plugin.permissions.Rank;
import de.noctivag.plugin.permissions.RankManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class SetRankCommand implements CommandExecutor {
    private final RankManager rankManager;

    public SetRankCommand(Plugin plugin) {
        this.rankManager = plugin.getRankManager();
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!sender.hasPermission("rank.admin")) {
            sender.sendMessage(Component.text("You don't have permission!").color(NamedTextColor.RED));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(Component.text("Usage: /setrank <player> <rank>").color(NamedTextColor.RED));
            return true;
        }

        Player target = Bukkit.getPlayer(args[0]);
        if (target == null) {
            sender.sendMessage(Component.text("Player not found!").color(NamedTextColor.RED));
            return true;
        }

        String rankName = args[1].toLowerCase();
        Rank rank = rankManager.getRank(rankName);
        if (rank == null) {
            sender.sendMessage(Component.text("Rank not found!").color(NamedTextColor.RED));
            return true;
        }

        rankManager.setPlayerRank(target.getUniqueId(), rankName);
        sender.sendMessage(Component.text("Set " + target.getName() + "'s rank to " + rankName).color(NamedTextColor.GREEN));
        target.sendMessage(Component.text("Your rank has been set to " + rankName).color(NamedTextColor.GREEN));

        return true;
    }
}
