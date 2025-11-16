package de.noctivag.plugin.listeners;

import de.noctivag.plugin.Plugin;
import de.noctivag.plugin.commands.VanishCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import java.util.UUID;

public class VanishListener implements Listener {

    private final Plugin plugin;
    private final VanishCommand vanishCommand;

    public VanishListener(Plugin plugin, VanishCommand vanishCommand) {
        this.plugin = plugin;
        this.vanishCommand = vanishCommand;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player joiningPlayer = event.getPlayer();

        // Hide other vanished players from the player who is joining
        for (UUID vanishedPlayerId : vanishCommand.getVanishedPlayers()) {
            Player vanishedPlayer = plugin.getServer().getPlayer(vanishedPlayerId);
            if (vanishedPlayer != null && !joiningPlayer.hasPermission("plugin.vanish.see")) {
                joiningPlayer.hidePlayer(plugin, vanishedPlayer);
            }
        }

        // If the joining player is supposed to be vanished, hide them from others
        if (vanishCommand.isVanished(joiningPlayer.getUniqueId())) {
            for (Player onlinePlayer : plugin.getServer().getOnlinePlayers()) {
                if (!onlinePlayer.hasPermission("plugin.vanish.see")) {
                    onlinePlayer.hidePlayer(plugin, joiningPlayer);
                }
            }
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        // No special handling needed on quit. If a player is vanished,
        // they will remain in the vanished list and be hidden on next login.
    }
}
