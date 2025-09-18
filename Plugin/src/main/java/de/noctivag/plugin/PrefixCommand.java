package de.noctivag.plugin;

import de.noctivag.plugin.utils.ColorUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class PrefixCommand implements CommandExecutor {
    private final HashMap<String, String> prefixMap;
    private final HashMap<String, String> nickMap;

    public PrefixCommand(HashMap<String, String> prefixMap, HashMap<String, String> nickMap) {
        this.prefixMap = prefixMap;
        this.nickMap = nickMap;
    }

    private void updateDisplayName(@NotNull Player player) {
        String prefix = prefixMap.getOrDefault(player.getName(), "");
        String nick = nickMap.getOrDefault(player.getName(), player.getName());

        Component displayName = Component.empty()
            .append(ColorUtils.parseColor(prefix))
            .append(Component.space())
            .append(ColorUtils.parseColor(nick));

        player.displayName(displayName);
        player.playerListName(displayName);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Nur Spieler können diesen Befehl nutzen.").color(NamedTextColor.RED));
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(Component.text("=== Prefix-Hilfe ===").color(NamedTextColor.GOLD));
            player.sendMessage(Component.text("Beispiele:").color(NamedTextColor.YELLOW));
            player.sendMessage(Component.text("/prefix #FF0000[Admin] ").color(NamedTextColor.WHITE)
                .append(Component.text("- Einfarbig").color(NamedTextColor.GRAY)));
            player.sendMessage(Component.text("/prefix gradient:#FF0000:#00FF00:[Admin] ").color(NamedTextColor.WHITE)
                .append(Component.text("- Farbverlauf").color(NamedTextColor.GRAY)));
            player.sendMessage(Component.text("/prefix multi:#FF0000:#00FF00:#0000FF:[Admin] ").color(NamedTextColor.WHITE)
                .append(Component.text("- Mehrere Farben").color(NamedTextColor.GRAY)));
            return true;
        }

        String prefix = String.join(" ", args);
        prefixMap.put(player.getName(), prefix);
        updateDisplayName(player);

        player.sendMessage(Component.text("Dein Prefix wurde gesetzt zu: ").color(NamedTextColor.GREEN)
            .append(ColorUtils.parseColor(prefix)));
        return true;
    }
}
