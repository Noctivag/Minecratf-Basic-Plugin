package de.noctivag.plugin;

import net.kyori.adventure.text.Component;
import org.bukkit.entity.Player;
import de.noctivag.plugin.utils.ColorUtils;

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
        plugin.getPrefixMap().put(player.getName(), prefix);
        updatePlayerDisplay(player);
    }

    public String getPrefix(Player player) {
        return plugin.getPrefixMap().getOrDefault(player.getName(), "");
    }

    public void removePrefix(Player player) {
        plugin.getPrefixMap().remove(player.getName());
        updatePlayerDisplay(player);
    }

    // Nickname Methoden
    public void setNickname(Player player, String nickname) {
        plugin.getNickMap().put(player.getName(), nickname);
        updatePlayerDisplay(player);
    }

    public String getNickname(Player player) {
        return plugin.getNickMap().getOrDefault(player.getName(), player.getName());
    }

    public void removeNickname(Player player) {
        plugin.getNickMap().remove(player.getName());
        updatePlayerDisplay(player);
    }

    // Join Message Methoden
    public void setJoinMessage(Player player, String message) {
        plugin.getJoinMessageManager().setCustomMessage(player.getName(), message);
    }

    public void removeJoinMessage(Player player) {
        plugin.getJoinMessageManager().removeCustomMessage(player.getName());
    }

    public void setJoinMessageEnabled(Player player, boolean enabled) {
        plugin.getJoinMessageManager().setMessageEnabled(player.getName(), enabled);
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
