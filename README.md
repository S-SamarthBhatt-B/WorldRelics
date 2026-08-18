# 🌍 WorldRelics

**WorldRelics** is a production-ready Minecraft **Paper** plugin created for **Surventure Season 2**.

It introduces a server-wide competitive relic system built around one strict rule:

> **ONE WORLD. ONE RELIC. ONE OWNER.**

A powerful relic appears somewhere in the world. Players must explore, locate, claim, and defend it. The relic grants unique abilities to its owner and remains active for a limited number of Minecraft days.

When its time runs out—or its owner is defeated—the relic returns to the world, beginning a new hunt.

---

## 📖 Table of Contents

1. [Overview](#-overview)
2. [How It Works](#-how-it-works)
3. [Core Lifecycle](#-core-lifecycle)
4. [Features](#-features)
5. [Available Relics](#-available-relics)
6. [Commands & Permissions](#-commands--permissions)
7. [Configuration](#-configuration)
8. [Creating Custom Relics](#-creating-custom-relics)
9. [API & Integration](#-api--integration)
10. [Database & Persistence](#-database--persistence)
11. [Installation](#-installation)
12. [Building From Source](#-building-from-source)
13. [Surventure Season 2](#-surventure-season-2)

---

## 🌟 Overview

At any given time, **exactly one World Relic** can exist on the server.

A relic is randomly selected from the enabled relic pool and spawned at a safe location far away from all online players.

By default, the location is:

* 📍 At least **3,000 blocks** from every online player
* 📍 No more than **10,000 blocks** away
* 🌍 Located in an allowed world
* 🏛️ Associated with a themed structure
* 🛡️ Protected while the relic is active

Players must explore the world to find the relic.

Once claimed, the player becomes the **sole owner** of the relic and gains its unique passive and active abilities.

The relic remains active for a configurable **10–15 Minecraft days** by default.

> **1 Minecraft day = 20 real-world minutes**

When the relic expires, it is destroyed and a new relic begins its journey somewhere else in the world.

If the owner dies, the relic drops naturally and becomes available for another player to claim.

---

# 🔄 How It Works

The WorldRelics gameplay loop is simple:

```text
🌍 RELIC AWAKENS
       ↓
📍 RANDOM DISTANT LOCATION
       ↓
🏛️ STRUCTURE APPEARS
       ↓
🧭 PLAYERS SEARCH
       ↓
⚔️ PLAYERS COMPETE
       ↓
👑 ONE PLAYER CLAIMS THE RELIC
       ↓
⚡ RELIC POWER BECOMES ACTIVE
       ↓
🎯 OWNER BECOMES A TARGET
       ↓
💀 OWNER DIES
       OR
       ⏳ RELIC EXPIRES
       ↓
💥 RELIC DISAPPEARS
       ↓
🌍 NEW RELIC AWAKENS
```

The result is a constantly changing server-wide objective.

---

# 🔄 Core Lifecycle

Normal lifecycle:

```text
NO_RELIC
    ↓
RESPAWNING
    ↓
AVAILABLE
    ↓
CLAIMED
    ↓
EXPIRED
    ↓
RESPAWNING
```

If the owner dies:

```text
CLAIMED
    ↓
OWNER_DEATH
    ↓
ITEM_DROPPED
    ↓
AVAILABLE
    ↓
NEW_OWNER
```

### Strict Single-Relic Rule

WorldRelics must maintain:

> **Never more than one active relic.**

This rule is enforced across:

* Server restarts
* Plugin reloads
* Player deaths
* Item movement
* Containers
* Dropped items
* Relic transfers
* Administrative commands

---

# 🚀 Features

### 🌍 Single Active Relic

Only one World Relic can exist across the entire server at any time.

### 🧭 Distant Spawn Locations

Relics spawn far away from all online players using a configurable distance range.

Default:

```text
Minimum: 3,000 blocks
Maximum: 10,000 blocks
```

### 🏛️ Themed Structures

Relics can appear inside unique locations such as:

* Ancient altars
* Storm shrines
* Frozen temples
* Ruined fortresses
* Corrupted chambers

### 🛡️ Temporary Structure Protection

The relic area can automatically protect itself from:

* Block breaking
* Block placing
* Explosions
* Fire

Protection disappears when the relic cycle ends.

### ⚡ Unique Relic Abilities

Every relic can provide:

* Passive effects
* Active abilities
* Cooldowns
* Configurable values
* Custom item appearances

### 💀 Relic Drops on Death

When the owner dies, the relic can drop naturally and become claimable by another player.

### ⏳ Minecraft-Day Lifetime

Relic lifetimes are based on Minecraft days rather than server uptime.

Default:

```text
10–15 Minecraft days
```

### 🛡️ Anti-Duplication

Relics use:

* Unique UUIDs
* PersistentDataContainer
* Database validation
* Ownership verification

to prevent illegal duplicates.

### 🗄️ SQLite Persistence

Active relic state, ownership, timestamps, and history survive server restarts.

### 🎛️ Interactive GUI

Players can use:

```text
/wr menu
```

to view information about the current relic.

### 🧭 Fuzzy Relic Locator

`/wr locate` provides directional and approximate distance information without necessarily revealing the exact coordinates.

Example:

```text
The relic lies approximately
6,200 blocks away, beyond the eastern lands.
```

### 🔌 Standalone API

WorldRelics provides an API and custom events for future Surventure plugins.

This allows integrations without creating hard dependencies.

---

# ⚡ Available Relics

WorldRelics includes eight example relics.

| Relic                  | Rarity    | Passive Effect                  | Active Ability    | Description                                     |
| ---------------------- | --------- | ------------------------------- | ----------------- | ----------------------------------------------- |
| 🔥 **Phoenix Heart**   | Legendary | Fire Resistance + Regeneration  | `Phoenix Revival` | Prevents a lethal blow and restores health      |
| ⚡ **Thunder Core**     | Epic      | Speed II                        | `Storm Strike`    | Calls lightning on a targeted enemy             |
| ❄️ **Frost Crown**     | Rare      | Resistance I                    | `Frost Nova`      | Slows and freezes nearby hostile targets        |
| 👁️ **Void Eye**       | Epic      | Night Vision                    | `Void Step`       | Performs a short-range shadow teleport          |
| 🛡️ **Guardian Heart** | Rare      | Resistance II                   | `Guardian Shield` | Provides temporary damage reduction             |
| 🩸 **Blood Relic**     | Common    | Strength I                      | `Blood Surge`     | Drains vitality and restores health             |
| 👻 **Phantom Mask**    | Mythic    | Speed I + Sneaking Invisibility | `Phantom Veil`    | Grants true invisibility for a limited duration |
| 🔥 **Inferno Core**    | Common    | Fire Resistance                 | `Inferno Burst`   | Creates an area-of-effect flame eruption        |

Relics, abilities, rarities, cooldowns, and values are designed to be configurable.

---

# 🛠️ Commands & Permissions

## 👤 Player Commands

| Command      | Description                                                   |
| ------------ | ------------------------------------------------------------- |
| `/wr`        | Display WorldRelics information                               |
| `/wr status` | View the current relic, owner, status, and remaining lifetime |
| `/wr locate` | Get approximate distance and direction to an available relic  |
| `/wr info`   | View detailed information about the active relic              |
| `/wr list`   | List available relic types                                    |
| `/wr menu`   | Open the WorldRelics GUI                                      |

---

## 🔧 Admin Commands

Requires:

```text
worldrelics.admin
```

| Command                        | Description                                |
| ------------------------------ | ------------------------------------------ |
| `/wr spawn [relic_id]`         | Force a new relic spawn                    |
| `/wr despawn`                  | Despawn the active relic                   |
| `/wr reset confirm`            | Reset the relic system                     |
| `/wr give <player> <relic_id>` | Give a relic item to a player              |
| `/wr reload`                   | Reload configuration and relic definitions |

Additional command-specific permissions can be configured as required.

---

# ⚙️ Configuration

Example `config.yml`:

```yaml
plugin:
  enabled: true
  debug: false

spawn:
  enabled: true
  min-distance: 3000
  max-distance: 10000
  max-attempts: 100
  allowed-worlds:
    - world

lifetime:
  min-days: 10
  max-days: 15

relic-selection:
  common: 60
  rare: 25
  epic: 10
  legendary: 5
  mythic: 1

structure:
  enabled: true

  protection:
    enabled: true
    radius: 15
    prevent-block-break: true
    prevent-block-place: true
    prevent-explosions: true
    prevent-fire: true
```

---

# 📜 Creating Custom Relics

Custom relics can be created using YAML definitions.

Create a file inside:

```text
plugins/WorldRelics/relics/
```

Example:

```yaml
id: custom_relic

display-name: "<gradient:#FFD700:#FF8C00><bold>Custom Relic</bold></gradient>"

material: NETHER_STAR

custom-model-data: 10099

rarity: LEGENDARY

weight: 5

structure-type: STORM_ALTAR

lifetime:
  min-days: 10
  max-days: 15

lore:
  - "<gray>A custom relic forged by the unknown."

passive:
  speed:
    enabled: true
    amplifier: 1

abilities:
  storm-strike:
    enabled: true
    cooldown: 45
```

Custom relic definitions should be reloadable using:

```text
/wr reload
```

without requiring a complete server restart.

---

# 🔌 API & Integration

WorldRelics is designed to work independently while providing integration points for other Surventure plugins.

Future plugins such as:

* **BountySMP**
* **ApocalypseSMP**
* **SurventureCore**

can interact with WorldRelics through its API and events.

### Example

```java
if (WorldRelicsAPI.isRelicActive()) {
    UUID owner = WorldRelicsAPI.getRelicOwner();
    String relicType = WorldRelicsAPI.getRelicType();
}
```

---

## 📢 Events

WorldRelics provides events including:

```text
RelicSpawnEvent
RelicClaimEvent
RelicDropEvent
RelicTransferEvent
RelicExpireEvent
```

### Integration Example

A future BountySMP plugin could listen for:

```text
RelicClaimEvent
```

and automatically increase the new relic owner's bounty.

This keeps the plugins modular and prevents unnecessary hard dependencies.

---

# 🗄️ Database & Persistence

WorldRelics uses **SQLite** for persistent data.

The database stores information such as:

* Active relic
* Relic UUID
* Relic type
* Rarity
* Owner UUID
* Owner name
* Spawn location
* Claim timestamp
* Expiration timestamp
* Relic status
* Relic history

Database location:

```text
plugins/WorldRelics/worldrelics.db
```

### 🔄 Restart Recovery

The active relic survives:

* Server restarts
* Plugin reloads
* Player disconnects
* Temporary world unloads where supported

When the server starts, WorldRelics restores the active relic state and continues its remaining lifetime.

If no valid active relic exists, the plugin safely begins a new relic cycle.

---

# 📦 Installation

## Requirements

* **Java 21**
* **Paper**
* Compatible Minecraft version supported by the current WorldRelics build

### Install

1. Download the latest `WorldRelics.jar` from the **Releases** section.
2. Place the JAR inside your server's:

```text
plugins/
```

directory.

3. Start or restart the server.
4. Verify the plugin with:

```text
/plugins
```

5. Configure:

```text
plugins/WorldRelics/config.yml
```

6. Restart the server or use:

```text
/wr reload
```

---

# 🛠️ Building From Source

Clone or download the repository and build with Gradle.

### Windows

```powershell
.\gradlew.bat clean build
```

### Linux / macOS

```bash
./gradlew clean build
```

The compiled plugin will be available in:

```text
build/libs/
```

Example:

```text
WorldRelics-1.0.0-SNAPSHOT.jar
```

---

# 🌍 Surventure Season 2

WorldRelics is one of the core systems planned for **Surventure Season 2**.

Season 2 combines multiple independent plugins into one connected survival experience:

```text
                    SURVENTURE
                    SEASON 2
                        │
          ┌─────────────┼─────────────┐
          │             │             │
          ▼             ▼             ▼
     WorldRelics    BountySMP    ApocalypseSMP
          │             │             │
          ▼             ▼             ▼
       Relics        Hunting       Survival
       Powers        Contracts     Events
       Exploration   PvP           Infected World
```

### The intended gameplay loop

```text
🌍 Explore the world
        ↓
🧭 Discover a relic
        ↓
⚡ Claim its power
        ↓
🎯 Become a valuable target
        ↓
⚔️ Defend yourself
        ↓
🧟 Survive the apocalypse
        ↓
💀 Lose the relic or survive its lifetime
        ↓
🌍 A new relic awakens
        ↓
        REPEAT
```

WorldRelics is designed to create organic competition between players while giving the entire server a constantly changing objective.

---

## ⭐ One World. One Relic. One Owner.

**WorldRelics**
*Surventure Season 2*

🌍 **Explore. Claim. Protect. Repeat.**
