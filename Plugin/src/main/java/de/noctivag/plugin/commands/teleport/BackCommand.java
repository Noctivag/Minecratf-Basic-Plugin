package de.noctivag.plugin.commands.teleport;

import de.noctivag.plugin.Plugin;
import de.noctivag.plugin.teleport.BackManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Command for /back - returns to previous location
 */
public class BackCommand implements CommandExecutor {
    private final Plugin plugin;
    private final BackManager backManager;

    public BackCommand(Plugin plugin, BackManager backManager) {
        this.plugin = plugin;
        this.backManager = backManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!plugin.getConfig().getBoolean("modules.teleportation.back.enabled", true)) {
            sender.sendMessage("§c/back is currently disabled!");
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use this command!");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("plugin.teleport.back")) {
            player.sendMessage(plugin.getMessageManager().getMessage("no_permission"));
            return true;
        }

        backManager.teleportBack(player);
        return true;
    }
}
