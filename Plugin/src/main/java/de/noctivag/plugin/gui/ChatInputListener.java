package de.noctivag.plugin.gui;

import de.noctivag.plugin.Plugin;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.entity.Player;

public class ChatInputListener implements Listener {
    private final Plugin plugin;
    private final ChatInputManager chatInputManager;

    public ChatInputListener(Plugin plugin, ChatInputManager chatInputManager) {
        this.plugin = plugin;
        this.chatInputManager = chatInputManager;
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent event) {
        Player player = event.getPlayer();
        if (!chatInputManager.hasPending(player)) return;

        event.setCancelled(true);
        String msg = event.getMessage();
        if (msg.equalsIgnoreCase("cancel")) {
            chatInputManager.clear(player);
            player.sendMessage("§cCancelled configuration edit.");
            return;
        }

        boolean ok = chatInputManager.applyValue(player, msg);
        if (ok) {
            player.sendMessage("§a✔ Value updated and saved.");
            // Save and reopen on next tick (main thread)
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                plugin.saveConfig();
                chatInputManager.reopenModuleMenu(player);
            });
            chatInputManager.clear(player);
        }
    }
}
