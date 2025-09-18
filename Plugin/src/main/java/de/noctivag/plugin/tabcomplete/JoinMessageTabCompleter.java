package de.noctivag.plugin.tabcomplete;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class JoinMessageTabCompleter implements TabCompleter {

    private final List<String> SUB_COMMANDS = Arrays.asList(
        "set", "remove", "toggle", "enable", "disable", "setdefault"
    );

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            // Erste Ebene: Unterbefehle
            return filterCompletions(SUB_COMMANDS, args[0]);
        }

        if (args.length == 2) {
            // Zweite Ebene: Spielername für alle außer setdefault
            if (!args[0].equalsIgnoreCase("setdefault")) {
                return filterCompletions(
                    Bukkit.getOnlinePlayers().stream()
                        .map(Player::getName)
                        .collect(Collectors.toList()),
                    args[1]
                );
            }
        }

        if (args.length == 3 && args[0].equalsIgnoreCase("set")) {
            // Dritte Ebene: Nachrichtenvorschläge für set
            completions.addAll(Arrays.asList(
                "&7[&a+&7] &e%player%",
                "&8[&6★&8] &6%player%",
                "&#FF0000[VIP] &e%player%"
            ));
            return filterCompletions(completions, args[2]);
        }

        return completions;
    }

    private List<String> filterCompletions(List<String> completions, String input) {
        if (input.isEmpty()) return completions;

        return completions.stream()
            .filter(s -> s.toLowerCase().startsWith(input.toLowerCase()))
            .collect(Collectors.toList());
    }
}
