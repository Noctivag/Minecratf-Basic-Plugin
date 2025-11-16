package de.noctivag.plugin;

import de.noctivag.plugin.data.PlayerDataManager;
import de.noctivag.plugin.managers.NametagManager;
import de.noctivag.plugin.messages.MessageManager;
import de.noctivag.plugin.utils.ColorUtils;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

public class NickCommand implements CommandExecutor {
    private final PlayerDataManager playerDataManager;
    private final NametagManager nametagManager;
    private final MessageManager messageManager;

    public NickCommand(PlayerDataManager playerDataManager, NametagManager nametagManager, MessageManager messageManager) {
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

        if (!player.hasPermission("plugin.nick")) {
            player.sendMessage(messageManager.getError("error.no_permission"));
            return true;
        }

        if (args.length == 0) {
            messageManager.getMessageList("nickname.help").forEach(player::sendMessage);
            return true;
        }

        String nick = String.join(" ", args);
        playerDataManager.setNickname(player.getUniqueId().toString(), nick);
        playerDataManager.savePlayerData(); // SOFORT SPEICHERN
        nametagManager.updateNametag(player);

        player.sendMessage(messageManager.getMessage("nickname.set").append(ColorUtils.parseColor(nick)));
        return true;
    }
}
