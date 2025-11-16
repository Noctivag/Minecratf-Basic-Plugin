package de.noctivag.plugin.managers;

import de.noctivag.plugin.Plugin;
import de.noctivag.plugin.data.PlayerDataManager;
import de.noctivag.plugin.utils.ColorUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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
        String prefix = playerDataManager.getPrefix(player.getUniqueId().toString());
        String suffix = playerDataManager.getSuffix(player.getUniqueId().toString());

        // Check if LuckPerms integration should override
        if (plugin.getLuckPermsHook() != null && plugin.getLuckPermsHook().shouldSyncDisplayNames()) {
            String lpPrefix = plugin.getLuckPermsHook().getPrefix(player);
            String lpSuffix = plugin.getLuckPermsHook().getSuffix(player);

            if (lpPrefix != null) prefix = lpPrefix;
            if (lpSuffix != null) suffix = lpSuffix;
        }

        // Hole oder erstelle Team für den Spieler
        Team team = getOrCreateTeam(player);
        
        if (team == null) {
            // Could not create team, possibly conflict with another plugin
            return;
        }

        // Team options (visibility/collision) and coloring
        applyTeamOptions(player, team);

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

        // PERFORMANCE FIX: Only update the player's own scoreboard if needed
        // All players already see the main scoreboard, no need to reset it for everyone
        if (player.getScoreboard() != scoreboard) {
            player.setScoreboard(scoreboard);
        }
    }

    /**
     * Aktualisiert den Display-Namen eines Spielers
     */
    private void updateDisplayName(Player player) {
        String prefix = playerDataManager.getPrefix(player.getUniqueId().toString());
        String suffix = playerDataManager.getSuffix(player.getUniqueId().toString());
        String nick = playerDataManager.getNickname(player.getUniqueId().toString());

        // Check if LuckPerms integration should override
        if (plugin.getLuckPermsHook() != null && plugin.getLuckPermsHook().shouldSyncDisplayNames()) {
            String lpPrefix = plugin.getLuckPermsHook().getPrefix(player);
            String lpSuffix = plugin.getLuckPermsHook().getSuffix(player);

            if (lpPrefix != null) prefix = lpPrefix;
            if (lpSuffix != null) suffix = lpSuffix;
        }

        String displayText = nick != null ? nick : player.getName();

        // Optionale Einfärbung des Namens (nicht Prefix/Suffix)
        String nameColor = plugin.getConfig().getString("modules.nametags.name-color", "");

        Component displayName = Component.empty();

        // Prefix
        if (prefix != null && !prefix.isEmpty()) {
            displayName = displayName.append(ColorUtils.parseColor(prefix)).append(Component.space());
        }

        // Name/Nick
        if (nameColor != null && !nameColor.trim().isEmpty()) {
            displayName = displayName.append(ColorUtils.parseColor(nameColor + displayText));
        } else {
            displayName = displayName.append(ColorUtils.parseColor(displayText));
        }

        // Suffix
        if (suffix != null && !suffix.isEmpty()) {
            displayName = displayName.append(Component.space()).append(ColorUtils.parseColor(suffix));
        }

        player.displayName(displayName);
        player.playerListName(displayName);
    }

    private void applyTeamOptions(Player player, Team team) {
        // Hide nametag for vanished players if enabled
        boolean hideVanished = plugin.getConfig().getBoolean("modules.nametags.hide-vanished-nametags", true);
        if (hideVanished && plugin.getTriggerCamCommand() != null) {
            // Try to obtain vanish state via Plugin's VanishCommand if available
            de.noctivag.plugin.commands.VanishCommand vc = plugin.getVanishCommand();
            if (vc != null && vc.isVanished(player.getUniqueId())) {
                team.setOption(Team.Option.NAME_TAG_VISIBILITY, Team.OptionStatus.NEVER);
            } else {
                // Apply configured visibility
                team.setOption(Team.Option.NAME_TAG_VISIBILITY, mapVisibility(plugin.getConfig().getString("modules.nametags.nametag-visibility", "always")));
            }
        } else {
            team.setOption(Team.Option.NAME_TAG_VISIBILITY, mapVisibility(plugin.getConfig().getString("modules.nametags.nametag-visibility", "always")));
        }

        // Collision rule
        team.setOption(Team.Option.COLLISION_RULE, mapCollision(plugin.getConfig().getString("modules.nametags.collision", "always")));

        // Team base color (affects simple name color). For hex, approximate to nearest legacy if possible.
        String nameColor = plugin.getConfig().getString("modules.nametags.name-color", "");
        ChatColor chatColor = mapToChatColor(nameColor);
        if (chatColor != null) {
            try {
                team.setColor(chatColor);
            } catch (NoSuchMethodError ignored) {
                // Older API: ignore
            }
        }
    }

    private Team.OptionStatus mapVisibility(String v) {
        if (v == null) return Team.OptionStatus.ALWAYS;
        v = v.toLowerCase();
        return switch (v) {
            case "never" -> Team.OptionStatus.NEVER;
            case "hide-for-other-teams", "hide_for_other_teams" -> Team.OptionStatus.FOR_OTHER_TEAMS;
            default -> Team.OptionStatus.ALWAYS;
        };
    }

    private Team.OptionStatus mapCollision(String v) {
        if (v == null) return Team.OptionStatus.ALWAYS;
        v = v.toLowerCase();
        return switch (v) {
            case "never" -> Team.OptionStatus.NEVER;
            case "push-other-teams", "push_for_other_teams", "other-teams" -> Team.OptionStatus.FOR_OTHER_TEAMS;
            default -> Team.OptionStatus.ALWAYS;
        };
    }

    private ChatColor mapToChatColor(String col) {
        if (col == null) return null;
        col = col.trim();
        if (col.isEmpty()) return null;
        // Legacy code like &a
        if (col.startsWith("&") && col.length() >= 2) {
            char code = Character.toLowerCase(col.charAt(1));
            return switch (code) {
                case '0' -> ChatColor.BLACK;
                case '1' -> ChatColor.DARK_BLUE;
                case '2' -> ChatColor.DARK_GREEN;
                case '3' -> ChatColor.DARK_AQUA;
                case '4' -> ChatColor.DARK_RED;
                case '5' -> ChatColor.DARK_PURPLE;
                case '6' -> ChatColor.GOLD;
                case '7' -> ChatColor.GRAY;
                case '8' -> ChatColor.DARK_GRAY;
                case '9' -> ChatColor.BLUE;
                case 'a' -> ChatColor.GREEN;
                case 'b' -> ChatColor.AQUA;
                case 'c' -> ChatColor.RED;
                case 'd' -> ChatColor.LIGHT_PURPLE;
                case 'e' -> ChatColor.YELLOW;
                case 'f' -> ChatColor.WHITE;
                default -> null;
            };
        }
        // Hex #RRGGBB -> nearest approximate (simple map)
        if (col.startsWith("#") && col.length() == 7) {
            try {
                int rgb = Integer.parseInt(col.substring(1), 16);
                // Simple nearest mapping to 16 colors
                return nearestChatColor(rgb);
            } catch (NumberFormatException ignored) {}
        }
        return null;
    }

    private ChatColor nearestChatColor(int rgb) {
        // Basic palette mapping
        ChatColor[] palette = {
            ChatColor.BLACK, ChatColor.DARK_BLUE, ChatColor.DARK_GREEN, ChatColor.DARK_AQUA,
            ChatColor.DARK_RED, ChatColor.DARK_PURPLE, ChatColor.GOLD, ChatColor.GRAY,
            ChatColor.DARK_GRAY, ChatColor.BLUE, ChatColor.GREEN, ChatColor.AQUA,
            ChatColor.RED, ChatColor.LIGHT_PURPLE, ChatColor.YELLOW, ChatColor.WHITE
        };
        int[][] paletteRGB = {
            {0x00,0x00,0x00},{0x00,0x00,0xAA},{0x00,0xAA,0x00},{0x00,0xAA,0xAA},
            {0xAA,0x00,0x00},{0xAA,0x00,0xAA},{0xFF,0xAA,0x00},{0xAA,0xAA,0xAA},
            {0x55,0x55,0x55},{0x55,0x55,0xFF},{0x55,0xFF,0x55},{0x55,0xFF,0xFF},
            {0xFF,0x55,0x55},{0xFF,0x55,0xFF},{0xFF,0xFF,0x55},{0xFF,0xFF,0xFF}
        };
        int r = (rgb >> 16) & 0xFF; int g = (rgb >> 8) & 0xFF; int b = rgb & 0xFF;
        int best = 0; double bestDist = Double.MAX_VALUE;
        for (int i = 0; i < paletteRGB.length; i++) {
            int dr = r - paletteRGB[i][0];
            int dg = g - paletteRGB[i][1];
            int db = b - paletteRGB[i][2];
            double d = dr*dr + dg*dg + db*db;
            if (d < bestDist) { bestDist = d; best = i; }
        }
        return palette[best];
    }

    /**
     * Holt oder erstellt ein Team für einen Spieler
     */
    private Team getOrCreateTeam(Player player) {
        String teamName = "nametag_" + player.getName();
        Team team = scoreboard.getTeam(teamName);
        
        if (team == null) {
            try {
                team = scoreboard.registerNewTeam(teamName);
            } catch (IllegalArgumentException e) {
                // Team already exists or invalid name
                plugin.getLogger().warning("Could not create team for " + player.getName() + ": " + e.getMessage());
                team = scoreboard.getTeam(teamName);
                if (team == null) {
                    return null; // Failed to create/get team
                }
            }
        }
        
        // Check if player is in another team (from another plugin)
        Team existingTeam = scoreboard.getEntryTeam(player.getName());
        if (existingTeam != null && !existingTeam.getName().equals(teamName)) {
            // Player is in another plugin's team - don't override unless configured
            if (!plugin.getConfig().getBoolean("nametags.override-other-teams", false)) {
                plugin.getLogger().fine("Player " + player.getName() + " is in team " + 
                    existingTeam.getName() + ", not overriding (set nametags.override-other-teams to true to change)");
                return existingTeam; // Use their team instead
            }
            existingTeam.removeEntry(player.getName());
        }
        
        // Füge Spieler zum Team hinzu falls noch nicht dabei
        if (team != null && !team.hasEntry(player.getName())) {
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
