# Jay's Hack Client

**Fabric · Minecraft 1.21.11 · v1.43.0**

PvP + server utility client with floating ClickGUI, persistent config, profiles, finders, and optional Baritone pathing.

> Loaded-chunk tools only. No seed / RNG locate modules.

---

## Dependencies (Prestige-style stack)

| Dependency | Role | Version |
|------------|------|---------|
| **Fabric API** | Core Fabric library | `0.141.4+1.21.11` |
| **Fabric Language Kotlin** | Kotlin runtime | `1.13.0+kotlin.2.1.0` |
| **Cloth Config API** | Config / settings GUI library | `21.11.153` (1.21.11) |

These are declared in `build.gradle` and listed under `depends` in `fabric.mod.json`.

**Runtime:** install **Fabric API**, **Fabric Language Kotlin**, and **Cloth Config** in `mods/` (or let the launcher pull them). The ClickGUI remains Jay’s custom floating UI; Cloth Config is on the classpath for settings integration.

---

## Features

### Combat
KillAura · AimAssist · TriggerBot · ComboHit · CritAssist · SoftBlink · TargetStrafe  
AutoSword · ShieldBreak · AutoBlock · SwordBlock · Velocity · WTap · STap  
JumpReset · NoJumpDelay · Criticals · Reach · Hitboxes · AutoClicker  
AutoPot · PotRefill · AnchorMacro · AutoWeb · AntiFireball · AntiBot

### Movement
AutoSprint · KeepSprint · NoSlow (shield-aware) · Speed · NoFall · SafeWalk  
Scaffold (**Normal / Telly / Godbridge / Tower**) · Fly · Step · Jesus · BoatFly · YClip

### Player / inventory
AutoArmor · AutoTotem (HardHP) · AutoGap · OffhandGap · AutoHead · Refill  
InvManager · InvSort · AutoReplenish · FastPlace · NoBreakDelay  
PearlAssist · PearlCatch · MiddleClickPearl · MiddleClickFriend

### Render / HUD
ESP · Nametags · StorageESP · FullBright · Freecam (`@`)  
HUD · InfoHUD · TargetHUD · ReachHUD · CombatHUD · HitParticles  
PlayerBoxes · PearlTrajectory · HoleESP

### World / utility
BaseFinder · FarmFinder · StorageFinder · BuildFinder · BeaconFinder  
SpawnerFinder · PortalFinder · PlayerRadar · DofNear · LogoutSpots  
NewChunks · Waypoints · PathToBase · BaritoneControl · AutoTool  
AutoTrap · Burrow · Surround · SelfTrap · AutoCrystal · HoleFill · AutoLog · AntiVoid

### Client systems
- **ClickGUI** — floating panels, search, RMB per-module settings, saved layout
- **Config** — modules, settings, keybinds, friends, favorites, waypoints, panel positions
- **Profiles** — sword · anarchy · scout · builder · nethpot · utility
- **Slot lock** · **shared combat target** · **panic**

---

## Install

### Requirements
| Item | Version |
|------|---------|
| Minecraft | **1.21.11** |
| Fabric Loader | latest for 1.21.11 |
| Fabric API | required |
| Fabric Language Kotlin | required |
| Cloth Config | required (1.21.11 build) |
| Baritone (optional) | Fabric 1.21.11 |

### Jar install (PC / Mojo / Pojav)
1. Fabric for **1.21.11**
2. Mods folder: **Fabric API** + **Fabric Language Kotlin** + **Cloth Config** + `jays-hack-client-1.43.0.jar`
3. Optional: Baritone

### Build (Termux / PC)

```bash
cd ~/Jay-s-hack-client
git fetch origin && git reset --hard origin/main
./gradlew clean build --no-daemon
cp build/libs/jays-hack-client-*.jar /storage/emulated/0/Download/
```

---

## Controls

| Key | Action |
|-----|--------|
| **Right Shift** | ClickGUI |
| **Delete** | Panic |
| **P** | Cycle profile |
| **R** | KillAura |
| **J** | AimAssist |
| **T** | TriggerBot |
| **N** | Velocity |
| **X** | ESP |
| **@** | Freecam |

LMB = toggle · **RMB** = settings · drag panel headers (saved)

---

## Chat commands

```text
.jay help
.jay gui
.jay sword | anarchy | scout | builder | nethpot | utility
.jay toggle <ModuleName>
.jay scan | storage | radar | near
.jay wp save <name> | wp goto <name> | wp list | wp del <name>
.jay goto <x> <y> <z> | path | stoppath
.jay friend add|del|list <name>
.jay fav <ModuleName>
.jay set aimrange|hitbox|reach|velh <n>
.jay config save|load|reset
.jay panic | off | unpanic
```

---

## Policy

Finders scan **loaded chunks only**. No seed/RNG locate modules.

**Repo:** https://github.com/barnesjayren0-sudo/Jay-s-hack-client  
**Version:** 1.43.0 · **MC:** 1.21.11 (Fabric)
