package de.noctivag.plugin.commands.teleport;

import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;

public class SpawnCommand implements CommandExecutor {
    private final JavaPlugin plugin;
    private final File spawnFile;
    private Location spawnLocation;

    public SpawnCommand(JavaPlugin plugin) {
        this.plugin = plugin;
        this.spawnFile = new File(plugin.getDataFolder(), "spawn.yml");
        loadSpawn();
    }

    private void loadSpawn() {
        if (!spawnFile.exists()) {
            // Use world spawn as default
            spawnLocation = Bukkit.getWorlds().get(0).getSpawnLocation();
            return;
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(spawnFile);
        spawnLocation = config.getLocation("spawn");
        
        if (spawnLocation == null) {
            spawnLocation = Bukkit.getWorlds().get(0).getSpawnLocation();
        }
    }

    private void saveSpawn() {
        FileConfiguration config = new YamlConfiguration();
        config.set("spawn", spawnLocation);

        try {
            config.save(spawnFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save spawn.yml: " + e.getMessage());
        }
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        String cmd = command.getName().toLowerCase();

        if (cmd.equals("setspawn")) {
            return handleSetSpawn(sender);
        } else if (cmd.equals("spawn")) {
            return handleSpawn(sender);
        }

        return false;
    }

    private boolean handleSetSpawn(CommandSender sender) {
        if (!sender.hasPermission("essentials.setspawn")) {
            sender.sendMessage(Component.text("You don't have permission!").color(NamedTextColor.RED));
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only players can use this command.").color(NamedTextColor.RED));
            return true;
        }

        spawnLocation = player.getLocation();
        saveSpawn();
        sender.sendMessage(Component.text("Spawn point set!").color(NamedTextColor.GREEN));
        return true;
    }

    private boolean handleSpawn(CommandSender sender) {
        if (!sender.hasPermission("essentials.spawn")) {
            sender.sendMessage(Component.text("You don't have permission!").color(NamedTextColor.RED));
            return true;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(Component.text("Only players can use this command.").color(NamedTextColor.RED));
            return true;
        }

        if (spawnLocation == null) {
            player.sendMessage(Component.text("Spawn point not set!").color(NamedTextColor.RED));
            return true;
        }

        player.teleport(spawnLocation);
        player.sendMessage(Component.text("Teleported to spawn!").color(NamedTextColor.GREEN));
        return true;
    }

    public Location getSpawnLocation() {
        return spawnLocation;
    }
}
