# Jay's Hack Client v1.16.0

Fabric **Minecraft 1.21.11** client focused on **sword PvP**, nethpot/UHC kits, and light utility.

Java + Kotlin hybrid · Vape-style ClickGUI · packet velocity · profiles · friends

> **Disclaimer:** For private / educational use only. Using cheats on public servers can get you banned and may violate Roblox/Minecraft ToS. You are responsible for how you use this.

---

## Requirements

| Item | Version |
|------|---------|
| Minecraft | **1.21.11** |
| Loader | Fabric Loader ≥ 0.16 |
| Java | **21+** |
| Mods | Fabric API + **fabric-language-kotlin** |

If the game says Kotlin is missing, put [fabric-language-kotlin](https://modrinth.com/mod/fabric-language-kotlin) in your `mods` folder.

---

## Install

1. Install Fabric for 1.21.11  
2. Drop into `mods/`:  
   - `fabric-api-…1.21.11.jar`  
   - `fabric-language-kotlin-….jar`  
   - `jays-hack-client-1.16.0.jar`  
3. Launch the **Fabric 1.21.11** profile  

### Build from source (PC / Termux)

```bash
cd ~/Jay-s-hack-client
git fetch origin && git reset --hard origin/main
export GRADLE_OPTS="-Xmx1536m"
./gradlew clean build --no-daemon
```

Jar output:

```text
build/libs/jays-hack-client-1.16.0.jar
```

Copy to phone (Termux example):

```bash
cp build/libs/jays-hack-client-*.jar /sdcard/Download/
```

First Kotlin build downloads the compiler and can be slow on mobile.

---

## Controls

| Key | Action |
|-----|--------|
| **Right Shift** | Open / close ClickGUI |
| **Delete** | Panic — disable all modules |
| **P** | Cycle profile (sword → aggro → nethpot → uhc → kit → legit) |
| **J** | Toggle AimAssist (default bind) |
| **N** | Toggle Velocity (default bind on module) |

### ClickGUI

- **Left click** — toggle module  
- **Right click** — settings panel  
- **Friends** tab — type name + Enter to add, click name to remove  
- Search bar filters modules  
- Favorites show ★ on the ArrayList HUD  

---

## Chat commands

Prefix: `.jay`

| Command | Description |
|---------|-------------|
| `.jay gui` | Open ClickGUI |
| `.jay sword` | Sword PvP profile |
| `.jay swordaggro` / `.jay aggro` | Aggressive sword |
| `.jay nethpot` | Pot / nethpot profile |
| `.jay uhc` | UHC-style profile |
| `.jay kit` | Kit PvP profile |
| `.jay legit` | Soft legit settings |
| `.jay profile <name>` | Apply named profile |
| `.jay profile` | Same as key **P** (cycle) |
| `.jay aimmode classic\|silent` | Aim style |
| `.jay priority closest\|lowest_hp\|crosshair` | Target priority |
| `.jay velmode soft\|medium\|strong` | Velocity strength |
| `.jay settings` | Print current settings |
| `.jay set <key> <value>` | Set a value (see below) |
| `.jay friend add\|del\|list <name>` | Friends list |
| `.jay fav <Module>` | Toggle favorite |
| `.jay config save\|load` | Force save / reload |
| `.jay off` | Disable all modules |
| `.jay panic` / `.jay unpanic` | Panic freeze / recover |
| `.jay binds` | Show key hints |

### `.jay set` keys

```text
velh | velv | aimrange | aimfov | aimsmooth
aurarange | hitbox | miss | potmin | potmax
```

Example:

```text
.jay set aimrange 4.5
.jay set potmin 0
.jay set potmax 3
.jay set miss 5
```

Config file: `.minecraft/config/jayhackclient.txt`

---

## Features (overview)

### Combat
AimAssist (classic / silent) · TriggerBot · KillAura · AutoSword · ShieldBreak · AutoBlock · Velocity · WTap / STap (mutual exclusive) · JumpReset · Criticals · CritAssist · Reach · Hitboxes · ComboAssist · AutoClicker

### Player / kits
AutoPot · PotRefill (configurable hotbar slots) · AutoGap · AutoHead · OffhandGap · Refill · AutoTotem · AutoArmor · PearlCatch · PearlAssist · InvManager

### Movement
AutoSprint · NoSlow · Speed · NoFall · NoJumpDelay

### Render / HUD
ESP · Nametags · StorageESP · FullBright · TargetHUD · ArrayList HUD (favorites, accent color, F2 hide)

### World / util
BaseFinder · SpawnerFinder · PortalFinder · PlayerRadar · PathToBase · Scaffold · AntiBot

### Systems
- **Friends** — ignored by combat aim/aura  
- **Profiles** — sword / aggro / nethpot / uhc / kit / legit / …  
- **SlotLock** — hotbar ownership between AutoSword / pots / shield  
- **Humanizer** — delays, miss chance, ping-scaled windows  
- **Death / world change** — auto-disables combat & player modules  
- **Panic** — instant full disable  

---

## Profiles (quick)

| Profile | Intent |
|---------|--------|
| `sword` | Soft aim, trigger, shield break, WTap |
| `swordaggro` | Same + velocity medium |
| `nethpot` | Pots, refill, pearl assist |
| `uhc` | Gap / head / pearl tools |
| `kit` | Sword + refill + offhand gap |
| `legit` | Minimal assist |

Cycle in-game with **P**.

---

## Notes

- **Velocity** only reduces **horizontal** knockback; Y is left alone so falling feels normal.  
- Prefer **WTap or STap**, not both.  
- Mobile / PoJav: GUI scales down; keep module count low for FPS.  
- Baritone is **not** included (keeps the jar small and Termux-friendly).  

---

## Repo

https://github.com/barnesjayren0-sudo/Jay-s-hack-client

**Author:** barnesjayren0-sudo  
**Version:** 1.16.0  
