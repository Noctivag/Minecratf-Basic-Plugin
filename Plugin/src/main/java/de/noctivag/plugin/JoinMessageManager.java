package de.noctivag.plugin;

import de.noctivag.plugin.utils.ColorUtils;
import net.kyori.adventure.text.Component;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.logging.Level;

public class JoinMessageManager {
    private final JavaPlugin plugin;
    private final File configFile;
    private FileConfiguration config;
    private final ConcurrentHashMap<String, String> customMessages;
    private final Set<String> disabledMessages;
    private volatile String defaultMessage;
    private final Map<String, Component> messageCache;
    private final ReentrantReadWriteLock configLock;
    private static final int CACHE_SIZE = 100;
    private static final int MAX_MESSAGE_LENGTH = 256;
    private static final String DEFAULT_JOIN_MESSAGE = "&7[&a+&7] &e%player% &7hat den Server betreten";

    public JoinMessageManager(@NotNull JavaPlugin plugin) {
        this.plugin = Objects.requireNonNull(plugin, "Plugin cannot be null");
        this.configFile = new File(plugin.getDataFolder(), "join_messages.yml");
        this.customMessages = new ConcurrentHashMap<>();
        this.disabledMessages = Collections.newSetFromMap(new ConcurrentHashMap<>());
        this.configLock = new ReentrantReadWriteLock();
        this.messageCache = Collections.synchronizedMap(new LinkedHashMap<>(CACHE_SIZE + 1, 0.75f, true) {
            @Override
            protected boolean removeEldestEntry(Map.Entry<String, Component> eldest) {
                return size() > CACHE_SIZE;
            }
        });

        // Erstelle das Plugin-Verzeichnis, falls es nicht existiert
        if (!plugin.getDataFolder().exists() && !plugin.getDataFolder().mkdirs()) {
            plugin.getLogger().warning("Konnte Plugin-Verzeichnis nicht erstellen!");
        }

        loadConfig();
    }

    public void loadConfig() {
        configLock.writeLock().lock();
        try {
            if (!configFile.exists()) {
                plugin.saveResource("join_messages.yml", false);
            }

            config = YamlConfiguration.loadConfiguration(configFile);
            defaultMessage = config.getString("default-message", DEFAULT_JOIN_MESSAGE);

            // Lade Nachrichten in einem Batch
            customMessages.clear();
            ConfigurationSection messagesSection = config.getConfigurationSection("custom-messages");
            if (messagesSection != null) {
                for (String playerName : messagesSection.getKeys(false)) {
                    String message = messagesSection.getString(playerName);
                    if (isValidMessage(message)) {
                        customMessages.put(playerName.toLowerCase(), message);
                    }
                }
            }

            // Lade deaktivierte Nachrichten
            List<String> disabledList = config.getStringList("disabled-messages");
            synchronized (disabledMessages) {
                disabledMessages.clear();
                disabledMessages.addAll(disabledList.stream()
                    .filter(Objects::nonNull)
                    .map(String::toLowerCase)
                    .toList());
            }

            // Cache leeren nach Reload
            messageCache.clear();

        } catch (Exception e) {
            plugin.getLogger().log(Level.SEVERE, "Fehler beim Laden der Join-Nachrichten", e);
            defaultMessage = DEFAULT_JOIN_MESSAGE;
        } finally {
            configLock.writeLock().unlock();
        }
    }

