package de.noctivag.plugin.commands.kits;

import de.noctivag.plugin.Plugin;
import de.noctivag.plugin.kits.KitManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Command for /kit and /kits
 */
public class KitCommand implements CommandExecutor {
    private final Plugin plugin;
    private final KitManager kitManager;

    public KitCommand(Plugin plugin, KitManager kitManager) {
        this.plugin = plugin;
        this.kitManager = kitManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!plugin.getConfig().getBoolean("modules.kits.enabled", true)) {
            sender.sendMessage("§cKits are currently disabled!");
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use this command!");
            return true;
        }

        Player player = (Player) sender;

        // /kits - list all kits
        if (command.getName().equalsIgnoreCase("kits") || args.length == 0) {
            if (!player.hasPermission("plugin.kit.list")) {
                player.sendMessage(plugin.getMessageManager().getMessage("no_permission"));
                return true;
            }

            player.sendMessage("§6§l=== Available Kits ===");
            for (KitManager.Kit kit : kitManager.getAllKits()) {
                String status = "§a✔";
                if (kit.getPermission() != null && !player.hasPermission(kit.getPermission())) {
                    status = "§c✘";
                }
                player.sendMessage("§e" + kit.getName() + " " + status + 
                        " §7- " + kit.getItems().size() + " items" +
                        (kit.getCooldown() > 0 ? " §7(Cooldown: " + kit.getCooldown() + "s)" : ""));
            }
            return true;
        }

        // /kit <name> - claim a kit
        if (!player.hasPermission("plugin.kit.use")) {
            player.sendMessage(plugin.getMessageManager().getMessage("no_permission"));
            return true;
        }

        String kitName = args[0];
        kitManager.giveKit(player, kitName);

        return true;
    }
}
