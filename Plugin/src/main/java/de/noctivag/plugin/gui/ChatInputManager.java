package de.noctivag.plugin.gui;

import de.noctivag.plugin.Plugin;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ChatInputManager {
    public enum ValueType { STRING, INTEGER, DOUBLE, BOOLEAN }

    public static class PendingEdit {
        public final String configPath;
        public final ValueType type;
        public final String moduleName;

        public PendingEdit(String configPath, ValueType type, String moduleName) {
            this.configPath = configPath;
            this.type = type;
            this.moduleName = moduleName;
        }
    }

    private final Plugin plugin;
    private final GuiManager guiManager;
    private final Map<UUID, PendingEdit> pending = new HashMap<>();

    public ChatInputManager(Plugin plugin, GuiManager guiManager) {
        this.plugin = plugin;
        this.guiManager = guiManager;
    }

    public void startEdit(Player player, String configPath, ValueType type, String moduleName) {
        pending.put(player.getUniqueId(), new PendingEdit(configPath, type, moduleName));
        Object current = plugin.getConfig().get(configPath);
        player.sendMessage("§e§lConfiguration Value Change");
        player.sendMessage("§7Path: §e" + configPath);
        player.sendMessage("§7Current: §e" + (current == null ? "<none>" : current.toString()));
        player.sendMessage("§7Expected type: §e" + type.name());
        player.sendMessage("§7Type the new value in chat, or type §ccancel§7 to abort.");
    }

    public boolean hasPending(Player player) {
        return pending.containsKey(player.getUniqueId());
    }

    public PendingEdit getPending(Player player) {
        return pending.get(player.getUniqueId());
    }

    public void clear(Player player) {
        pending.remove(player.getUniqueId());
    }

    public boolean applyValue(Player player, String input) {
        PendingEdit edit = pending.get(player.getUniqueId());
        if (edit == null) return false;

        String path = edit.configPath;
        try {
            switch (edit.type) {
                case BOOLEAN -> {
                    boolean val = parseBoolean(input);
                    plugin.getConfig().set(path, val);
                }
                case INTEGER -> {
                    int val = Integer.parseInt(input.trim());
                    plugin.getConfig().set(path, val);
                }
                case DOUBLE -> {
                    String norm = input.trim().replace(',', '.');
                    double val = Double.parseDouble(norm);
                    plugin.getConfig().set(path, val);
                }
                case STRING -> {
                    plugin.getConfig().set(path, input);
                }
            }
            return true;
        } catch (Exception ex) {
            player.sendMessage("§cInvalid value for type §4" + edit.type + "§c. Please try again or type §4cancel§c.");
            return false;
        }
    }

    public void reopenModuleMenu(Player player) {
        PendingEdit edit = pending.get(player.getUniqueId());
        if (edit != null) {
            guiManager.openModuleConfig(player, edit.moduleName);
        }
    }

    private boolean parseBoolean(String s) {
        String v = s.trim().toLowerCase();
        return v.equals("true") || v.equals("yes") || v.equals("on") || v.equals("1");
    }
}
