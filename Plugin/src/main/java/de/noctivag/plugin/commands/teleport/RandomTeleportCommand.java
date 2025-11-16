package de.noctivag.plugin.commands.teleport;

import de.noctivag.plugin.Plugin;
import org.bukkit.*;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Random;

/**
 * Command for /tprandom - random teleport
 */
public class RandomTeleportCommand implements CommandExecutor {
    private final Plugin plugin;
    private final Random random = new Random();

    public RandomTeleportCommand(Plugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!plugin.getConfig().getBoolean("modules.teleportation.random.enabled", true)) {
            sender.sendMessage("§c/tprandom is currently disabled!");
            return true;
        }

        if (!(sender instanceof Player)) {
            sender.sendMessage("§cOnly players can use this command!");
            return true;
        }

        Player player = (Player) sender;

        if (!player.hasPermission("plugin.teleport.random")) {
            player.sendMessage(plugin.getMessageManager().getMessage("no_permission"));
            return true;
        }

        int maxDistance = plugin.getConfig().getInt("modules.teleportation.random.max-distance", 5000);
        int minDistance = plugin.getConfig().getInt("modules.teleportation.random.min-distance", 100);

        player.sendMessage("§eSearching for a safe location...");

        Location safeLoc = findSafeRandomLocation(player.getWorld(), maxDistance, minDistance);
        
        if (safeLoc == null) {
            player.sendMessage("§cCould not find a safe location! Try again.");
            return true;
        }

        player.teleport(safeLoc);
        player.sendMessage("§aTeleported to §e" + safeLoc.getBlockX() + ", " + safeLoc.getBlockY() + ", " + safeLoc.getBlockZ());
        player.playSound(player.getLocation(), Sound.ENTITY_ENDERMAN_TELEPORT, 1.0f, 1.0f);

        return true;
    }

    private Location findSafeRandomLocation(World world, int maxDistance, int minDistance) {
        int attempts = 0;
        int maxAttempts = 10;

        while (attempts < maxAttempts) {
            int x = random.nextInt(maxDistance * 2) - maxDistance;
            int z = random.nextInt(maxDistance * 2) - maxDistance;

            // Ensure minimum distance
            if (Math.abs(x) < minDistance && Math.abs(z) < minDistance) {
                attempts++;
                continue;
            }

            Location loc = new Location(world, x, 100, z);
            Location safeLoc = getSafeLocation(loc);

            if (safeLoc != null) {
                return safeLoc;
            }

            attempts++;
        }

        return null;
    }

    private Location getSafeLocation(Location location) {
        World world = location.getWorld();
        if (world == null) return null;

        int x = location.getBlockX();
        int z = location.getBlockZ();

        // Find highest block
        int y = world.getHighestBlockYAt(x, z);
        Location checkLoc = new Location(world, x + 0.5, y, z + 0.5);

        Material blockType = checkLoc.getBlock().getType();
        Material blockAbove = checkLoc.clone().add(0, 1, 0).getBlock().getType();
        Material blockAbove2 = checkLoc.clone().add(0, 2, 0).getBlock().getType();

        // Check if it's safe
        if (blockType == Material.LAVA || blockType == Material.WATER || 
            blockType == Material.FIRE || blockType == Material.CACTUS) {
            return null;
        }

        // Need 2 air blocks above
        if (!blockAbove.isAir() || !blockAbove2.isAir()) {
            return null;
        }

        checkLoc.add(0, 1, 0); // Stand on top of the block
        return checkLoc;
    }
}
