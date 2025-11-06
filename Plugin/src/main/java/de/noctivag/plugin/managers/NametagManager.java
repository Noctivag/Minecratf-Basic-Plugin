package de.noctivag.plugin.managers;

import de.noctivag.plugin.Plugin;
import de.noctivag.plugin.data.PlayerDataManager;
import de.noctivag.plugin.utils.ColorUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

/**
 * Manager für Nametags über Spieler-Köpfen
 * Verwendet das Scoreboard-Team-System für persistente Nametags
 */
public class NametagManager {
    private final Plugin plugin;
    private final PlayerDataManager playerDataManager;
    private Scoreboard scoreboard;

    public NametagManager(Plugin plugin) {
        this.plugin = plugin;
        this.playerDataManager = plugin.getPlayerDataManager();
        initializeScoreboard();
    }

    /**
     * Initialisiert das Scoreboard
     */
    private void initializeScoreboard() {
        scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
    }

    /**
     * Aktualisiert den Nametag eines Spielers
     */
    public void updateNametag(Player player) {
        String prefix = playerDataManager.getPrefix(player.getName());
        String suffix = playerDataManager.getSuffix(player.getName());
        String nick = playerDataManager.getNickname(player.getName());
        
        // Hole oder erstelle Team für den Spieler
        Team team = getOrCreateTeam(player);
        
        // Setze Prefix (max 64 Zeichen für Legacy-Format)
        if (prefix != null && !prefix.isEmpty()) {
            Component prefixComponent = ColorUtils.parseColor(prefix);
            team.prefix(prefixComponent);
        } else {
            team.prefix(Component.empty());
        }
        
        // Setze Suffix (max 64 Zeichen für Legacy-Format)
        if (suffix != null && !suffix.isEmpty()) {
            Component suffixComponent = ColorUtils.parseColor(suffix);
            team.suffix(suffixComponent);
        } else {
            team.suffix(Component.empty());
        }
        
        // Aktualisiere Display-Name und PlayerList-Name
        updateDisplayName(player);
    }

    /**
     * Aktualisiert den Display-Namen eines Spielers
     */
    private void updateDisplayName(Player player) {
        String prefix = playerDataManager.getPrefix(player.getName());
        String suffix = playerDataManager.getSuffix(player.getName());
        String nick = playerDataManager.getNickname(player.getName());
        String displayText = nick != null ? nick : player.getName();

        Component displayName = Component.empty();
        
        // Prefix
        if (prefix != null && !prefix.isEmpty()) {
            displayName = displayName.append(ColorUtils.parseColor(prefix)).append(Component.space());
        }
        
        // Name/Nick
        displayName = displayName.append(ColorUtils.parseColor(displayText));
        
        // Suffix
        if (suffix != null && !suffix.isEmpty()) {
            displayName = displayName.append(Component.space()).append(ColorUtils.parseColor(suffix));
        }

        player.displayName(displayName);
        player.playerListName(displayName);
    }

    /**
     * Holt oder erstellt ein Team für einen Spieler
     */
    private Team getOrCreateTeam(Player player) {
        String teamName = "nametag_" + player.getName();
        Team team = scoreboard.getTeam(teamName);
        
        if (team == null) {
            team = scoreboard.registerNewTeam(teamName);
        }
        
        // Füge Spieler zum Team hinzu falls noch nicht dabei
        if (!team.hasEntry(player.getName())) {
            team.addEntry(player.getName());
        }
        
        return team;
    }

    /**
     * Entfernt den Nametag eines Spielers
     */
    public void removeNametag(Player player) {
        String teamName = "nametag_" + player.getName();
        Team team = scoreboard.getTeam(teamName);
        
        if (team != null) {
            team.unregister();
        }
        
        // Setze Display-Name zurück
        player.displayName(Component.text(player.getName()));
        player.playerListName(Component.text(player.getName()));
    }

    /**
     * Lädt den Nametag beim Join
     */
    public void loadNametag(Player player) {
        updateNametag(player);
    }

    /**
     * Cleanup beim Quit
     */
    public void cleanup(Player player) {
        // Team bleibt bestehen für Persistenz
        // Wird beim erneuten Join wiederverwendet
    }

    /**
     * Entfernt alle Teams (für Plugin-Deaktivierung)
     */
    public void removeAllTeams() {
        for (Team team : scoreboard.getTeams()) {
            if (team.getName().startsWith("nametag_")) {
                team.unregister();
            }
        }
    }
}
