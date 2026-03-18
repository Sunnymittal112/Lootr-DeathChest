package com.lootr.deathchest;

import org.bukkit.plugin.java.JavaPlugin;

import com.lootr.deathchest.listeners.ChestInteractListener;
import com.lootr.deathchest.listeners.PlayerDeathListener;
import com.lootr.deathchest.manager.DeathChestManager;

public class LootrDeathChest extends JavaPlugin {

    private static LootrDeathChest instance;
    private DeathChestManager deathChestManager;

    @Override
    public void onEnable() {

        instance = this;

        saveDefaultConfig();

        deathChestManager = new DeathChestManager(this);

        getServer().getPluginManager().registerEvents(
                new PlayerDeathListener(this), this);

        getServer().getPluginManager().registerEvents(
                new ChestInteractListener(this), this);

        getLogger().info("LootrDeathChest enabled!");
    }

    public static LootrDeathChest getInstance() {
        return instance;
    }

    public DeathChestManager getDeathChestManager() {
        return deathChestManager;
    }
}