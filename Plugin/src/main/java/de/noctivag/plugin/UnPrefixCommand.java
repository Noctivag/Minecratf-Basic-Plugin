package de.noctivag.plugin;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import java.util.HashMap;

public class UnPrefixCommand implements CommandExecutor {
    private final HashMap<String, String> prefixMap;
    private final HashMap<String, String> nickMap;

    public UnPrefixCommand(HashMap<String, String> prefixMap, HashMap<String, String> nickMap) {
        this.prefixMap = prefixMap;
        this.nickMap = nickMap;
    }

    private void updateDisplayName(@NotNull Player player) {
        String nick = nickMap.getOrDefault(player.getName(), player.getName());
        Component displayName = Component.empty()
                .append(Component.text(nick));

        player.displayName(displayName);
        player.playerListName(displayName);
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Nur Spieler können diesen Befehl nutzen.").color(NamedTextColor.RED));
            return true;
        }

        prefixMap.remove(player.getName());
        updateDisplayName(player);
        player.sendMessage(Component.text("Dein Prefix wurde entfernt.").color(NamedTextColor.GREEN));
        return true;
    }
}
