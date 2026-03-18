package com.lootr.deathchest.manager;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import java.util.Set;
import java.util.HashSet;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import java.util.Collections;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.Chest;
import org.bukkit.entity.ArmorStand;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;

import com.lootr.deathchest.LootrDeathChest;
import com.lootr.deathchest.utils.ChestUtils;

public class DeathChestManager {

    private final LootrDeathChest plugin;

    // Active hologram ArmorStands track karne ke liye
    private final Map<String, UUID> activeHolograms = new HashMap<>();
    private final Set<String> activeChests = Collections.synchronizedSet(new HashSet<>());

    public DeathChestManager(LootrDeathChest plugin) {
        this.plugin = plugin;
    }

    public void createDeathChest(Player player, Location loc, List<ItemStack> items) {

        int removeTimeSecs = plugin.getConfig().getInt("death-chest.remove-time", 300);
        Location safeLoc = ChestUtils.findSafeLocation(loc);

        // Step 1: Same tick mein chest block place karo
        ChestUtils.placeChest(safeLoc);

        // Step 2: 1 tick baad tile entity initialize hone do, phir items daalo
        // FIX: Pehle chest.update() nahi karna tha inventory fill karne ke BAAD —
        // update(force=true) tile entity ko re-push karta hai jo inventory wipe
        // kar deta tha. Ab: pehle update, phir inventory fill.
        Bukkit.getScheduler().runTask(plugin, () -> {

            Block block = safeLoc.getBlock();

            if (!(block.getState() instanceof Chest)) {
                plugin.getLogger().warning("[LootrDeathChest] Chest place nahi hui at "
                        + safeLoc + " | Player: " + player.getName()
                        + " | Block: " + block.getType());
                // Safety: items drop karo taaki player ka saman na jaaye
                for (ItemStack item : items) {
                    if (item != null) {
                        loc.getWorld().dropItemNaturally(loc, item);
                    }
                }
                return;
            }

            Chest chest = (Chest) block.getState();

            // FIX: update() PEHLE call karo — block state sync hoga,
            // tile entity fresh rehega, PHIR inventory fill karenge
            chest.update(true, false);
            activeChests.add(locKey(safeLoc));

            // Live inventory reference lo
            Inventory inv = chest.getInventory();

            int placed = 0;
            for (ItemStack item : items) {
                if (item != null && !item.getType().isAir()) {
                    // addItem returns leftover agar chest full ho jaaye
                    Map<Integer, ItemStack> leftover = inv.addItem(item);
                    if (leftover.isEmpty()) {
                        placed++;
                    } else {
                        // Overflow items naturally drop karo
                        leftover.values().forEach(drop -> loc.getWorld().dropItemNaturally(loc, drop));
                    }
                }
            }

            plugin.getLogger().info("[LootrDeathChest] Death chest bani | Player: "
                    + player.getName() + " | Location: " + safeLoc
                    + " | Items: " + placed + "/" + items.size()
                    + " | Expires in: " + removeTimeSecs + "s");

            // Hologram spawn karo chest ke upar
            spawnHologram(safeLoc, player.getName(), removeTimeSecs);
            startDistanceTracker(player, safeLoc, removeTimeSecs);

            // Timer: remove-time ke baad chest gayab kar do
            if (removeTimeSecs > 0) {
                scheduleRemoval(safeLoc, loc, removeTimeSecs);
            }
        });
    }

    /**
     * Chest ke upar ek invisible ArmorStand spawn karo jo player ka naam show kare.
     */
    private void spawnHologram(Location chestLoc, String playerName, int removeTimeSecs) {
        // Block ke center ke upar position
        Location hologramLoc = chestLoc.clone().add(0.5, 1.35, 0.5);

        ArmorStand stand = (ArmorStand) hologramLoc.getWorld()
                .spawnEntity(hologramLoc, EntityType.ARMOR_STAND);

        int mins = removeTimeSecs / 60;
        int secs = removeTimeSecs % 60;
        String timeStr = (mins > 0) ? mins + "m " + (secs > 0 ? secs + "s" : "") : secs + "s";

        stand.setCustomName(
                ChatColor.GOLD + "☠ " + playerName
                        + ChatColor.GRAY + " | "
                        + ChatColor.RED + "⏳ " + timeStr.trim());
        stand.setCustomNameVisible(true);
        stand.setVisible(false); // Invisible body
        stand.setGravity(false); // Float in place
        stand.setSmall(true); // Smaller hitbox
        stand.setInvulnerable(true); // Break nahi hogi
        stand.setCollidable(false); // Players se collide nahi
        stand.setMarker(true); // Click se interact nahi hoga

        // Location ko normalized key ke roop mein store karo
        activeHolograms.put(locKey(chestLoc), stand.getUniqueId());
    }

