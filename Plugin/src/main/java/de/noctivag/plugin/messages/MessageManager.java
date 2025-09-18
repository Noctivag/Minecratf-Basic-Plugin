package de.noctivag.plugin.messages;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import de.noctivag.plugin.utils.ColorUtils;

public class MessageManager {
    private final JavaPlugin plugin;
    private final Map<String, String> messages;
    private String language;

    public MessageManager(JavaPlugin plugin) {
        this.plugin = plugin;
        this.messages = new HashMap<>();
        this.language = "de_DE"; // Standard-Sprache
        loadMessages();
    }

    private void loadMessages() {
        // Erstelle messages_de_DE.yml und messages_en_US.yml
        saveDefaultMessageFile("de_DE");
        saveDefaultMessageFile("en_US");

        // Lade Nachrichten der aktuellen Sprache
        File messageFile = new File(plugin.getDataFolder(), "messages_" + language + ".yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(messageFile);

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

    public void setLanguage(String language) {
        this.language = language;
        loadMessages();
    }

    public Component getMessage(String key, Object... args) {
        String message = messages.getOrDefault(key, "Message '" + key + "' not found");
        // Ersetze Platzhalter {0}, {1}, etc. mit den Argumenten
        for (int i = 0; i < args.length; i++) {
            message = message.replace("{" + i + "}", String.valueOf(args[i]));
        }
        return ColorUtils.parseColor(message);
    }

    public Component getError(String key, Object... args) {
        return getMessage(key, args).color(NamedTextColor.RED);
    }
}
