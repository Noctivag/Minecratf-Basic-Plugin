package de.noctivag.plugin.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class InvseeCommand implements CommandExecutor {

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cDieser Befehl kann nur von Spielern ausgeführt werden!");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("plugin.invsee")) {
            player.sendMessage("§cDu hast keine Berechtigung für diesen Befehl!");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage("§cVerwendung: /invsee <Spieler>");
            return true;
        }

        String targetName = args[0];
        Player target = Bukkit.getPlayer(targetName);

        if (target == null) {
            player.sendMessage("§cDer Spieler §e" + targetName + " §cist nicht online!");
            return true;
        }

        // Öffne das Inventar des Zielspielers
        player.openInventory(target.getInventory());
        player.sendMessage("§aDu siehst jetzt das Inventar von §e" + target.getName() + "§a.");

        return true;
    }
}
