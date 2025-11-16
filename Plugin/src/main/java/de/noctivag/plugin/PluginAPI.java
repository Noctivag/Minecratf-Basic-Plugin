package de.noctivag.plugin;

import de.noctivag.plugin.data.PlayerDataManager;
import de.noctivag.plugin.managers.NametagManager;
import de.noctivag.plugin.utils.ColorUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;

/**
 * Öffentliche API für andere Plugins
 */
public class PluginAPI {
    private static Plugin plugin;
    private static PluginAPI instance;

    private PluginAPI(Plugin plugin) {
        PluginAPI.plugin = plugin;
    }

    public static PluginAPI getInstance() {
        if (instance == null) {
            throw new IllegalStateException("API wurde noch nicht initialisiert!");
        }
        return instance;
    }

    static void init(Plugin plugin) {
        if (instance != null) {
            throw new IllegalStateException("API wurde bereits initialisiert!");
        }
        instance = new PluginAPI(plugin);
    }

    // Prefix Methoden
    public void setPrefix(Player player, String prefix) {
        PlayerDataManager dataManager = plugin.getPlayerDataManager();
        NametagManager nametagManager = plugin.getNametagManager();

        if (dataManager != null) {
            dataManager.setPrefix(player.getUniqueId().toString(), prefix);
            dataManager.savePlayerData();
        }

        if (nametagManager != null) {
            nametagManager.updateNametag(player);
        } else {
            updatePlayerDisplay(player);
        }
    }

    public String getPrefix(Player player) {
        PlayerDataManager dataManager = plugin.getPlayerDataManager();
        if (dataManager == null) {
            return "";
        }

        String storedPrefix = dataManager.getPrefix(player.getUniqueId().toString());
        return storedPrefix != null ? storedPrefix : "";
    }

    public void removePrefix(Player player) {
        PlayerDataManager dataManager = plugin.getPlayerDataManager();
        NametagManager nametagManager = plugin.getNametagManager();

        if (dataManager != null) {
            dataManager.removePrefix(player.getUniqueId().toString());
            dataManager.savePlayerData();
        }

        if (nametagManager != null) {
            nametagManager.updateNametag(player);
        } else {
            updatePlayerDisplay(player);
        }
    }

    // Nickname Methoden
    public void setNickname(Player player, String nickname) {
        PlayerDataManager dataManager = plugin.getPlayerDataManager();
        NametagManager nametagManager = plugin.getNametagManager();

        if (dataManager != null) {
            dataManager.setNickname(player.getUniqueId().toString(), nickname);
            dataManager.savePlayerData();
        }

        if (nametagManager != null) {
            nametagManager.updateNametag(player);
        } else {
            updatePlayerDisplay(player);
        }
    }

    public String getNickname(Player player) {
        PlayerDataManager dataManager = plugin.getPlayerDataManager();
        if (dataManager == null) {
            return player.getName();
        }

        String storedNick = dataManager.getNickname(player.getUniqueId().toString());
        return storedNick != null ? storedNick : player.getName();
    }

    public void removeNickname(Player player) {
        PlayerDataManager dataManager = plugin.getPlayerDataManager();
        NametagManager nametagManager = plugin.getNametagManager();

        if (dataManager != null) {
            dataManager.removeNickname(player.getUniqueId().toString());
            dataManager.savePlayerData();
        }

        if (nametagManager != null) {
            nametagManager.updateNametag(player);
        } else {
            updatePlayerDisplay(player);
        }
    }

    // Join Message Methoden
    public void setJoinMessage(Player player, String message) {
        plugin.getJoinMessageManager().setCustomMessage(player.getUniqueId().toString(), message);
    }

    public void removeJoinMessage(Player player) {
        plugin.getJoinMessageManager().removeCustomMessage(player.getUniqueId().toString());
    }

    public void setJoinMessageEnabled(Player player, boolean enabled) {
        plugin.getJoinMessageManager().setMessageEnabled(player.getUniqueId().toString(), enabled);
    }

    // Hilfsmethoden
    private void updatePlayerDisplay(Player player) {
        String prefix = getPrefix(player);
        String nick = getNickname(player);
        Component displayName = Component.empty()
                .append(ColorUtils.parseColor(prefix))
                .append(Component.space())
                .append(ColorUtils.parseColor(nick));
        player.displayName(displayName);
        player.playerListName(displayName);
    }
}
