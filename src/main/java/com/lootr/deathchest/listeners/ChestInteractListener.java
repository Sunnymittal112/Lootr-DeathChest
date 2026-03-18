package com.lootr.deathchest.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.block.Chest;

import com.lootr.deathchest.LootrDeathChest;

public class ChestInteractListener implements Listener {

    private final LootrDeathChest plugin;

    public ChestInteractListener(LootrDeathChest plugin) {
        this.plugin = plugin;
    }

    /**
     * Player ne chest band ki — agar bilkul khaali hai to remove karo.
     */
    @EventHandler(priority = EventPriority.MONITOR)
    public void onInventoryClose(InventoryCloseEvent event) {
        InventoryHolder holder = event.getInventory().getHolder();

        if (!(holder instanceof Chest chest))
            return;

        // Death chest hai?
        if (!plugin.getDeathChestManager().isDeathChest(chest.getLocation()))
            return;

        // Khaali hai?
        if (event.getInventory().isEmpty()) {
            // 1 tick baad remove karo — close event ke andar block modify karna unsafe hai
            plugin.getServer().getScheduler().runTask(plugin, () -> {
                plugin.getDeathChestManager().removeChest(chest.getLocation(), false);

                // Player ko notify karo
                if (event.getPlayer() instanceof Player player) {
                    player.sendMessage("§a✅ You took all your summons — chest disappeared.");
                }
            });
        }
    }

    /**
     * Player chest todne ki koshish kare — cancel karo aur chest silently destroy
     * karo.
     * Chest item drop nahi hogi — unfair advantage band.
     */
    @EventHandler(priority = EventPriority.HIGH)
    public void onBlockBreak(BlockBreakEvent event) {
        if (event.getBlock().getType() != org.bukkit.Material.CHEST)
            return;

        if (!plugin.getDeathChestManager().isDeathChest(event.getBlock().getLocation()))
            return;

        // Break cancel karo (chest item nahi giregi)
        event.setCancelled(true);
        event.setDropItems(false);

        // Chest destroy karo — andar jo bhi items hain woh naturally drop honge
        plugin.getDeathChestManager().removeChest(event.getBlock().getLocation(), true);

        event.getPlayer().sendMessage("§c⚠ You Can Not Break Chest");
    }
}