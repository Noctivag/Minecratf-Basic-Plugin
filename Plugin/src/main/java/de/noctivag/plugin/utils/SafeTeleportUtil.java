package de.noctivag.plugin.utils;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

public final class SafeTeleportUtil {
    private SafeTeleportUtil() {}

    public static Location findSafeLocationNear(Location base) {
        if (base == null) return null;
        World world = base.getWorld();
        if (world == null) return null;

        int x = base.getBlockX();
        int z = base.getBlockZ();

        // Try around the base Y first (downwards, then upwards)
        int startY = base.getBlockY();
        Location loc = scanForSafe(world, x, z, startY, 12);
        if (loc != null) return centerOnBlock(loc);

        // Fallback to highest block at column
        int highest = world.getHighestBlockYAt(x, z);
        Location check = new Location(world, x, highest, z);
        if (isSafeFeet(check)) {
            return centerOnBlock(check.add(0, 1, 0));
        }
        // Try just above highest
        check = new Location(world, x, highest + 1, z);
        if (isSafeFeet(check)) {
            return centerOnBlock(check);
        }
        return null;
    }

    private static Location scanForSafe(World world, int x, int z, int startY, int range) {
        // Scan downwards from startY
        for (int dy = 0; dy <= range; dy++) {
            int y = startY - dy;
            if (y < world.getMinHeight()) break;
            Location feet = new Location(world, x, y, z);
            if (isSafeFeet(feet)) {
                return feet;
            }
        }
        // Then upwards
        for (int dy = 1; dy <= range; dy++) {
            int y = startY + dy;
            if (y > world.getMaxHeight() - 2) break;
            Location feet = new Location(world, x, y, z);
            if (isSafeFeet(feet)) {
                return feet;
            }
        }
        return null;
    }

    private static boolean isSafeFeet(Location feet) {
        World world = feet.getWorld();
        if (world == null) return false;
        // Need solid block below and two air blocks for feet/head
        Block below = world.getBlockAt(feet.getBlockX(), feet.getBlockY() - 1, feet.getBlockZ());
        Block feetBlock = world.getBlockAt(feet.getBlockX(), feet.getBlockY(), feet.getBlockZ());
        Block headBlock = world.getBlockAt(feet.getBlockX(), feet.getBlockY() + 1, feet.getBlockZ());

        if (!below.getType().isSolid()) return false;
        if (!feetBlock.getType().isAir()) return false;
        if (!headBlock.getType().isAir()) return false;

        // Avoid dangerous surfaces
        Material belowType = below.getType();
        if (belowType == Material.LAVA || belowType == Material.CACTUS || belowType == Material.FIRE) {
            return false;
        }
        // Avoid water feet
        if (feetBlock.isLiquid() || headBlock.isLiquid()) return false;

        return true;
    }

    private static Location centerOnBlock(Location loc) {
        return new Location(loc.getWorld(), loc.getBlockX() + 0.5, loc.getBlockY(), loc.getBlockZ() + 0.5, loc.getYaw(), loc.getPitch());
    }
}
