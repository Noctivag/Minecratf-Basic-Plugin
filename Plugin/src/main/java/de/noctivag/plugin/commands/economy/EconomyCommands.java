package de.noctivag.plugin.commands.economy;

import de.noctivag.plugin.Plugin;
import de.noctivag.plugin.economy.EconomyManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Economy commands: balance, pay, baltop
 */
public class EconomyCommands implements CommandExecutor {
    private final Plugin plugin;
    private final EconomyManager economyManager;

    public EconomyCommands(Plugin plugin, EconomyManager economyManager) {
        this.plugin = plugin;
        this.economyManager = economyManager;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        String cmd = command.getName().toLowerCase();

        switch (cmd) {
            case "balance":
            case "bal":
                return handleBalance(sender, args);
            case "pay":
                return handlePay(sender, args);
            case "eco":
                return handleEco(sender, args);
            default:
                return false;
        }
    }

    private boolean handleBalance(CommandSender sender, String[] args) {
        if (!plugin.getConfig().getBoolean("modules.economy.enabled", false)) {
            sender.sendMessage("§cEconomy module is disabled!");
            return true;
        }

        Player target;
        if (args.length == 0) {
            if (!(sender instanceof Player)) {
                sender.sendMessage("§cYou must specify a player!");
                return true;
            }
            target = (Player) sender;
        } else {
            if (!sender.hasPermission("plugin.economy.balance.others")) {
                sender.sendMessage(plugin.getMessageManager().getMessage("no_permission"));
                return true;
            }
            target = Bukkit.getPlayer(args[0]);
            if (target == null) {
                sender.sendMessage("§cPlayer not found!");
                return true;
            }
        }

        double balance = economyManager.getBalance(target);
        sender.sendMessage("§e" + target.getName() + "'s balance: §a" + economyManager.formatBalance(balance));
        return true;
    }

    private boolean handlePay(CommandSender sender, String[] args) {
        if (!plugin.getConfig().getBoolean("modules.economy.enabled", false)) {
            sender.sendMessage("§cEconomy module is disabled!");
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use this command!");
            return true;
        }

        if (!sender.hasPermission("plugin.economy.pay")) {
            sender.sendMessage(plugin.getMessageManager().getMessage("no_permission"));
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("§cUsage: /pay <player> <amount>");
            return true;
        }

        Player player = (Player) sender;
        Player target = Bukkit.getPlayer(args[0]);
        
        if (target == null) {
            sender.sendMessage("§cPlayer not found!");
            return true;
        }

        if (target.equals(player)) {
            sender.sendMessage("§cYou cannot pay yourself!");
            return true;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[1]);
        } catch (NumberFormatException e) {
            sender.sendMessage("§cInvalid amount!");
            return true;
        }

        if (amount <= 0) {
            sender.sendMessage("§cAmount must be positive!");
            return true;
        }

        if (!economyManager.hasBalance(player, amount)) {
            sender.sendMessage("§cInsufficient funds! You need " + economyManager.formatBalance(amount));
            return true;
        }

        if (economyManager.transfer(player, target, amount)) {
            player.sendMessage("§aYou paid " + economyManager.formatBalance(amount) + " to §e" + target.getName());
            target.sendMessage("§aYou received " + economyManager.formatBalance(amount) + " from §e" + player.getName());
        } else {
            player.sendMessage("§cTransaction failed!");
        }

        return true;
    }

    private boolean handleEco(CommandSender sender, String[] args) {
        if (!sender.hasPermission("plugin.economy.admin")) {
            sender.sendMessage(plugin.getMessageManager().getMessage("no_permission"));
            return true;
        }

        if (args.length < 3) {
            sender.sendMessage("§cUsage: /eco <give|take|set> <player> <amount>");
            return true;
        }

        String action = args[0].toLowerCase();
        Player target = Bukkit.getPlayer(args[1]);
        
        if (target == null) {
            sender.sendMessage("§cPlayer not found!");
            return true;
        }

        double amount;
        try {
            amount = Double.parseDouble(args[2]);
        } catch (NumberFormatException e) {
            sender.sendMessage("§cInvalid amount!");
            return true;
        }

        switch (action) {
            case "give":
                economyManager.deposit(target, amount);
                sender.sendMessage("§aGave " + economyManager.formatBalance(amount) + " to " + target.getName());
                target.sendMessage("§aYou received " + economyManager.formatBalance(amount));
                break;
            case "take":
                economyManager.withdraw(target, amount);
                sender.sendMessage("§aTook " + economyManager.formatBalance(amount) + " from " + target.getName());
                target.sendMessage("§c" + economyManager.formatBalance(amount) + " was taken from your account");
                break;
            case "set":
                economyManager.setBalance(target, amount);
                sender.sendMessage("§aSet " + target.getName() + "'s balance to " + economyManager.formatBalance(amount));
                target.sendMessage("§aYour balance was set to " + economyManager.formatBalance(amount));
                break;
            default:
                sender.sendMessage("§cInvalid action! Use: give, take, or set");
                return true;
        }

        return true;
    }
}
