# LootrDeathChest

**LootrDeathChest** is a lightweight Minecraft plugin that creates a chest containing a player's dropped items at their death location.
It is designed to be **compatible with Lootr** while remaining completely **independent and conflict-free**.

This plugin prevents items from scattering on the ground and instead stores them safely inside a chest.

---

## Features

* Creates a **death chest** at the player's death location
* Stores all dropped items safely inside the chest
* Prevents item loss from despawning or scattering
* Lightweight and simple implementation
* Fully **compatible with Lootr plugin** (no dependency required)
* Works across multiple Minecraft versions

---

## Compatibility

Tested and designed to run on:

* Minecraft **1.20.x**
* Minecraft **1.21.x**

The plugin only uses the standard **Spigot/Bukkit API**, which allows it to remain compatible across multiple versions.

---

## Installation

1. Download the latest compiled `.jar` file.
2. Place the file inside your server's `plugins` folder.
3. Restart or reload the server.

```
/plugins/LootrDeathChest.jar
```

---

## How It Works

When a player dies:

1. The plugin intercepts the dropped items.
2. A chest is placed at the death location.
3. All dropped items are stored inside the chest.

This keeps the area clean and prevents items from being lost.

---

## Configuration

Example `config.yml`:

```yaml
death-chest:
  remove-time: 300
  hologram: false
  protect-owner: false
```

You can modify these values to change how the death chest behaves.

---

## Compatibility with Lootr

LootrDeathChest is designed to **coexist with the Lootr plugin** without modifying or interacting with its internal systems.

* No API linking
* No shared storage
* No conflicts
 `FOR NOW`

Both plugins can run side-by-side safely.

---

## Building from Source

Requirements:

* Java **17**
* Maven

Build the plugin with:

```
mvn clean package
```

The compiled plugin will appear in:

```
target/LootrDeathChest-1.0.jar
```

---

## Author

**Sunny Mittal**

GitHub:
https://github.com/Sunnymittal112
