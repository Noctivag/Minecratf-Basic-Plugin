package de.noctivag.plugin.permissions;

import de.noctivag.plugin.Plugin;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

public class PermissionManager {

    private final Plugin plugin;
    private final Map<String, String> permissions = new HashMap<>();

    public PermissionManager(Plugin plugin) {
        this.plugin = plugin;
        loadPermissions();
    }

    private void loadPermissions() {
        File permissionsFile = new File(plugin.getDataFolder(), "permissions.yml");
        if (!permissionsFile.exists()) {
            plugin.saveResource("permissions.yml", false);
        }
        FileConfiguration config = YamlConfiguration.loadConfiguration(permissionsFile);

        permissions.clear();
        if (config.isConfigurationSection("permissions")) {
            for (String command : config.getConfigurationSection("permissions").getKeys(false)) {
                permissions.put(command, config.getString("permissions." + command));
            }
        }
    }

    public String getPermission(String command) {
        return permissions.getOrDefault(command, "plugin." + command);
    }

    public boolean hasPermission(CommandSender sender, String command) {
        if (!(sender instanceof Player)) {
            return true; // Console has all permissions
        }
        return sender.hasPermission(getPermission(command));
    }

    public void reload() {
        loadPermissions();
    }
}
