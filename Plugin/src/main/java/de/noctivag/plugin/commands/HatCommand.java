package de.noctivag.plugin.commands;

import de.noctivag.plugin.messages.MessageManager;
import org.bukkit.Material;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;

public class HatCommand implements CommandExecutor {
    
    private final MessageManager messageManager;

    public HatCommand(MessageManager messageManager) {
        this.messageManager = messageManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(messageManager.getMessage("general.player-only"));
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("plugin.hat")) {
            player.sendMessage(messageManager.getMessage("general.no-permission"));
            return true;
        }

        PlayerInventory inv = player.getInventory();
        ItemStack handItem = inv.getItemInMainHand();

        if (handItem.getType() == Material.AIR) {
            player.sendMessage(messageManager.getMessage("hat.no-item"));
            return true;
        }

        ItemStack currentHelmet = inv.getHelmet();
        
        // Set the item in hand as helmet
        ItemStack hatItem = handItem.clone();
        inv.setHelmet(hatItem);
        
        // Give back the old helmet to hand (or add to inventory if possible)
        if (currentHelmet != null && currentHelmet.getType() != Material.AIR) {
            inv.setItemInMainHand(currentHelmet);
            player.sendMessage(messageManager.getMessage("hat.swapped"));
        } else {
            inv.setItemInMainHand(new ItemStack(Material.AIR));
            player.sendMessage(messageManager.getMessage("hat.equipped", handItem.getType().name()));
        }

        return true;
    }
}
