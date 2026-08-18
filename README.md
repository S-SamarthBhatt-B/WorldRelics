# WorldRelics — Surventure Season 2 Core Plugin

**WorldRelics** is a production-ready Minecraft Paper plugin designed for **Surventure Season 2**.

The plugin introduces a server-wide competitive event built around one strict global rule:

> **ONE WORLD. ONE RELIC. ONE OWNER.**

---

## 📖 Table of Contents
1. [Overview](#overview)
2. [Core Concept & Lifecycle](#core-concept--lifecycle)
3. [Features](#features)
4. [Relics & Abilities](#relics--abilities)
5. [Commands & Permissions](#commands--permissions)
6. [Configuration](#configuration)
7. [Creating Custom Relics](#creating-custom-relics)
8. [API & Event Integration](#api--event-integration)
9. [Database & Restart Recovery](#database--restart-recovery)
10. [Build & Installation](#build--installation)

---

## 🌟 Overview

At any given time, exactly **one** World Relic exists across the entire server.

Relics spawn at safe surface locations far away from all online players (between 3,000 to 10,000 blocks away by default) inside themed structures (altars, temples, ruined fortresses).

Players explore the world, locate the relic, claim it, and gain powerful passive and active abilities. The owner becomes a prized target for other players!

A relic lasts for a configurable duration of **10 to 15 Minecraft days** (where 1 MC day = 20 real minutes). When a relic expires, it vanishes back into legend, and a brand new relic awakens somewhere else in the world.

If the owner dies, the relic drops naturally so another player can claim it.

---

## 🔄 Core Concept & Lifecycle

```
NO_RELIC ➔ RESPAWNING ➔ AVAILABLE (Structure Spawned) ➔ CLAIMED (Player Owner) ➔ EXPIRED ➔ RESPAWNING
```

If owner dies:
```
CLAIMED ➔ OWNER_DEATH ➔ ITEM_DROPPED ➔ AVAILABLE ➔ NEW_OWNER
```

---

## 🚀 Features

- **Strict Single-Relic Enforcement**: Guaranteed single active relic across the server.
- **Async Location Engine**: Searches for spawn points 3,000–10,000 blocks away from *all* online players without freezing the server main thread.
- **Themed Structures & Protection**: Generates shrines/altars with temporary structure protection (block place/break, fire, and explosion protection).
- **Anti-Duplication**: `PersistentDataContainer` UUID matching combined with SQLite validation wipes illegal duplicates instantly.
- **8 Pre-Configured Relics**: Phoenix Heart, Thunder Core, Frost Crown, Void Eye, Guardian Heart, Blood Relic, Phantom Mask, Inferno Core.
- **Interactive Control GUI**: `/wr menu` and `/wr` history log.
- **Fuzzy Coordinate Locator**: `/wr locate` provides relative direction & estimated distance (`~6,200 blocks far beyond the eastern lands`) without spoiling exact coordinates.
- **Standalone API & Events**: Clean API (`WorldRelicsAPI`) and Paper events (`RelicSpawnEvent`, `RelicClaimEvent`, `RelicDropEvent`, `RelicTransferEvent`, `RelicExpireEvent`) for zero-dependency integration with future Surventure plugins like BountySMP & ApocalypseSMP.
- **SQLite Restart Recovery**: Expiration timers and active state persist across server reboots.

---

## ⚡ Relics & Abilities

| Relic | Rarity | Passive Effect | Active Ability | Description |
| :--- | :--- | :--- | :--- | :--- |
| **Phoenix Heart** | Legendary | Fire Resistance & Regen I | `Phoenix Revival` | Prevent death & restore health on lethal blow |
| **Thunder Core** | Epic | Speed II | `Storm Strike` | Strike lightning on targeted enemies |
| **Frost Nova** | Rare | Resistance I | `Frost Nova` | Freeze & slow all nearby hostile targets |
| **Void Eye** | Epic | Night Vision | `Void Step` | Short-range shadow teleportation |
| **Guardian Heart** | Rare | Resistance II | `Guardian Shield` | Temporary massive damage reduction |
| **Blood Relic** | Common | Strength I | `Blood Surge` | Siphon vitality & heal instantly |
| **Phantom Mask** | Mythic | Speed I & Invisibility (Sneaking) | `Phantom Veil` | Full true invisibility for 10s |
| **Inferno Core** | Common | Fire Resistance | `Inferno Burst` | Area-of-effect flame eruption |

---

## 🛠️ Commands & Permissions

### Player Commands
- `/wr status` — View active relic state, owner, and remaining lifetime.
- `/wr locate` — Get fuzzy distance & compass direction to unclaimed relic.
- `/wr info` — View detailed info on the active relic.
- `/wr list` — List all loaded relic types.
- `/wr menu` — Open interactive GUI control center.

### Admin Commands (`worldrelics.admin`)
- `/wr spawn [relic_id]` — Force spawn a new relic cycle.
- `/wr despawn` — Despawn active relic immediately.
- `/wr reset confirm` — Reset relic system (requires `confirm` safety argument).
- `/wr give <player> <relic_id>` — Give a relic item to a player.
- `/wr reload` — Reload configuration & relic definitions.

---

## ⚙️ Configuration (`config.yml`)

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

## 📜 Creating Custom Relics

Add new YAML files in `plugins/WorldRelics/relics/my_relic.yml`:

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
  - "<gray>A custom relic definition."

passive:
  speed:
    enabled: true
    amplifier: 1

abilities:
  storm-strike:
    enabled: true
    cooldown: 45
```

---

## 🔌 API & Event Integration

Other plugins (such as BountySMP or ApocalypseSMP) can easily hook into WorldRelics without hard dependencies:

```java
// Check if a relic is active
if (WorldRelicsAPI.isRelicActive()) {
    UUID owner = WorldRelicsAPI.getRelicOwner();
    String relicType = WorldRelicsAPI.getRelicType();
}
```

### Events
- `RelicSpawnEvent`
- `RelicClaimEvent` (Cancellable)
- `RelicDropEvent`
- `RelicTransferEvent`
- `RelicExpireEvent`

---

## 🛠️ Build & Installation

### Requirements
- **Java 21 JDK**
- **Paper 1.20.4+ / 1.20.6 / 1.21**

### Build with Gradle
```bash
./gradlew build
```
Output artifact location: `build/libs/WorldRelics-1.0.0-SNAPSHOT.jar`.
Drop the JAR into your server's `plugins/` directory and restart!
