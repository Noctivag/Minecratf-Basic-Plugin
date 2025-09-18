package de.noctivag.plugin;

import de.noctivag.plugin.utils.ColorUtils;
import io.papermc.paper.chat.ChatRenderer;
import io.papermc.paper.event.player.AsyncChatEvent;
import net.kyori.adventure.text.Component;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;

public class PrefixListener implements Listener {
    private final HashMap<String, String> prefixMap;
    private final HashMap<String, String> nickMap;

    public PrefixListener(HashMap<String, String> prefixMap, HashMap<String, String> nickMap) {
        this.prefixMap = prefixMap;
        this.nickMap = nickMap;
    }

    @EventHandler
    public void onPlayerChat(@NotNull AsyncChatEvent event) {
        String playerName = event.getPlayer().getName();
        String prefix = prefixMap.getOrDefault(playerName, "");
        String nick = nickMap.getOrDefault(playerName, event.getPlayer().getName());

        event.renderer(ChatRenderer.viewerUnaware((source, sourceDisplayName, message) ->
            Component.empty()
                .append(ColorUtils.parseColor(prefix))
                .append(Component.space())
                .append(ColorUtils.parseColor(nick))
                .append(Component.text(": "))
                .append(message)
        ));
    }
}
