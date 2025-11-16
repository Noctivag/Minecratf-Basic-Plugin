package de.noctivag.plugin.commands;

import de.noctivag.plugin.Plugin;
import de.noctivag.plugin.config.ConfigManager;
import de.noctivag.plugin.messages.MessageManager;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class PluginAdminCommand implements CommandExecutor {
    private final Plugin plugin;
    private final MessageManager messages;
    private final ConfigManager configManager;

    public PluginAdminCommand(Plugin plugin) {
        this.plugin = plugin;
        this.messages = plugin.getMessageManager();
        this.configManager = plugin.getConfigManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (sender instanceof Player p && !p.hasPermission("plugin.config")) {
            sender.sendMessage(messages.getError("error.no_permission"));
            return true;
        }

        if (args.length < 1) {
            double current = plugin.getConfig().getDouble("modules.cosmetics.sit.y-offset", -1.2);
            sender.sendMessage((messages != null ? messages.getMessage("messages.prefix") : "") + "Aktueller sit-offset: " + current);
            sender.sendMessage(usage());
            return true;
        }

        String action = args[0].toLowerCase();
        if (action.equals("get")) {
            if (args.length >= 2) {
                String key = args[1].toLowerCase();
                return handleGet(sender, key);
            }
            sender.sendMessage(usage());
            return true;
        }
        if (action.equals("set")) {
            if (args.length >= 3) {
                String key = args[1].toLowerCase();
                String value = args[2];

                if (key.equals("sit-offset") || key.equals("sitoffset")) {
                    try {
                        double offset = Double.parseDouble(value);
                        // Clamp to sensible range
                        if (offset < -2.0) offset = -2.0;
                        if (offset > 1.0) offset = 1.0;

                        setConfig("modules.cosmetics.sit.y-offset", offset);
                        sender.sendMessage(messages.getMessage("messages.prefix") + "Sitz-Höhe gesetzt auf " + offset + ".");
                        return true;
                    } catch (NumberFormatException ex) {
                        sender.sendMessage(messages.getError("error.invalid_number"));
                        return true;
                    }
                }
                if (key.equals("allow-on-stairs") || key.equals("stairs")) {
                    boolean state = parseBoolean(value);
                    setConfig("modules.cosmetics.sit.allow-on-stairs", state);
                    sender.sendMessage(messages.getMessage("messages.prefix") + "allow-on-stairs: " + state);
                    return true;
                }
                if (key.equals("allow-on-slabs") || key.equals("slabs")) {
                    boolean state = parseBoolean(value);
                    setConfig("modules.cosmetics.sit.allow-on-slabs", state);
                    sender.sendMessage(messages.getMessage("messages.prefix") + "allow-on-slabs: " + state);
                    return true;
                }
            }
            sender.sendMessage(usage());
            return true;
        }

        sender.sendMessage(usage());
        return true;
    }

    private String usage() {
        return (messages != null ? messages.getMessage("messages.prefix") : "") +
                "§e/plugin get §7<sit-offset|stairs|slabs>\n" +
                "§e/plugin set §7<sit-offset|stairs|slabs> <value>";
    }

    private boolean handleGet(CommandSender sender, String key) {
        String result;
        switch (key) {
            case "sit-offset", "sitoffset" -> {
                double val = plugin.getConfig().getDouble("modules.cosmetics.sit.y-offset", -1.2);
                result = "sit-offset: " + val;
            }
            case "allow-on-stairs", "stairs" -> {
                boolean val = plugin.getConfig().getBoolean("modules.cosmetics.sit.allow-on-stairs", true);
                result = "allow-on-stairs: " + val;
            }
            case "allow-on-slabs", "slabs" -> {
                boolean val = plugin.getConfig().getBoolean("modules.cosmetics.sit.allow-on-slabs", true);
                result = "allow-on-slabs: " + val;
            }
            default -> {
                sender.sendMessage(messages.getError("error.unknown_command"));
                return true;
            }
        }
        sender.sendMessage(messages.getMessage("messages.prefix") + result);
        return true;
    }

    private void setConfig(String path, Object value) {
        plugin.getConfig().set(path, value);
        plugin.saveConfig();
        if (configManager != null && configManager.getConfig() != null) {
            configManager.getConfig().set(path, value);
            configManager.saveConfig();
        }
    }

    private boolean parseBoolean(String val) {
        return val.equalsIgnoreCase("true") || val.equalsIgnoreCase("on") || val.equals("1");
    }
}
