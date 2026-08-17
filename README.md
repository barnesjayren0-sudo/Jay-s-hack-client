# Jay's Hack Client

**Version 1.1.0**  
Minecraft **1.21.11** (Fabric) · Sword PvP focused

---

## Modules

| Module | Category | Key | Description |
|--------|----------|-----|-------------|
| KillAura | Combat | R | Sword-only aura with look + timing |
| TriggerBot | Combat | - | Attacks when crosshair is on a player |
| AutoClicker | Combat | - | Timed left-click CPS for sword |
| AutoSword | Combat | - | Switches to best hotbar sword |
| Criticals | Combat | - | Packet crit helper |
| Velocity | Combat | - | Reduces horizontal knockback |
| WTap | Combat | - | Sprint-reset style taps |
| Reach | Combat | - | Extra attack range helper |
| AutoSprint | Movement | G | Keeps sprint for combos |
| NoSlow | Movement | - | Less slowdown while using items |
| ESP | Render | - | Glow outline on players |
| FullBright | Render | - | Max gamma / night vision style |

---

## Requirements

- Java **21+**
- Minecraft **1.21.11**
- Fabric Loader **0.16+**
- Fabric API for 1.21.11

---

## Build

```bash
git clone https://github.com/barnesjayren0-sudo/Jay-s-hack-client.git
cd Jay-s-hack-client

# If gradlew is missing:
gradle wrapper --gradle-version 8.10.2

./gradlew build
```

Output:
```
build/libs/jays-hack-client-1.1.0.jar
```

Windows:
```bat
gradlew.bat build
```

---

## Install

1. Install Fabric for 1.21.11
2. Put `jays-hack-client-1.1.0.jar` in `.minecraft/mods`
3. Also install Fabric API
4. Launch Minecraft
5. Press **Right Shift** for menu / status

---

## Controls

| Key | Action |
|-----|--------|
| **Right Shift** | Client menu (lists modules + toggle help) |
| **R** | Toggle KillAura |
| **G** | Toggle AutoSprint |

Chat commands (type in chat):
```
.jay help
.jay list
.jay toggle <module>
```

---

## Notes

- Client-side only. Many servers detect combat mods — use at your own risk.
- Reach / Velocity / NoSlow are improved stubs; full power needs mixins (planned).
- Built for learning Fabric + sword PvP tooling.

---

**Author:** barnesjayren0-sudo  
**Repo:** https://github.com/barnesjayren0-sudo/Jay-s-hack-client
