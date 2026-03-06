package com.lootr.deathchest.listeners;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;

import com.lootr.deathchest.LootrDeathChest;

public class PlayerDeathListener implements Listener {

    private final LootrDeathChest plugin;

    public PlayerDeathListener(LootrDeathChest plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onDeath(PlayerDeathEvent event) {

        Player player = event.getEntity();
        Location loc = player.getLocation();

        List<ItemStack> drops = new ArrayList<>(event.getDrops());

        if (drops.isEmpty()) return;

        event.getDrops().clear();

        plugin.getDeathChestManager().createDeathChest(player, loc, drops);
    }
}