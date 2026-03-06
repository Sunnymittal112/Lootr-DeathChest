package com.lootr.deathchest.utils;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;

public class ChestUtils {

    public static Location findSafeLocation(Location loc) {

        Block block = loc.getBlock();

        if (block.getType().isAir()) {
            return loc;
        }

        Location above = loc.clone().add(0, 1, 0);

        if (above.getBlock().getType().isAir()) {
            return above;
        }

        return loc;
    }

    public static void placeChest(Location loc) {

        loc.getBlock().setType(Material.CHEST);

    }
}