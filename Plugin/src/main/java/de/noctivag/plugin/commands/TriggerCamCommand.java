package de.noctivag.plugin.commands;

import org.bukkit.GameMode;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TriggerCamCommand implements CommandExecutor {
    private final Map<UUID, GameMode> previousGameModes;

    public TriggerCamCommand() {
        this.previousGameModes = new HashMap<>();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("§cDieser Befehl kann nur von Spielern ausgeführt werden!");
            return true;
        }

        Player player = (Player) sender;
        UUID playerId = player.getUniqueId();

        // Wenn der Spieler bereits im Spectator-Modus ist, wechsle zurück
        if (previousGameModes.containsKey(playerId)) {
            GameMode previousMode = previousGameModes.remove(playerId);
            player.setGameMode(previousMode);
            player.sendMessage("§aKamera-Modus deaktiviert. Zurück zu " + previousMode.name() + ".");
        } else {
            // Speichere den aktuellen GameMode und wechsle zu Spectator
            GameMode currentMode = player.getGameMode();
            
            // Verhindere, dass bereits im Spectator-Modus befindliche Spieler den Befehl nutzen
            if (currentMode == GameMode.SPECTATOR) {
                player.sendMessage("§cDu bist bereits im Spectator-Modus!");
                return true;
            }
            
            previousGameModes.put(playerId, currentMode);
            player.setGameMode(GameMode.SPECTATOR);
            player.sendMessage("§aKamera-Modus aktiviert. Nutze den Befehl erneut, um zurückzukehren.");
        }

        return true;
    }

    /**
     * Entfernt einen Spieler aus der Tracking-Map beim Verlassen des Servers
     * @param playerId Die UUID des Spielers
     */
    public void removePlayer(UUID playerId) {
        previousGameModes.remove(playerId);
    }

    /**
     * Stellt alle Spieler wieder her (z.B. beim Plugin-Disable)
     */
    public void restoreAllPlayers() {
        previousGameModes.clear();
    }

    /**
     * Prüft ob ein Spieler im Kamera-Modus ist
     * @param playerId Die UUID des Spielers
     * @return true wenn der Spieler im Kamera-Modus ist
     */
    public boolean isInCameraMode(UUID playerId) {
        return previousGameModes.containsKey(playerId);
    }
}
