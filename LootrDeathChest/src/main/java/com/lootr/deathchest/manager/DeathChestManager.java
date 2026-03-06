package com.lootr.deathchest.manager;

import java.util.List;

import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Chest;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import com.lootr.deathchest.LootrDeathChest;

public class DeathChestManager {

    private final LootrDeathChest plugin;

    public DeathChestManager(LootrDeathChest plugin) {
        this.plugin = plugin;
    }

    public void createDeathChest(Player player, Location loc, List<ItemStack> items) {

        loc.getBlock().setType(Material.CHEST);

        Chest chest = (Chest) loc.getBlock().getState();

        for (ItemStack item : items) {
            chest.getBlockInventory().addItem(item);
        }

        chest.update();

        registerLootrChest(loc);
    }

    private void registerLootrChest(Location loc) {

        // TODO: integrate with Lootr API
        // Example:

        /*
        LootrAPI.registerChest(loc);
        */

    }
}