package de.noctivag.plugin.commands;

import de.noctivag.plugin.messages.MessageManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

public class SkullCommand implements CommandExecutor {
    
    private final MessageManager messageManager;

    public SkullCommand(MessageManager messageManager) {
        this.messageManager = messageManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(messageManager.getMessage("general.player-only"));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("plugin.skull")) {
            player.sendMessage(messageManager.getMessage("general.no-permission"));
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(messageManager.getMessage("skull.usage"));
            return true;
        }

        String targetName = args[0];
        
        // Get the player (online or offline)
        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);

        // Create player head
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();
        
        if (meta != null) {
            meta.setOwningPlayer(target);
            skull.setItemMeta(meta);
        }

        // Give to player
        player.getInventory().addItem(skull);
        player.sendMessage(messageManager.getMessage("skull.received", targetName));

        return true;
    }
}
