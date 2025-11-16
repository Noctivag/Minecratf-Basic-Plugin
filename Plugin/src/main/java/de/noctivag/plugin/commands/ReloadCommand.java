package de.noctivag.plugin.commands;

import de.noctivag.plugin.Plugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class ReloadCommand implements CommandExecutor {
    private final Plugin plugin;

    public ReloadCommand(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player player) {
            if (!player.hasPermission("plugin.reload")) {
                sender.sendMessage(plugin.getMessageManager().getError("error.no_permission"));
                return true;
            }
        }

        try {
            // Reload Bukkit config (JavaPlugin)
            plugin.reloadConfig();
            // Reload our ConfigManager copy
            if (plugin.getConfigManager() != null) {
                plugin.getConfigManager().loadConfig();
            }
            // Reload join messages
            if (plugin.getJoinMessageManager() != null) {
                plugin.getJoinMessageManager().reload();
            }

            sender.sendMessage(plugin.getMessageManager().getMessage("messages.prefix") + "Konfiguration neu geladen.");
        } catch (Exception e) {
            plugin.getLogger().severe("Fehler beim Neuladen der Konfiguration: " + e.getMessage());
            sender.sendMessage(plugin.getMessageManager().getError("error.generic"));
        }
        return true;
    }
}