    public void saveConfig() {
        configLock.writeLock().lock();
        try {
            // Sichere Kopien erstellen
            Map<String, String> messagesToSave = new HashMap<>(customMessages);
            List<String> disabledToSave;
            synchronized (disabledMessages) {
                disabledToSave = new ArrayList<>(disabledMessages);
            }

            // Konfiguration vorbereiten
            config.set("default-message", defaultMessage);

            // Custom Messages speichern
            config.set("custom-messages", null);
            ConfigurationSection messagesSection = config.createSection("custom-messages");
            messagesToSave.forEach((player, message) -> {
                if (isValidMessage(message)) {
                    messagesSection.set(player, message);
                }
            });

            // Deaktivierte Nachrichten speichern
            config.set("disabled-messages", disabledToSave);

            // Atomic Speicherung
            File tempFile = new File(configFile.getParentFile(), "join_messages.yml.tmp");
            config.save(tempFile);
            if (!tempFile.renameTo(configFile)) {
                config.save(configFile); // Fallback: direktes Speichern
            }

            if (plugin.getConfig().getBoolean("settings.debug-mode", false)) {
                plugin.getLogger().info(String.format(
                    "Join-Nachrichten gespeichert: %d Nachrichten, %d deaktiviert",
                    messagesToSave.size(),
                    disabledToSave.size()
                ));
            }
        } catch (IOException e) {
            plugin.getLogger().log(Level.WARNING, "Fehler beim Speichern der Join-Nachrichten", e);
        } finally {
            configLock.writeLock().unlock();
        }
    }

    private boolean isValidMessage(String message) {
        return message != null && !message.isEmpty() && message.length() <= MAX_MESSAGE_LENGTH;
    }

    @NotNull
    public Component getJoinMessage(@NotNull String playerName) {
        String lowercaseName = playerName.toLowerCase();

        // Prüfe Cache
        Component cached = messageCache.get(lowercaseName);
        if (cached != null) {
            return cached;
        }

        // Keine Nachricht wenn deaktiviert
        if (disabledMessages.contains(lowercaseName)) {
            return Component.empty();
        }

        // Erstelle und cache die Nachricht
        try {
            String message = customMessages.getOrDefault(lowercaseName, defaultMessage)
                .replace("%player%", playerName);
            Component component = de.noctivag.plugin.utils.ColorUtils.parseColor(message);

            // Cache nur wenn die Nachricht nicht zu groß ist
            if (message.length() <= 256) {
                messageCache.put(lowercaseName, component);
            }

            return component;
        } catch (Exception e) {
            plugin.getLogger().log(Level.WARNING,
                "Fehler beim Erstellen der Join-Nachricht für " + playerName, e);
            return Component.text(playerName); // Fallback im Fehlerfall
        }
    }

    public void setCustomMessage(@NotNull String playerName, @NotNull String message) {
        Objects.requireNonNull(playerName, "PlayerName cannot be null");
        Objects.requireNonNull(message, "Message cannot be null");

        String lowercaseName = playerName.toLowerCase();
        customMessages.put(lowercaseName, message);
        messageCache.remove(lowercaseName);
    }

    public void removeCustomMessage(@NotNull String playerName) {
        Objects.requireNonNull(playerName, "PlayerName cannot be null");
        String lowercaseName = playerName.toLowerCase();
        customMessages.remove(lowercaseName);
        messageCache.remove(lowercaseName);
    }

    public boolean hasCustomMessage(@NotNull String playerName) {
        Objects.requireNonNull(playerName, "PlayerName cannot be null");
        return customMessages.containsKey(playerName.toLowerCase());
    }

    public boolean isMessageDisabled(@NotNull String playerName) {
        Objects.requireNonNull(playerName, "PlayerName cannot be null");
        return disabledMessages.contains(playerName.toLowerCase());
    }

    public void setMessageEnabled(@NotNull String playerName, boolean enabled) {
        Objects.requireNonNull(playerName, "PlayerName cannot be null");
        String lowercaseName = playerName.toLowerCase();

        synchronized (disabledMessages) {
            if (enabled) {
                disabledMessages.remove(lowercaseName);
            } else {
                disabledMessages.add(lowercaseName);
            }
        }
        messageCache.remove(lowercaseName);
    }

    public void setDefaultMessage(@NotNull String message) {
        Objects.requireNonNull(message, "Default message cannot be null");
        this.defaultMessage = message;
        messageCache.clear();
    }

    /**
     * Löscht den Cache und lädt die Konfiguration neu
     */
    @SuppressWarnings("unused")
    public void reload() {
        messageCache.clear();
        loadConfig();
    }
}
