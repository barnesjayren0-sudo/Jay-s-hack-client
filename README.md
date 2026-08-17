# Jay's Hack Client

**Version 1.2.0** · Minecraft **1.21.11** (Fabric)

Sword PvP focused client with **advanced base finders**, cleaner HUD, and legit-style combat timing.

---

## Sections

### Combat
KillAura · TriggerBot · AutoClicker · AutoSword · Criticals · Velocity · WTap · Reach

### Movement
AutoSprint · NoSlow · Speed

### Render
ESP · FullBright · HUD · StorageESP

### World / Base Finders (NEW)
| Module | Description |
|--------|-------------|
| **BaseFinder** | Scans loaded chunks for chests, barrels, shulkers, spawners, furnaces |
| **SpawnerFinder** | Alerts when a mob spawner is nearby |
| **PlayerRadar** | Lists nearby players with distance |
| **PortalFinder** | Flags nether portals in loaded areas |

---

## Legit / quieter behavior

- Randomized attack delays (less robotic CPS)
- Friends ignored by combat modules
- Optional silent-style targeting (less snap)
- No claims of being fully undetectable — good anticheats still ban. Use smart configs.

---

## Build

```bash
./gradlew build
```

Jar: `build/libs/jays-hack-client-1.2.0.jar`

---

## Commands

```
.jay list
.jay toggle <module>
.jay friend add|del|list <name>
.jay config save|load
.jay scan          # force BaseFinder report
.jay radar         # nearby players
```

**Keys:** Right Shift = menu · R = KillAura · G = AutoSprint

---

https://github.com/barnesjayren0-sudo/Jay-s-hack-client
