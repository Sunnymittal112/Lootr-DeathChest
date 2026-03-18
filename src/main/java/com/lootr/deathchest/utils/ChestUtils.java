package com.lootr.deathchest.utils;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;

public class ChestUtils {

    /**
     * Finds a safe location to place a chest near the given location.
     * Searches upward up to 10 blocks for an air/replaceable block
     * that won't accidentally form a double chest.
     */
    public static Location findSafeLocation(Location loc) {
        for (int i = 0; i <= 10; i++) {
            Location check = loc.clone().add(0, i, 0);
            if (isReplaceable(check.getBlock()) && !hasAdjacentChest(check)) {
                return check;
            }
        }
        // Fallback: always 11 blocks above death point
        return loc.clone().add(0, 11, 0);
    }

    /**
     * Checks if a block can be safely replaced by a chest.
     * NOTE: LAVA removed — chest placed in lava gets destroyed instantly,
     * burning all items. Water is fine (waterlogged chest survives).
     */
    private static boolean isReplaceable(Block block) {
        Material type = block.getType();
        return type.isAir()
                || type == Material.WATER
                || type == Material.SHORT_GRASS
                || type == Material.TALL_GRASS
                || type == Material.FERN
                || type == Material.LARGE_FERN
                || type == Material.DEAD_BUSH
                || type == Material.SNOW
                || type == Material.VINE;
    }

    /**
     * Returns true if any horizontal neighbour is already a chest.
     * Placing next to an existing chest creates a double chest which
     * merges inventories and causes items to appear in the wrong chest.
     */
    public static boolean hasAdjacentChest(Location loc) {
        Block block = loc.getBlock();
        for (BlockFace face : new BlockFace[] {
                BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST }) {
            Material neighbour = block.getRelative(face).getType();
            if (neighbour == Material.CHEST || neighbour == Material.TRAPPED_CHEST) {
                return true;
            }
        }
        return false;
    }

    /**
     * Places a chest WITHOUT applying physics.
     * CRITICAL: applyPhysics=false prevents the chest from being silently
     * popped when placed inside solid blocks (underground deaths etc.)
     */
    public static void placeChest(Location loc) {
        loc.getBlock().setType(Material.CHEST, false);
    }
}
