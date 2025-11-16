package de.noctivag.plugin;

import de.noctivag.plugin.data.PlayerDataManager;
import de.noctivag.plugin.managers.NametagManager;
import de.noctivag.plugin.messages.MessageManager;
import de.noctivag.plugin.utils.ColorUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class PrefixCommand implements CommandExecutor {
    private final PlayerDataManager playerDataManager;
    private final NametagManager nametagManager;
    private final MessageManager messageManager;

    public PrefixCommand(PlayerDataManager playerDataManager, NametagManager nametagManager, MessageManager messageManager) {
        this.playerDataManager = playerDataManager;
        this.nametagManager = nametagManager;
        this.messageManager = messageManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(messageManager.getError("error.players_only"));
            return true;
        }

        if (!player.hasPermission("plugin.prefix")) {
            player.sendMessage(messageManager.getError("error.no_permission"));
            return true;
        }

        if (args.length == 0) {
            messageManager.getMessageList("prefix.help").forEach(player::sendMessage);
            return true;
        }

        String prefix = String.join(" ", args);
        playerDataManager.setPrefix(player.getUniqueId().toString(), prefix);
        playerDataManager.savePlayerData(); // SOFORT SPEICHERN
        nametagManager.updateNametag(player);

        player.sendMessage(messageManager.getMessage("prefix.set").append(ColorUtils.parseColor(prefix)));
        return true;
    }
}
