package de.noctivag.plugin;

import de.noctivag.plugin.data.PlayerDataManager;
import de.noctivag.plugin.managers.NametagManager;
import de.noctivag.plugin.utils.ColorUtils;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class UnNickCommand implements CommandExecutor {
    private final PlayerDataManager playerDataManager;
    private final NametagManager nametagManager;

    public UnNickCommand(PlayerDataManager playerDataManager, NametagManager nametagManager) {
        this.playerDataManager = playerDataManager;
        this.nametagManager = nametagManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Nur Spieler können diesen Befehl nutzen.").color(NamedTextColor.RED));
            return true;
        }

        if (!player.hasPermission("plugin.nick")) {
            player.sendMessage(Component.text("Du hast keine Berechtigung für diesen Befehl!").color(NamedTextColor.RED));
            return true;
        }

        playerDataManager.removeNickname(player.getName());
        playerDataManager.savePlayerData(); // SOFORT SPEICHERN
        nametagManager.updateNametag(player);
        player.sendMessage(Component.text("Dein Nickname wurde entfernt.").color(NamedTextColor.GREEN));
        return true;
    }
}
