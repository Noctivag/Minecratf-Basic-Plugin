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

public class NickCommand implements CommandExecutor {
    private final HashMap<String, String> nickMap;
    private final HashMap<String, String> prefixMap;

    public NickCommand(HashMap<String, String> nickMap, HashMap<String, String> prefixMap) {
        this.nickMap = nickMap;
        this.prefixMap = prefixMap;
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
            player.sendMessage(Component.text("=== Nickname-Hilfe ===").color(NamedTextColor.GOLD));
            player.sendMessage(Component.text("Beispiele:").color(NamedTextColor.YELLOW));
            player.sendMessage(Component.text("/nick #FF0000Spieler ").color(NamedTextColor.WHITE)
                .append(Component.text("- Einfarbig").color(NamedTextColor.GRAY)));
            player.sendMessage(Component.text("/nick gradient:#FF0000:#00FF00:Spieler ").color(NamedTextColor.WHITE)
                .append(Component.text("- Farbverlauf").color(NamedTextColor.GRAY)));
            player.sendMessage(Component.text("/nick multi:#FF0000:#00FF00:#0000FF:Spieler ").color(NamedTextColor.WHITE)
                .append(Component.text("- Mehrere Farben").color(NamedTextColor.GRAY)));
            return true;
        }

        String nick = String.join(" ", args);
        nickMap.put(player.getName(), nick);
        updateDisplayName(player);

        player.sendMessage(Component.text("Dein Nickname wurde gesetzt zu: ").color(NamedTextColor.GREEN)
            .append(ColorUtils.parseColor(nick)));
        return true;
    }
}
