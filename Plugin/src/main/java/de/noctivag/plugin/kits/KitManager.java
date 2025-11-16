package de.noctivag.plugin.kits;

import de.noctivag.plugin.Plugin;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Level;

/**
 * Manages kits - predefined item sets
 */
public class KitManager {
    private final Plugin plugin;
    private final Map<String, Kit> kits = new HashMap<>();
    private final Map<UUID, Map<String, Long>> cooldowns = new HashMap<>();
    private File kitsFile;
    private FileConfiguration kitsConfig;

    public KitManager(Plugin plugin) {
        this.plugin = plugin;
        loadKits();
    }

    private void loadKits() {
        kitsFile = new File(plugin.getDataFolder(), "kits.yml");
        boolean isNewFile = !kitsFile.exists();
        
        if (isNewFile) {
            try {
                kitsFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().log(Level.SEVERE, "Could not create kits.yml", e);
                return;
            }
        }

        kitsConfig = YamlConfiguration.loadConfiguration(kitsFile);
        
        if (isNewFile) {
            createDefaultKits();
        }
        kits.clear();

        ConfigurationSection kitsSection = kitsConfig.getConfigurationSection("kits");
        if (kitsSection != null) {
            for (String kitName : kitsSection.getKeys(false)) {
                Kit kit = loadKit(kitName);
                if (kit != null) {
                    kits.put(kitName.toLowerCase(), kit);
                }
            }
        }
    }

    private void createDefaultKits() {
        // Starter kit
        kitsConfig.set("kits.starter.permission", "plugin.kit.starter");
        kitsConfig.set("kits.starter.cooldown", 0);
        kitsConfig.set("kits.starter.one-time", true);
        kitsConfig.set("kits.starter.items", Arrays.asList(
                "WOODEN_SWORD:1",
                "WOODEN_PICKAXE:1",
                "WOODEN_AXE:1",
                "BREAD:16"
        ));

        // Tools kit
        kitsConfig.set("kits.tools.permission", "plugin.kit.tools");
        kitsConfig.set("kits.tools.cooldown", 3600); // 1 hour
        kitsConfig.set("kits.tools.one-time", false);
        kitsConfig.set("kits.tools.items", Arrays.asList(
                "IRON_PICKAXE:1",
                "IRON_AXE:1",
                "IRON_SHOVEL:1"
        ));

        // PvP kit
        kitsConfig.set("kits.pvp.permission", "plugin.kit.pvp");
        kitsConfig.set("kits.pvp.cooldown", 7200); // 2 hours
        kitsConfig.set("kits.pvp.one-time", false);
        kitsConfig.set("kits.pvp.items", Arrays.asList(
                "DIAMOND_SWORD:1",
                "DIAMOND_HELMET:1",
                "DIAMOND_CHESTPLATE:1",
                "DIAMOND_LEGGINGS:1",
                "DIAMOND_BOOTS:1",
                "GOLDEN_APPLE:16"
        ));

        saveKits();
    }

    private Kit loadKit(String name) {
        String basePath = "kits." + name;
        
        String permission = kitsConfig.getString(basePath + ".permission");
        int cooldown = kitsConfig.getInt(basePath + ".cooldown", 0);
        boolean oneTime = kitsConfig.getBoolean(basePath + ".one-time", false);
        List<String> itemStrings = kitsConfig.getStringList(basePath + ".items");

        List<ItemStack> items = new ArrayList<>();
        for (String itemStr : itemStrings) {
            ItemStack item = parseItemString(itemStr);
            if (item != null) {
                items.add(item);
            }
        }

        return new Kit(name, permission, cooldown, oneTime, items);
    }

    private ItemStack parseItemString(String str) {
        String[] parts = str.split(":");
        if (parts.length < 1) return null;

        Material material = Material.matchMaterial(parts[0]);
        if (material == null) return null;

        int amount = parts.length > 1 ? Integer.parseInt(parts[1]) : 1;
        
        return new ItemStack(material, amount);
    }

    public void saveKits() {
        try {
            kitsConfig.save(kitsFile);
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Could not save kits.yml", e);
        }
    }

    public Kit getKit(String name) {
        return kits.get(name.toLowerCase());
    }

    public Collection<Kit> getAllKits() {
        return kits.values();
    }

    public boolean giveKit(Player player, String kitName) {
        if (!plugin.getConfig().getBoolean("modules.kits.enabled", true)) {
            player.sendMessage("§cKits are currently disabled!");
            return false;
        }

        Kit kit = getKit(kitName);
        if (kit == null) {
            player.sendMessage("§cKit not found!");
            return false;
        }

        if (kit.getPermission() != null && !player.hasPermission(kit.getPermission())) {
            player.sendMessage("§cYou don't have permission for this kit!");
            return false;
        }

        if (kit.isOneTime() && hasClaimedKit(player, kitName)) {
            player.sendMessage("§cYou have already claimed this kit!");
            return false;
        }

        if (plugin.getConfig().getBoolean("modules.kits.cooldowns.enabled", true)) {
            long cooldownRemaining = getCooldownRemaining(player, kitName);
            if (cooldownRemaining > 0) {
                player.sendMessage("§cKit on cooldown! Wait " + formatTime(cooldownRemaining));
                return false;
            }
        }

        // Give items
        for (ItemStack item : kit.getItems()) {
            player.getInventory().addItem(item.clone());
        }

        player.sendMessage("§aYou received the §e" + kitName + "§a kit!");

        // Set cooldown
        if (kit.getCooldown() > 0) {
            setCooldown(player, kitName, kit.getCooldown());
        }

        return true;
    }

    private long getCooldownRemaining(Player player, String kitName) {
        Map<String, Long> playerCooldowns = cooldowns.get(player.getUniqueId());
        if (playerCooldowns == null) return 0;

        Long cooldownEnd = playerCooldowns.get(kitName.toLowerCase());
        if (cooldownEnd == null) return 0;

        long remaining = cooldownEnd - System.currentTimeMillis();
        return remaining > 0 ? remaining : 0;
    }

    private void setCooldown(Player player, String kitName, int seconds) {
        Map<String, Long> playerCooldowns = cooldowns.computeIfAbsent(player.getUniqueId(), k -> new HashMap<>());
        playerCooldowns.put(kitName.toLowerCase(), System.currentTimeMillis() + (seconds * 1000L));
    }

    private boolean hasClaimedKit(Player player, String kitName) {
        // Check persistent storage for one-time kits
        String path = "claimed." + player.getUniqueId() + "." + kitName.toLowerCase();
        return kitsConfig.getBoolean(path, false);
    }

    private String formatTime(long milliseconds) {
        long seconds = milliseconds / 1000;
        long minutes = seconds / 60;
        long hours = minutes / 60;

        if (hours > 0) {
            return hours + "h " + (minutes % 60) + "m";
        } else if (minutes > 0) {
            return minutes + "m " + (seconds % 60) + "s";
        } else {
            return seconds + "s";
        }
    }

    public void reload() {
        loadKits();
    }

    /**
     * Inner class representing a kit
     */
    public static class Kit {
        private final String name;
        private final String permission;
        private final int cooldown;
        private final boolean oneTime;
        private final List<ItemStack> items;

        public Kit(String name, String permission, int cooldown, boolean oneTime, List<ItemStack> items) {
            this.name = name;
            this.permission = permission;
            this.cooldown = cooldown;
            this.oneTime = oneTime;
            this.items = items;
        }

        public String getName() { return name; }
        public String getPermission() { return permission; }
        public int getCooldown() { return cooldown; }
        public boolean isOneTime() { return oneTime; }
        public List<ItemStack> getItems() { return items; }
    }
}
