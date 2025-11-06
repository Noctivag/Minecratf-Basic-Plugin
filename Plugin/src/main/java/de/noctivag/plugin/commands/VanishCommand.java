package de.noctivag.plugin.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class VanishCommand implements CommandExecutor {
    private final Set<UUID> vanishedPlayers = new HashSet<>();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cDieser Befehl kann nur von Spielern ausgeführt werden!");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("plugin.vanish")) {
            player.sendMessage("§cDu hast keine Berechtigung für diesen Befehl!");
            return true;
        }

        UUID playerId = player.getUniqueId();

        if (vanishedPlayers.contains(playerId)) {
            // Spieler ist unsichtbar -> sichtbar machen
            vanishedPlayers.remove(playerId);
            
            // Für alle Spieler wieder sichtbar machen
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                onlinePlayer.showPlayer(player.getServer().getPluginManager().getPlugin("Plugin"), player);
            }
            
            player.sendMessage("§aDu bist jetzt §lsichtbar§a!");
        } else {
            // Spieler ist sichtbar -> unsichtbar machen
            vanishedPlayers.add(playerId);
            
            // Für alle Spieler unsichtbar machen (außer für andere Admins mit Vanish-Permission)
            for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
                if (!onlinePlayer.hasPermission("plugin.vanish.see") && !onlinePlayer.equals(player)) {
                    onlinePlayer.hidePlayer(player.getServer().getPluginManager().getPlugin("Plugin"), player);
                }
            }
            
            player.sendMessage("§aDu bist jetzt §lunsichtbar§a!");
            player.sendMessage("§7Andere Admins können dich weiterhin sehen.");
        }

        return true;
    }

    public boolean isVanished(UUID playerId) {
        return vanishedPlayers.contains(playerId);
    }

    public void removeVanish(UUID playerId) {
        vanishedPlayers.remove(playerId);
    }

    public Set<UUID> getVanishedPlayers() {
        return new HashSet<>(vanishedPlayers);
    }
}
