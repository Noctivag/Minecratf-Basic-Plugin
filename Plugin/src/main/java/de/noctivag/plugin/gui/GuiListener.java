package de.noctivag.plugin.gui;

import de.noctivag.plugin.Plugin;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.ClickType;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

/**
 * Handles GUI click events
 */
public class GuiListener implements Listener {
    private final Plugin plugin;
    private final GuiManager guiManager;
    private final ChatInputManager chatInputManager;

    public GuiListener(Plugin plugin, GuiManager guiManager, ChatInputManager chatInputManager) {
        this.plugin = plugin;
        this.guiManager = guiManager;
        this.chatInputManager = chatInputManager;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String title = event.getView().getTitle();
        
        if (!title.contains("Plugin Configuration") && 
            !title.contains("Config") && 
            !title.contains("Settings")) {
            return;
        }

        event.setCancelled(true);

        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }

        Player player = (Player) event.getWhoClicked();
        ItemStack clicked = event.getCurrentItem();

        if (clicked == null || !clicked.hasItemMeta()) {
            return;
        }

        ItemMeta meta = clicked.getItemMeta();
        if (meta == null || !meta.hasDisplayName()) {
            return;
        }

        String displayName = ChatColor.stripColor(meta.getDisplayName());
        List<String> lore = meta.getLore();

        // Main menu handling
        if (title.contains("Plugin Configuration")) {
            handleMainMenuClick(player, displayName, lore, event.getClick());
        }
        // Module config handling
        else if (title.contains("Config")) {
            handleModuleConfigClick(player, displayName, lore, event.getClick());
        }
    }

    private void handleMainMenuClick(Player player, String displayName, List<String> lore, ClickType clickType) {
        // Module items
        if (displayName.contains("Basic Commands") || 
            displayName.contains("Workbenches") ||
            displayName.contains("Teleportation") ||
            displayName.contains("Economy") ||
            displayName.contains("Kit System") ||
            displayName.contains("Moderation") ||
            displayName.contains("Messaging") ||
            displayName.contains("Nametags") ||
            displayName.contains("Cosmetics") ||
            displayName.contains("Rank System") ||
            displayName.contains("Chat") ||
            displayName.contains("Tab List")) {
            
            String moduleName = displayName.replace("§l", "").trim();
            String configPath = getConfigPathFromLore(lore);

            if (clickType == ClickType.LEFT) {
                // Toggle module
                if (configPath != null) {
                    guiManager.toggleConfig(configPath);
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
                    player.sendMessage("§e" + moduleName + " §7has been " + 
                        (plugin.getConfig().getBoolean(configPath) ? "§aenabled" : "§cdisabled"));
                    guiManager.openMainMenu(player); // Refresh menu
                }
            } else if (clickType == ClickType.RIGHT) {
                // Open module config
                guiManager.openModuleConfig(player, moduleName);
                player.playSound(player.getLocation(), Sound.BLOCK_CHEST_OPEN, 1.0f, 1.0f);
            }
        }
        // Integration toggles
        else if (displayName.contains("LuckPerms") || 
                 displayName.contains("PlaceholderAPI") ||
                 displayName.contains("Vault")) {
            
            String integration = displayName.toLowerCase().replace(" ", "");
            String configPath = "integrations." + integration + ".enabled";
            
            guiManager.toggleConfig(configPath);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            player.sendMessage("§e" + displayName + " integration §7has been " + 
                (plugin.getConfig().getBoolean(configPath) ? "§aenabled" : "§cdisabled"));
            guiManager.openMainMenu(player); // Refresh menu
        }
        // Action buttons
        else if (displayName.contains("Save Configuration")) {
            guiManager.saveConfig();
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
            player.sendMessage("§a✔ Configuration saved successfully!");
        }
        else if (displayName.contains("Reload Plugin")) {
            guiManager.reloadConfig();
            player.playSound(player.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1.0f, 1.0f);
            player.sendMessage("§a✔ Plugin reloaded successfully!");
            player.closeInventory();
        }
        else if (displayName.contains("Close")) {
            player.closeInventory();
            player.playSound(player.getLocation(), Sound.BLOCK_CHEST_CLOSE, 1.0f, 1.0f);
        }
    }

    private void handleModuleConfigClick(Player player, String displayName, List<String> lore, ClickType clickType) {
        if (displayName.contains("Back to Main Menu")) {
            guiManager.openMainMenu(player);
            player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
            return;
        }

        if (displayName.contains("Save Changes")) {
            guiManager.saveConfig();
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1.0f, 1.0f);
            player.sendMessage("§a✔ Configuration saved successfully!");
            return;
        }

        // Toggle items
        String configPath = getConfigPathFromLore(lore);
        if (configPath != null) {
            if (configPath.endsWith(".enabled") || displayName.contains("Enable")) {
                guiManager.toggleConfig(configPath);
                player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1.0f, 1.0f);
                player.sendMessage("§e" + displayName + " §7has been " + 
                    (plugin.getConfig().getBoolean(configPath) ? "§aenabled" : "§cdisabled"));
                
                // Refresh the current view
                String title = player.getOpenInventory().getTitle();
                String moduleName = title.replace("§6§l", "").replace(" Config", "").trim();
                guiManager.openModuleConfig(player, moduleName);
            }
            // Value change items (numeric/text) -> start chat input handler
            else {
                String title = player.getOpenInventory().getTitle();
                String moduleName = title.replace("§6§l", "").replace(" Config", "").trim();
                Object current = plugin.getConfig().get(configPath);
                ChatInputManager.ValueType type = determineType(current);
                player.closeInventory();
                chatInputManager.startEdit(player, configPath, type, moduleName);
            }
        }
    }

    private ChatInputManager.ValueType determineType(Object current) {
        if (current instanceof Boolean) return ChatInputManager.ValueType.BOOLEAN;
        if (current instanceof Integer) return ChatInputManager.ValueType.INTEGER;
        if (current instanceof Double || current instanceof Float) return ChatInputManager.ValueType.DOUBLE;
        return ChatInputManager.ValueType.STRING;
    }

    private String getConfigPathFromLore(List<String> lore) {
        if (lore == null) return null;
        
        for (String line : lore) {
            if (line.contains("Config:")) {
                return ChatColor.stripColor(line).replace("Config:", "").trim();
            }
        }
        return null;
    }
}
