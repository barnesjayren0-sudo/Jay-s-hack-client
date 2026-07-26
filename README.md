# ⚔️ Jay's Hack Client

**Minecraft 1.21.11 • Sword PvP Focused Client**

A clean Fabric client built specifically for sword combat.

---

## Features

| Module | Description |
|--------|-------------|
| **KillAura** | Sword-only KillAura with smart targeting |
| **AutoSword** | Automatically switches to best sword |
| **Reach** | Extended attack reach |
| **Criticals** | Packet-based critical hits |
| **AutoSprint** | Perfect sprint for combos |
| **Velocity** | Reduce / cancel knockback |
| **ESP** | See players through walls |
| **NoSlow** | No slowdown while using items |

---

## Requirements

- Minecraft **1.21.11**
- Fabric Loader
- Fabric API
- Java 21+

---

## How to Build the .jar

```bash
# 1. Clone the repo
git clone https://github.com/barnesjayren0-sudo/Jay-s-hack-client.git
cd Jay-s-hack-client

# 2. Build
./gradlew build
```

After building, the jar will be here:

```
build/libs/jays-hack-client-1.0.0.jar
```

### Creating a GitHub Release

1. Go to your repo → **Releases** → **Create a new release**
2. Tag: `v1.0.0`
3. Upload the `.jar` from `build/libs/`
4. Publish

---

## Installation

1. Install Fabric for Minecraft 1.21.11
2. Put `jays-hack-client-1.0.0.jar` into your `.minecraft/mods` folder
3. Launch the game

---

## Controls

- **Right Shift** → Open ClickGUI (coming soon)

---

## Project Structure

```
src/main/java/com/jay/hackclient/
├── JayHackClient.java          ← Main entrypoint
├── module/
│   ├── Module.java
│   ├── ModuleManager.java
│   └── modules/
│       ├── KillAura.java
│       ├── AutoSword.java
│       ├── Reach.java
│       ├── Criticals.java
│       ├── AutoSprint.java
│       ├── Velocity.java
│       ├── ESP.java
│       └── NoSlow.java
└── resources/
    └── fabric.mod.json
```

---

## Credits

Created by **barnesjayren0-sudo**  
Sword PvP only. No trash modules.
