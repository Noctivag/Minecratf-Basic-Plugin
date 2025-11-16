package de.noctivag.plugin.tabcomplete;

import de.noctivag.plugin.Plugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class PluginAdminTabCompleter implements TabCompleter {
    private final Plugin plugin;

    public PluginAdminTabCompleter(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender sender, @NotNull Command command, @NotNull String alias, @NotNull String[] args) {
        if (args.length == 0) {
            return List.of("get", "set");
        }
        String first = args[0].toLowerCase(Locale.ROOT);
        if (args.length == 1) {
            return filterStartsWith(List.of("get", "set"), first);
        }
        
        List<String> keys = List.of("sit-offset", "allow-on-stairs", "allow-on-slabs");
        
        if (first.equals("get") || first.equals("set")) {
            if (args.length == 2) {
                return filterStartsWith(keys, args[1].toLowerCase(Locale.ROOT));
            }
            if (first.equals("set") && args.length == 3) {
                String key = args[1].toLowerCase(Locale.ROOT);
                if (key.equals("sit-offset") || key.equals("sitoffset")) {
                    double current = plugin.getConfig().getDouble("modules.cosmetics.sit.y-offset", -1.2);
                    List<String> suggestions = new ArrayList<>(Arrays.asList("-1.4", "-1.2", "-1.1", "-1.0", "-0.9", "-0.8"));
                    suggestions.add(String.valueOf(current));
                    return filterStartsWith(suggestions, args[2].toLowerCase(Locale.ROOT));
                }
                if (key.contains("stair") || key.contains("slab")) {
                    return filterStartsWith(List.of("true", "false", "on", "off"), args[2].toLowerCase(Locale.ROOT));
                }
            }
        }
        return List.of();
    }

    private List<String> filterStartsWith(List<String> list, String prefix) {
        return list.stream().filter(s -> s.toLowerCase(Locale.ROOT).startsWith(prefix)).collect(Collectors.toList());
    }
}
