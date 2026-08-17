# Jay's Hack Client

**Version 1.1.1**  
Minecraft **1.21.11** (Fabric) · Sword PvP focused

---

## What's new in 1.1.1

- On-screen **ArrayList HUD** (enabled modules)
- **Config** save/load (enabled modules)
- **Friends** list (KillAura / TriggerBot skip friends)
- **Speed** movement module
- **StorageESP** (chests)
- Cleaner commands: `.jay`
- Safer tick error handling

---

## Modules

| Module | Category | Description |
|--------|----------|-------------|
| KillAura | Combat | Sword-only aura |
| TriggerBot | Combat | Attack on crosshair |
| AutoClicker | Combat | ~8 CPS swings |
| AutoSword | Combat | Best hotbar sword |
| Criticals | Combat | Crit helper |
| Velocity | Combat | Less knockback |
| WTap | Combat | Sprint reset assist |
| Reach | Combat | Reach helper |
| AutoSprint | Movement | Keep sprint |
| NoSlow | Movement | Less item slowdown |
| Speed | Movement | Faster ground move |
| ESP | Render | Player glow |
| FullBright | Render | Max brightness |
| StorageESP | Render | Chest glow |
| HUD | Render | ArrayList overlay |

---

## Build

```bash
git clone https://github.com/barnesjayren0-sudo/Jay-s-hack-client.git
cd Jay-s-hack-client
gradle wrapper --gradle-version 8.10.2   # if needed
./gradlew build
```

Jar: `build/libs/jays-hack-client-1.1.1.jar`

---

## Controls & commands

| Key | Action |
|-----|--------|
| Right Shift | Module menu |
| R | Toggle KillAura |
| G | Toggle AutoSprint |

```
.jay help
.jay list
.jay toggle <module>
.jay friend add <name>
.jay friend del <name>
.jay friend list
.jay config save
.jay config load
```

---

**Author:** barnesjayren0-sudo  
https://github.com/barnesjayren0-sudo/Jay-s-hack-client
