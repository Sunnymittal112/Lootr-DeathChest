package com.lootr.deathchest.listeners;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import com.lootr.deathchest.LootrDeathChest;

public class PlayerDeathListener implements Listener {

    private final LootrDeathChest plugin;

    public PlayerDeathListener(LootrDeathChest plugin) {
        this.plugin = plugin;
    }

    // BUG FIX: Priority.HIGH ensures this runs AFTER vanilla drop handling
    // but BEFORE other plugins that might clear drops (e.g. EssentialsX, CMI).
    // With default NORMAL priority, other plugins can wipe drops first,
    // making drops.isEmpty() = true and silently skipping chest creation.
    @EventHandler(priority = EventPriority.HIGH)
    public void onDeath(PlayerDeathEvent event) {

        Player player = event.getEntity();
        Location loc = player.getLocation();

        // Snapshot the drops before any other plugin clears them
        List<ItemStack> drops = new ArrayList<>(event.getDrops());

        if (drops.isEmpty()) {
            plugin.getLogger().info("[LootrDeathChest] No drops for "
                    + player.getName() + " — skipping chest creation.");
            return;
        }

        // Clear vanilla drops so items don't scatter on the ground
        event.getDrops().clear();

        plugin.getDeathChestManager().createDeathChest(player, loc, drops);
    }
}
