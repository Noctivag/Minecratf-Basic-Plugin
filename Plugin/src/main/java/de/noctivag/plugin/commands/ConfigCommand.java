package de.noctivag.plugin.commands;

import de.noctivag.plugin.Plugin;
import de.noctivag.plugin.gui.GuiManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Command to open the GUI configuration menu
 */
public class ConfigCommand implements CommandExecutor {
    private final Plugin plugin;
    private final GuiManager guiManager;

    public ConfigCommand(Plugin plugin, GuiManager guiManager) {
        this.plugin = plugin;
        this.guiManager = guiManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cThis command can only be used by players!");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("plugin.config")) {
            player.sendMessage(plugin.getMessageManager().getMessage("no_permission"));
            return true;
        }

        guiManager.openMainMenu(player);
        return true;
    }
}
