package de.noctivag.plugin.commands;

import de.noctivag.plugin.managers.SitManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class TriggerSitCommand implements CommandExecutor {
    private final SitManager sitManager;

    public TriggerSitCommand(SitManager sitManager) {
        this.sitManager = sitManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cDieser Befehl kann nur von Spielern ausgeführt werden!");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("plugin.sit")) {
            player.sendMessage("§cDu hast keine Berechtigung für diesen Befehl!");
            return true;
        }

        // Wenn der Spieler bereits sitzt, stehe auf
        if (sitManager.isSitting(player)) {
            if (sitManager.unsitPlayer(player)) {
                player.sendMessage("§aDu stehst nun auf.");
            }
        } else {
            // Ansonsten setze dich hin
            if (sitManager.sitPlayer(player)) {
                player.sendMessage("§aDu sitzt nun.");
            } else {
                player.sendMessage("§cDu kannst dich hier nicht hinsetzen.");
            }
        }

        return true;
    }
}
