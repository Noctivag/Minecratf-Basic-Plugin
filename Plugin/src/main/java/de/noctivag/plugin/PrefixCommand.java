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

public class PrefixCommand implements CommandExecutor {
    private final PlayerDataManager playerDataManager;
    private final NametagManager nametagManager;

    public PrefixCommand(PlayerDataManager playerDataManager, NametagManager nametagManager) {
        this.playerDataManager = playerDataManager;
        this.nametagManager = nametagManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Nur Spieler können diesen Befehl nutzen.").color(NamedTextColor.RED));
            return true;
        }

        if (!player.hasPermission("plugin.prefix")) {
            player.sendMessage(Component.text("Du hast keine Berechtigung für diesen Befehl!").color(NamedTextColor.RED));
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(Component.text("=== Prefix-Hilfe ===").color(NamedTextColor.GOLD));
            player.sendMessage(Component.text("Unterstützte Formate:").color(NamedTextColor.YELLOW));
            player.sendMessage(Component.text("• Legacy: ").color(NamedTextColor.GRAY)
                .append(Component.text("&c[Admin] ").color(NamedTextColor.WHITE)));
            player.sendMessage(Component.text("• Hex: ").color(NamedTextColor.GRAY)
                .append(Component.text("#FF0000[VIP] oder &#FF0000[VIP]").color(NamedTextColor.WHITE)));
            player.sendMessage(Component.text("• Gradient: ").color(NamedTextColor.GRAY)
                .append(Component.text("<gradient:#FF0000:#0000FF>[Admin]</gradient>").color(NamedTextColor.WHITE)));
            player.sendMessage(Component.text("• Rainbow: ").color(NamedTextColor.GRAY)
                .append(Component.text("<rainbow>[VIP]</rainbow>").color(NamedTextColor.WHITE)));
            player.sendMessage(Component.text("Beispiele:").color(NamedTextColor.YELLOW));
            player.sendMessage(Component.text("/prefix &c[Admin] ").color(NamedTextColor.WHITE));
            player.sendMessage(Component.text("/prefix <gradient:#FF0000:#00FF00>[VIP]</gradient>").color(NamedTextColor.WHITE));
            player.sendMessage(Component.text("/prefix <rainbow>[★]</rainbow>").color(NamedTextColor.WHITE));
            return true;
        }

        String prefix = String.join(" ", args);
        playerDataManager.setPrefix(player.getName(), prefix);
        nametagManager.updateNametag(player);

        player.sendMessage(Component.text("Dein Prefix wurde gesetzt zu: ").color(NamedTextColor.GREEN)
            .append(ColorUtils.parseColor(prefix)));
        return true;
    }
}
