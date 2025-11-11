package de.noctivag.plugin.modules;

import de.noctivag.plugin.Plugin;
import org.bukkit.configuration.ConfigurationSection;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages plugin modules and their enabled/disabled state
 * Provides centralized access to module configuration
 */
public class ModuleManager {
    private final Plugin plugin;
    private final Map<String, Boolean> moduleStates;

    public ModuleManager(Plugin plugin) {
        this.plugin = plugin;
        this.moduleStates = new HashMap<>();
        loadModuleStates();
    }

    private void loadModuleStates() {
        ConfigurationSection modules = plugin.getConfig().getConfigurationSection("modules");
        if (modules == null) {
            plugin.getLogger().warning("No modules section found in config.yml! All modules will be enabled by default.");
            return;
        }

        // Load all module states recursively
        loadModuleStatesRecursive("modules", modules);
    }

    private void loadModuleStatesRecursive(String path, ConfigurationSection section) {
        for (String key : section.getKeys(false)) {
            String fullPath = path + "." + key;

            if (section.isConfigurationSection(key)) {
                ConfigurationSection subSection = section.getConfigurationSection(key);
                if (subSection != null) {
                    // Check if this section has an 'enabled' key
                    if (subSection.contains("enabled")) {
                        boolean enabled = subSection.getBoolean("enabled", true);
                        moduleStates.put(fullPath, enabled);

                        if (plugin.getConfig().getBoolean("settings.debug-mode", false)) {
                            plugin.getLogger().info("Module " + fullPath + ": " + (enabled ? "ENABLED" : "DISABLED"));
                        }
                    }

                    // Recursively load sub-modules
                    loadModuleStatesRecursive(fullPath, subSection);
                }
            }
        }
    }

    /**
     * Check if a module is enabled
     * @param modulePath The dot-separated path to the module (e.g., "modules.homes")
     * @return true if enabled, false otherwise
     */
    public boolean isModuleEnabled(String modulePath) {
        // Normalize path - add "modules." prefix if not present
        if (!modulePath.startsWith("modules.")) {
            modulePath = "modules." + modulePath;
        }

        return moduleStates.getOrDefault(modulePath, true);
    }

    /**
     * Check if a specific feature within a module is enabled
     * @param modulePath The module path
     * @param feature The feature name
     * @return true if both module and feature are enabled
     */
    public boolean isFeatureEnabled(String modulePath, String feature) {
        if (!isModuleEnabled(modulePath)) {
            return false;
        }

        String featurePath = modulePath + "." + feature;
        if (!featurePath.startsWith("modules.")) {
            featurePath = "modules." + featurePath;
        }

        return moduleStates.getOrDefault(featurePath, true);
    }

    /**
     * Get a config value from a module
     */
    public <T> T getModuleConfig(String path, T defaultValue) {
        Object value = plugin.getConfig().get(path, defaultValue);
        try {
            @SuppressWarnings("unchecked")
            T result = (T) value;
            return result;
        } catch (ClassCastException e) {
            plugin.getLogger().warning("Config type mismatch for " + path + ", using default value");
            return defaultValue;
        }
    }

    /**
     * Reload module states from config
     */
    public void reload() {
        moduleStates.clear();
        loadModuleStates();
    }

    /**
     * Get all module states for debugging
     */
    public Map<String, Boolean> getModuleStates() {
        return new HashMap<>(moduleStates);
    }
}
