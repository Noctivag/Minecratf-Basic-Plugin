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

public class SuffixCommand implements CommandExecutor {
    private final PlayerDataManager playerDataManager;
    private final NametagManager nametagManager;

    public SuffixCommand(PlayerDataManager playerDataManager, NametagManager nametagManager) {
        this.playerDataManager = playerDataManager;
        this.nametagManager = nametagManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Nur Spieler können diesen Befehl nutzen.").color(NamedTextColor.RED));
            return true;
        }

        if (!player.hasPermission("plugin.suffix")) {
            player.sendMessage(Component.text("Du hast keine Berechtigung für diesen Befehl!").color(NamedTextColor.RED));
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(Component.text("=== Suffix-Hilfe ===").color(NamedTextColor.GOLD));
            player.sendMessage(Component.text("Unterstützte Formate:").color(NamedTextColor.YELLOW));
            player.sendMessage(Component.text("• Legacy: ").color(NamedTextColor.GRAY)
                .append(Component.text("&c[VIP]").color(NamedTextColor.WHITE)));
            player.sendMessage(Component.text("• Hex: ").color(NamedTextColor.GRAY)
                .append(Component.text("#FF0000[★] oder &#FF0000[★]").color(NamedTextColor.WHITE)));
            player.sendMessage(Component.text("• Gradient: ").color(NamedTextColor.GRAY)
                .append(Component.text("<gradient:#FF0000:#0000FF>[Premium]</gradient>").color(NamedTextColor.WHITE)));
            player.sendMessage(Component.text("• Rainbow: ").color(NamedTextColor.GRAY)
                .append(Component.text("<rainbow>[★]</rainbow>").color(NamedTextColor.WHITE)));
            player.sendMessage(Component.text("Beispiele:").color(NamedTextColor.YELLOW));
            player.sendMessage(Component.text("/suffix &a[VIP]").color(NamedTextColor.WHITE));
            player.sendMessage(Component.text("/suffix <gradient:#FF0000:#00FF00>[Premium]</gradient>").color(NamedTextColor.WHITE));
            player.sendMessage(Component.text("/suffix <rainbow>[★]</rainbow>").color(NamedTextColor.WHITE));
            return true;
        }

        String suffix = String.join(" ", args);
        playerDataManager.setSuffix(player.getName(), suffix);
        playerDataManager.savePlayerData(); // SOFORT SPEICHERN
        nametagManager.updateNametag(player);

        player.sendMessage(Component.text("Dein Suffix wurde gesetzt zu: ").color(NamedTextColor.GREEN)
            .append(ColorUtils.parseColor(suffix)));
        return true;
    }
}
