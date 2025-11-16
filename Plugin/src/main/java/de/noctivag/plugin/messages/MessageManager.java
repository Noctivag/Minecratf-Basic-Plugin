package de.noctivag.plugin.messages;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.HashMap;
import java.util.Map;
import de.noctivag.plugin.utils.ColorUtils;

public class MessageManager {
    private final JavaPlugin plugin;
    private final Map<String, String> messages;
    private final Map<String, String> defaultMessages;
    private String language;

    public MessageManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.messages = new HashMap<>();
        this.defaultMessages = new HashMap<>();
        this.language = "de_DE"; // Standard-Sprache
        loadMessages();
    }

    private void loadMessages() {
        // Erstelle messages_de_DE.yml und messages_en_US.yml
        saveDefaultMessageFile("de_DE");
        saveDefaultMessageFile("en_US");

        // Lade Default-Nachrichten aus Ressourcen (aktuelle Sprache + Fallback en_US)
        defaultMessages.clear();
        loadDefaultsIntoMap(language, defaultMessages);
        // Fallback-Defaults laden, falls in der gewählten Sprache etwas fehlt
        loadDefaultsIntoMap("en_US", defaultMessages);

        // Lade Nachrichten der aktuellen Sprache (Datei im Plugin-Ordner)
        File messageFile = new File(plugin.getDataFolder(), "messages_" + language + ".yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(messageFile);

        // Fehlende Keys aus Default in Datei ergänzen
        boolean updated = false;
        for (Map.Entry<String, String> entry : defaultMessages.entrySet()) {
            String key = entry.getKey();
            if (!config.contains(key)) {
                config.set(key, entry.getValue());
                updated = true;
            }
        }
        if (updated) {
            try {
                config.save(messageFile);
            } catch (Exception e) {
                plugin.getLogger().warning("Could not update missing message keys in " + messageFile.getName() + ": " + e.getMessage());
            }
        }

        // In den Speicher laden
        messages.clear();
        for (String key : config.getKeys(true)) {
            if (config.isString(key)) {
                messages.put(key, config.getString(key));
            }
        }
    }

    private void saveDefaultMessageFile(String lang) {
        File messageFile = new File(plugin.getDataFolder(), "messages_" + lang + ".yml");
        if (!messageFile.exists()) {
            plugin.saveResource("messages_" + lang + ".yml", false);
        }
    }

    private void loadDefaultsIntoMap(String lang, Map<String, String> target) {
        try {
            String resourceName = "messages_" + lang + ".yml";
            InputStream is = plugin.getResource(resourceName);
            if (is == null) return;
            YamlConfiguration cfg = YamlConfiguration.loadConfiguration(new InputStreamReader(is));
            for (String key : cfg.getKeys(true)) {
                if (cfg.isString(key) && !target.containsKey(key)) {
                    target.put(key, cfg.getString(key));
                }
            }
        } catch (Exception e) {
            plugin.getLogger().warning("Failed to load default messages for " + lang + ": " + e.getMessage());
        }
    }

    public void setLanguage(String language) {
        this.language = language;
        loadMessages();
    }

    public Component getMessage(String key, Object... args) {
        String message = messages.containsKey(key)
                ? messages.get(key)
                : defaultMessages.getOrDefault(key, "Message '" + key + "' not found");
        // Ersetze Platzhalter {0}, {1}, etc. mit den Argumenten
        for (int i = 0; i < args.length; i++) {
            message = message.replace("{" + i + "}", String.valueOf(args[i]));
        }
        return ColorUtils.parseColor(message);
    }

    public Component getError(String key, Object... args) {
        return getMessage(key, args).color(NamedTextColor.RED);
    }

    public Component getComponentMessage(String key, Component... args) {
        String message = messages.containsKey(key)
                ? messages.get(key)
                : defaultMessages.getOrDefault(key, "Message '" + key + "' not found");
        Component result = Component.empty();
        String[] parts = message.split("\\{\\d+\\}");
        for (int i = 0; i < parts.length; i++) {
            result = result.append(ColorUtils.parseColor(parts[i]));
            if (i < args.length) {
                result = result.append(args[i]);
            }
        }
        return result;
    }

    public java.util.List<Component> getMessageList(String key) {
        java.util.List<Component> components = new java.util.ArrayList<>();
        Object value = messages.get(key);
        if (value instanceof java.util.List) {
            for (String line : (java.util.List<String>) value) {
                components.add(ColorUtils.parseColor(line));
            }
        } else if (value instanceof String) {
            components.add(ColorUtils.parseColor((String) value));
        }
        return components;
    }
}
