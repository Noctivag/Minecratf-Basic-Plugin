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

public class GlobalTabCompleter implements TabCompleter {
    private final List<String> BASIC_COMMANDS = Arrays.asList(
        "heal", "feed", "fly", "gmc", "gms", "gmsp", "ci", "clearinventory"
    );

    private final List<String> WORKBENCH_COMMANDS = Arrays.asList(
        "craftingtable", "anvil", "enderchest", "grindstone",
        "smithingtable", "stonecutter", "loom", "cartography"
    );

    @Override
    public List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, String[] args) {
        List<String> completions = new ArrayList<>();

        String cmdName = command.getName().toLowerCase();

        // Basic Commands ohne Parameter
        if (BASIC_COMMANDS.contains(cmdName)) {
            return completions; // Leere Liste, da keine Parameter benötigt
        }

        // Workbench Commands ohne Parameter
        if (WORKBENCH_COMMANDS.contains(cmdName)) {
            return completions; // Leere Liste, da keine Parameter benötigt
        }

        // Prefix Command
        if (cmdName.equals("prefix")) {
            if (args.length == 1) {
                completions.add("<prefix>");
                completions.add("&#FF0000[Admin]"); // Beispiel-Prefix
                completions.add("&#00FF00[VIP]");   // Beispiel-Prefix
            }
            return filterCompletions(completions, args[args.length - 1]);
        }

        // Nick Command
        if (cmdName.equals("nick")) {
            if (args.length == 1) {
                completions.add("<nickname>");
                completions.addAll(Bukkit.getOnlinePlayers().stream()
                    .map(Player::getName)
                    .collect(Collectors.toList()));
            }
            return filterCompletions(completions, args[args.length - 1]);
        }

        // Menu Command
        if (cmdName.equals("menu")) {
            if (args.length == 1) {
                completions.addAll(Arrays.asList(
                    "cosmetics", "events", "warps", "settings"
                ));
            }
            return filterCompletions(completions, args[args.length - 1]);
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