    private void startDistanceTracker(Player player, Location chestLoc, int durationSecs) {
        final int[] ticksLeft = { durationSecs * 20 };

        Bukkit.getScheduler().runTaskTimer(plugin, task -> {

            ticksLeft[0] -= 20;

            // Timer khatam ya chest gone — tracker band karo
            if (ticksLeft[0] <= 0 || chestLoc.getBlock().getType() != Material.CHEST) {
                task.cancel();
                // Final message
                sendActionBar(player, ChatColor.RED + "💀 Death chest expired.");
                notifyAdmins(ChatColor.GRAY + "[DeathChest] " + player.getName() + " ki chest expire ho gayi.");
                return;
            }

            // Player offline ho gaya
            if (!player.isOnline()) {
                task.cancel();
                return;
            }

            double dist = player.getLocation().distance(chestLoc);

            // Player paas aa gaya — tracker band karo
            if (dist <= 3.0) {
                task.cancel();
                sendActionBar(player, ChatColor.GREEN + "✅ Chest yahi hai!");
                return;
            }

            String direction = getDirection(player.getLocation(), chestLoc);
            int distInt = (int) dist;
            int secsLeft = ticksLeft[0] / 20;

            String msg = ChatColor.GOLD + "💀 Chest: "
                    + ChatColor.WHITE + direction + " "
                    + ChatColor.AQUA + distInt + " blocks"
                    + ChatColor.GRAY + " | "
                    + ChatColor.RED + "⏳ " + secsLeft + "s";

            // Dead player ko dikhao
            sendActionBar(player, msg);

            // Sabhi online admins ko dikhao
            notifyAdmins(ChatColor.GRAY + "[DC] " + ChatColor.YELLOW + player.getName()
                    + ChatColor.GRAY + " chest → " + direction + " " + distInt + " blocks | " + secsLeft + "s");

        }, 0L, 20L); // Har 1 second (20 ticks) update
    }

    /**
     * Player ke facing aur chest ke position se compass direction nikalo.
     */
    private String getDirection(Location from, Location to) {
        double dx = to.getX() - from.getX();
        double dz = to.getZ() - from.getZ();

        double angle = Math.toDegrees(Math.atan2(dz, dx));
        // atan2 East = 0°, hume Minecraft yaw se match karna hai
        // Normalize 0–360
        angle = (angle + 360) % 360;

        // 8-direction compass
        if (angle < 22.5 || angle >= 337.5)
            return "→ E";
        else if (angle < 67.5)
            return "↘ SE";
        else if (angle < 112.5)
            return "↓ S";
        else if (angle < 157.5)
            return "↙ SW";
        else if (angle < 202.5)
            return "← W";
        else if (angle < 247.5)
            return "↖ NW";
        else if (angle < 292.5)
            return "↑ N";
        else
            return "↗ NE";
    }

    /**
     * Player ko ActionBar message bhejo.
     */
    private void sendActionBar(Player player, String message) {
        if (player.isOnline()) {
            player.spigot().sendMessage(
                    net.md_5.bungee.api.ChatMessageType.ACTION_BAR,
                    net.md_5.bungee.api.chat.TextComponent.fromLegacyText(message));
        }
    }

    /**
     * Sabhi online players jinka permission `deathchest.admin` ho unhe chat mein
     * notify karo.
     */
    private void notifyAdmins(String message) {
        for (Player online : Bukkit.getOnlinePlayers()) {
            if (online.hasPermission("deathchest.admin")) {
                online.sendMessage(message);
            }
        }
    }

    /**
     * remove-time ke baad chest aur uska hologram dono remove karo.
     */
    private void scheduleRemoval(Location chestLoc, Location dropLoc, int seconds) {
        long ticks = seconds * 20L;
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            Block block = chestLoc.getBlock();
            if (block.getType() == Material.CHEST) {
                Chest chest = (Chest) block.getState();
                // Jo bhi items bache hain, drop kar do
                for (ItemStack leftover : chest.getInventory().getContents()) {
                    if (leftover != null) {
                        chestLoc.getWorld().dropItemNaturally(chestLoc, leftover);
                    }
                }
                chest.getInventory().clear();
                block.setType(Material.AIR, false);
                activeChests.remove(locKey(chestLoc));
                plugin.getLogger().info("[LootrDeathChest] Chest expired and removed at " + chestLoc);
            }
            // Hologram bhi remove karo
            removeHologram(chestLoc);
            removeChest(chestLoc, false);
        }, ticks);
    }

    /**
     * ArmorStand hologram ko world se hata do.
     */
    private void removeHologram(Location chestLoc) {
        UUID uuid = activeHolograms.remove(locKey(chestLoc));
        if (uuid != null) {
            Entity entity = Bukkit.getEntity(uuid);
            if (entity != null) {
                entity.remove();
            }
        }
    }

    /**
     * Check karo ki given location pe death chest hai ya nahi.
     */
    public boolean isDeathChest(Location loc) {
        return activeChests.contains(locKey(loc));
    }

    /**
     * Death chest forcefully remove karo — inventory clear, block AIR, hologram +
     * tracker band.
     * Break event aur empty event dono use karte hain isko.
     */
    public void removeChest(Location loc, boolean dropItems) {
        String key = locKey(loc);
        if (!activeChests.contains(key))
            return;

        activeChests.remove(key);

        Block block = loc.getBlock();
        if (block.getType() == Material.CHEST) {
            Chest chest = (Chest) block.getState();
            if (dropItems) {
                // Items naturally drop karo
                for (ItemStack item : chest.getInventory().getContents()) {
                    if (item != null) {
                        loc.getWorld().dropItemNaturally(loc, item);
                    }
                }
            }
            chest.getInventory().clear();
            block.setType(Material.AIR, false);
        }

        // Player head (grave) bhi remove karo agar tha
        Block headBlock = loc.clone().add(0, 1, 0).getBlock();
        if (headBlock.getType() == Material.PLAYER_HEAD) {
            headBlock.setType(Material.AIR, false);
        }

        // Hologram remove karo
        removeHologram(loc);

        plugin.getLogger().info("[LootrDeathChest] Chest removed (looted/broken) at " + loc);
    }

    /**
     * Location ko ek consistent String key mein convert karo (HashMap ke liye).
     */
    private String locKey(Location loc) {
        return loc.getWorld().getName() + ":"
                + loc.getBlockX() + ":"
                + loc.getBlockY() + ":"
                + loc.getBlockZ();
    }
}