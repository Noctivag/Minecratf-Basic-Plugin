package de.noctivag.plugin.gui;

import de.noctivag.plugin.Plugin;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Manages all GUI menus for the plugin
 */
public class GuiManager {
    private final Plugin plugin;
    private final Map<String, Inventory> guiCache = new HashMap<>();

    public GuiManager(Plugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Open the main configuration menu
     */
    public void openMainMenu(Player player) {
        Inventory gui = Bukkit.createInventory(null, 54, "§6§lPlugin Configuration");

        // Module toggles
        gui.setItem(10, createModuleItem(Material.COMMAND_BLOCK, "§e§lBasic Commands",
                "modules.basic-commands.enabled", 
                "§7Heal, Feed, Fly, Gamemode, etc."));
        
        gui.setItem(11, createModuleItem(Material.CRAFTING_TABLE, "§e§lWorkbenches",
                "modules.workbenches.enabled",
                "§7Portable workbenches"));
        
        gui.setItem(12, createModuleItem(Material.ENDER_PEARL, "§e§lTeleportation",
                "modules.teleportation.enabled",
                "§7TP, TPA, Homes, Warps, Back"));
        
        gui.setItem(13, createModuleItem(Material.EMERALD, "§e§lEconomy",
                "modules.economy.enabled",
                "§7Balance, Pay, Command costs"));
        
        gui.setItem(14, createModuleItem(Material.CHEST, "§e§lKit System",
                "modules.kits.enabled",
                "§7Predefined item kits"));
        
        gui.setItem(15, createModuleItem(Material.DIAMOND_SWORD, "§e§lModeration",
                "modules.moderation.enabled",
                "§7Ban, Mute, Warn, Kick"));
        
        gui.setItem(16, createModuleItem(Material.PAPER, "§e§lMessaging",
                "modules.messaging.enabled",
                "§7Private messages, Reply, Ignore"));

        gui.setItem(19, createModuleItem(Material.NAME_TAG, "§e§lNametags",
                "modules.nametags.enabled",
                "§7Custom prefixes and suffixes"));
        
        gui.setItem(20, createModuleItem(Material.PAINTING, "§e§lCosmetics",
                "modules.cosmetics.enabled",
                "§7Sit, Camera, Vanish, Particles"));
        
        gui.setItem(21, createModuleItem(Material.BOOK, "§e§lRank System",
                "modules.ranks.enabled",
                "§7Custom ranks and permissions"));
        
        gui.setItem(22, createModuleItem(Material.WRITABLE_BOOK, "§e§lChat",
                "modules.chat.enabled",
                "§7Chat formatting and filters"));
        
        gui.setItem(23, createModuleItem(Material.BARRIER, "§e§lTab List",
                "modules.tablist.enabled",
                "§7Custom tab list formatting"));

        // Integration status
        gui.setItem(37, createInfoItem(Material.GOLD_INGOT, "§6§lLuckPerms",
                plugin.getConfig().getBoolean("integrations.luckperms.enabled", false),
                "§7Click to toggle integration"));
        
        gui.setItem(38, createInfoItem(Material.DIAMOND, "§6§lPlaceholderAPI",
                plugin.getConfig().getBoolean("integrations.placeholderapi.enabled", false),
                "§7Click to toggle integration"));
        
        gui.setItem(39, createInfoItem(Material.GOLD_BLOCK, "§6§lVault",
                plugin.getConfig().getBoolean("integrations.vault.enabled", false),
                "§7Click to toggle integration"));

        // Save & Reload buttons
        gui.setItem(49, createActionItem(Material.EMERALD_BLOCK, "§a§lSave Configuration",
                "§7Saves changes to config.yml"));
        
        gui.setItem(48, createActionItem(Material.REDSTONE_BLOCK, "§c§lReload Plugin",
                "§7Reloads all configurations"));
        
        gui.setItem(50, createActionItem(Material.BARRIER, "§c§lClose",
                "§7Close this menu"));

        player.openInventory(gui);
    }

    /**
     * Open module-specific configuration
     */
    public void openModuleConfig(Player player, String moduleName) {
        Inventory gui = Bukkit.createInventory(null, 54, "§6§l" + moduleName + " Config");

        switch (moduleName.toLowerCase()) {
            case "economy":
                setupEconomyConfig(gui);
                break;
            case "teleportation":
                setupTeleportationConfig(gui);
                break;
                        case "nametags":
                                setupNametagsConfig(gui);
                                break;
            case "kits":
                setupKitsConfig(gui);
                break;
            case "moderation":
                setupModerationConfig(gui);
                break;
            case "messaging":
                setupMessagingConfig(gui);
                break;
            case "cosmetics":
                setupCosmeticsConfig(gui);
                break;
            default:
                setupGenericConfig(gui, moduleName);
                break;
        }

        // Back button
        gui.setItem(45, createActionItem(Material.ARROW, "§e§lBack to Main Menu",
                "§7Return to main configuration"));
        
        // Save button
        gui.setItem(49, createActionItem(Material.EMERALD_BLOCK, "§a§lSave Changes",
                "§7Save configuration"));

        player.openInventory(gui);
    }

    private void setupEconomyConfig(Inventory gui) {
        gui.setItem(10, createToggleItem(Material.EMERALD, "§eEnable Economy",
                "modules.economy.enabled"));
        
        gui.setItem(11, createConfigItem(Material.GOLD_INGOT, "§eStarting Balance",
                "modules.economy.starting-balance", "1000"));
        
        gui.setItem(12, createConfigItem(Material.PAPER, "§eCurrency Symbol",
                "modules.economy.currency-symbol", "$"));
        
        gui.setItem(13, createToggleItem(Material.COMMAND_BLOCK, "§eCommand Costs",
                "modules.economy.command-costs.enabled"));
    }

    private void setupTeleportationConfig(Inventory gui) {
        gui.setItem(10, createToggleItem(Material.ENDER_PEARL, "§eEnable Teleportation",
                "modules.teleportation.enabled"));
        
        gui.setItem(11, createConfigItem(Material.CLOCK, "§eWarmup Time (seconds)",
                "modules.teleportation.warmup-time", "3"));
        
        gui.setItem(12, createToggleItem(Material.OBSIDIAN, "§eSafe Teleport",
                "modules.teleportation.safe-teleport"));
        
        gui.setItem(13, createConfigItem(Material.COMPASS, "§eMax Homes",
                "modules.teleportation.max-homes", "5"));
        
        gui.setItem(14, createToggleItem(Material.RECOVERY_COMPASS, "§eEnable /back",
                "modules.teleportation.back.enabled"));
        
        gui.setItem(15, createToggleItem(Material.MAP, "§eEnable /tprandom",
                "modules.teleportation.random.enabled"));
    }

    private void setupKitsConfig(Inventory gui) {
        gui.setItem(10, createToggleItem(Material.CHEST, "§eEnable Kits",
                "modules.kits.enabled"));
        
        gui.setItem(11, createActionItem(Material.WRITABLE_BOOK, "§eManage Kits",
                "§7Add, edit, or remove kits"));
        
        gui.setItem(12, createToggleItem(Material.CLOCK, "§eKit Cooldowns",
                "modules.kits.cooldowns.enabled"));
    }

    private void setupModerationConfig(Inventory gui) {
        gui.setItem(10, createToggleItem(Material.DIAMOND_SWORD, "§eEnable Moderation",
                "modules.moderation.enabled"));
        
        gui.setItem(11, createToggleItem(Material.BARRIER, "§eBan System",
                "modules.moderation.ban.enabled"));
        
        gui.setItem(12, createToggleItem(Material.BOOK, "§eMute System",
                "modules.moderation.mute.enabled"));
        
        gui.setItem(13, createToggleItem(Material.PAPER, "§eWarn System",
                "modules.moderation.warn.enabled"));
        
        gui.setItem(14, createConfigItem(Material.CLOCK, "§eMax Warnings",
                "modules.moderation.warn.max-warnings", "3"));
        
        gui.setItem(15, createToggleItem(Material.REDSTONE, "§eAuto-ban on Max Warnings",
                "modules.moderation.warn.auto-ban"));
    }

    private void setupMessagingConfig(Inventory gui) {
        gui.setItem(10, createToggleItem(Material.PAPER, "§eEnable Messaging",
                "modules.messaging.enabled"));
        
        gui.setItem(11, createToggleItem(Material.BELL, "§eMessage Sound",
                "modules.messaging.sound.enabled"));
        
        gui.setItem(12, createToggleItem(Material.BARRIER, "§eIgnore System",
                "modules.messaging.ignore.enabled"));
        
        gui.setItem(13, createConfigItem(Material.NAME_TAG, "§eMessage Format",
                "modules.messaging.format", "&7[&dPM&7] &f%sender% &7-> &f%receiver%: &7%message%"));
    }

    private void setupCosmeticsConfig(Inventory gui) {
        gui.setItem(10, createToggleItem(Material.ARMOR_STAND, "§eEnable Sit",
                "modules.cosmetics.sit.enabled"));
        
        gui.setItem(11, createConfigItem(Material.FEATHER, "§eSit Y-Offset",
                "modules.cosmetics.sit.y-offset", "-1.2"));
        
        gui.setItem(12, createToggleItem(Material.ENDER_EYE, "§eEnable Camera",
                "modules.cosmetics.camera.enabled"));
        
        gui.setItem(13, createToggleItem(Material.POTION, "§eEnable Vanish",
                "modules.cosmetics.vanish.enabled"));
        
        gui.setItem(14, createToggleItem(Material.GLOWSTONE_DUST, "§eParticle Effects",
                "modules.cosmetics.particles.enabled"));
    }

    private void setupNametagsConfig(Inventory gui) {
        gui.setItem(10, createToggleItem(Material.NAME_TAG, "§eEnable Nametags",
                "modules.nametags.enabled"));

        gui.setItem(11, createConfigItem(Material.LIGHT_BLUE_DYE, "§eName Color",
                "modules.nametags.name-color", "#55FF55"));

        gui.setItem(12, createToggleItem(Material.ENDER_EYE, "§eHide Vanished Nametags",
                "modules.nametags.hide-vanished-nametags"));

        gui.setItem(13, createConfigItem(Material.MAP, "§eNametag Visibility",
                "modules.nametags.nametag-visibility", "always"));

        gui.setItem(14, createConfigItem(Material.SHIELD, "§eCollision Rule",
                "modules.nametags.collision", "always"));
    }

    private void setupGenericConfig(Inventory gui, String moduleName) {
        String path = "modules." + moduleName.toLowerCase() + ".enabled";
        gui.setItem(13, createToggleItem(Material.LEVER, "§eEnable " + moduleName,
                path));
    }

    private ItemStack createModuleItem(Material material, String name, String configPath, String description) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            List<String> lore = new ArrayList<>();
            lore.add(description);
            lore.add("");
            boolean enabled = plugin.getConfig().getBoolean(configPath, false);
            lore.add(enabled ? "§a§l✔ ENABLED" : "§c§l✘ DISABLED");
            lore.add("");
            lore.add("§7Left-click: §eToggle");
            lore.add("§7Right-click: §eConfigure");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createToggleItem(Material material, String name, String configPath) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            List<String> lore = new ArrayList<>();
            boolean enabled = plugin.getConfig().getBoolean(configPath, false);
            lore.add(enabled ? "§a§l✔ ENABLED" : "§c§l✘ DISABLED");
            lore.add("");
            lore.add("§7Click to toggle");
            lore.add("§8Config: " + configPath);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createConfigItem(Material material, String name, String configPath, Object defaultValue) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            List<String> lore = new ArrayList<>();
            Object value = plugin.getConfig().get(configPath, defaultValue);
            lore.add("§7Current: §e" + value);
            lore.add("");
            lore.add("§7Click to change");
            lore.add("§8Config: " + configPath);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createInfoItem(Material material, String name, boolean enabled, String description) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            List<String> lore = new ArrayList<>();
            lore.add(description);
            lore.add("");
            lore.add(enabled ? "§a§l✔ ENABLED" : "§c§l✘ DISABLED");
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createActionItem(Material material, String name, String description) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(name);
            List<String> lore = new ArrayList<>();
            lore.add(description);
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    public void toggleConfig(String path) {
        boolean current = plugin.getConfig().getBoolean(path, false);
                plugin.getConfig().set(path, !current);
                plugin.saveConfig();
    }

    public void setConfig(String path, Object value) {
                plugin.getConfig().set(path, value);
                plugin.saveConfig();
    }

    public void saveConfig() {
        plugin.saveConfig();
    }

    public void reloadConfig() {
        plugin.reloadConfig();
    }
}
