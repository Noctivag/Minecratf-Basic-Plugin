package de.noctivag.plugin.utils;

import org.bukkit.Bukkit;
import org.bukkit.plugin.Plugin;

/**
 * Utility class for checking compatibility with other popular plugins
 */
public class PluginCompatibility {
    
    /**
     * Check if EssentialsX is installed
     */
    public static boolean hasEssentialsX() {
        Plugin essentials = Bukkit.getPluginManager().getPlugin("Essentials");
        return essentials != null && essentials.isEnabled();
    }
    
    /**
     * Check if LuckPerms is installed
     */
    public static boolean hasLuckPerms() {
        Plugin luckPerms = Bukkit.getPluginManager().getPlugin("LuckPerms");
        return luckPerms != null && luckPerms.isEnabled();
    }
    
    /**
     * Check if Vault is installed
     */
    public static boolean hasVault() {
        Plugin vault = Bukkit.getPluginManager().getPlugin("Vault");
        return vault != null && vault.isEnabled();
    }
    
    /**
     * Check if PlaceholderAPI is installed
     */
    public static boolean hasPlaceholderAPI() {
        Plugin papi = Bukkit.getPluginManager().getPlugin("PlaceholderAPI");
        return papi != null && papi.isEnabled();
    }
    
    /**
     * Check if WorldGuard is installed
     */
    public static boolean hasWorldGuard() {
        Plugin worldGuard = Bukkit.getPluginManager().getPlugin("WorldGuard");
        return worldGuard != null && worldGuard.isEnabled();
    }
    
    /**
     * Check if GriefPrevention is installed
     */
    public static boolean hasGriefPrevention() {
        Plugin griefPrevention = Bukkit.getPluginManager().getPlugin("GriefPrevention");
        return griefPrevention != null && griefPrevention.isEnabled();
    }
    
    /**
     * Check if CMI (alternative to EssentialsX) is installed
     */
    public static boolean hasCMI() {
        Plugin cmi = Bukkit.getPluginManager().getPlugin("CMI");
        return cmi != null && cmi.isEnabled();
    }
    
    /**
     * Check if DeluxeChat is installed (chat formatting plugin)
     */
    public static boolean hasDeluxeChat() {
        Plugin deluxeChat = Bukkit.getPluginManager().getPlugin("DeluxeChat");
        return deluxeChat != null && deluxeChat.isEnabled();
    }
    
    /**
     * Check if ChatControl is installed
     */
    public static boolean hasChatControl() {
        Plugin chatControl = Bukkit.getPluginManager().getPlugin("ChatControl");
        return chatControl != null && chatControl.isEnabled();
    }
    
    /**
     * Get compatibility report for logging
     */
    public static String getCompatibilityReport() {
        StringBuilder report = new StringBuilder("Plugin Compatibility Check:\n");
        
        report.append("  - LuckPerms: ").append(hasLuckPerms() ? "✓ Found" : "✗ Not found").append("\n");
        report.append("  - EssentialsX: ").append(hasEssentialsX() ? "✓ Found" : "✗ Not found").append("\n");
        report.append("  - PlaceholderAPI: ").append(hasPlaceholderAPI() ? "✓ Found" : "✗ Not found").append("\n");
        report.append("  - Vault: ").append(hasVault() ? "✓ Found" : "✗ Not found").append("\n");
        report.append("  - WorldGuard: ").append(hasWorldGuard() ? "✓ Found" : "✗ Not found").append("\n");
        report.append("  - GriefPrevention: ").append(hasGriefPrevention() ? "✓ Found" : "✗ Not found").append("\n");
        report.append("  - CMI: ").append(hasCMI() ? "✓ Found" : "✗ Not found").append("\n");
        report.append("  - DeluxeChat: ").append(hasDeluxeChat() ? "✓ Found (disable chat.enabled)" : "✗ Not found").append("\n");
        report.append("  - ChatControl: ").append(hasChatControl() ? "✓ Found (disable chat.enabled)" : "✗ Not found");
        
        return report.toString();
    }
    
    /**
     * Check if we should warn about chat plugin conflicts
     */
    public static boolean hasChatPluginConflict() {
        return hasDeluxeChat() || hasChatControl();
    }
}
