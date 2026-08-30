# Jay's Hack Client

**Fabric · Minecraft 1.21.11 · v1.42.1**

PvP + server utility client with floating ClickGUI, persistent config, profiles, finders, and optional Baritone pathing.

> Loaded-chunk tools only. No seed / RNG locate modules.

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
- **ClickGUI** — floating Meteor-style panels, search, RMB per-module settings, saved layout
- **Config** — modules, settings, keybinds, friends, favorites, waypoints, panel positions
- **Profiles** — sword · anarchy · scout · builder · nethpot · utility · more
- **Slot lock** — AutoTotem / AutoSword / ShieldBreak don’t fight the hotbar
- **Shared combat target** — AimAssist / KillAura / ComboHit
- **Panic** — one key disables everything

---

## Install

### Requirements
| Item | Version |
|------|---------|
| Minecraft | **1.21.11** |
| Fabric Loader | latest for 1.21.11 |
| Fabric API | required |
| Baritone (optional) | Fabric build for 1.21.11 |

### Jar install (PC / Mojo / Pojav)
1. Install Fabric for **1.21.11**
2. Drop **Fabric API** into `mods/`
3. Drop `jays-hack-client-1.42.1.jar` into `mods/`
4. Optional: Baritone jar for `#goto` / pathing

### Build (Termux / PC)

```bash
cd ~/Jay-s-hack-client
git fetch origin && git reset --hard origin/main
./gradlew clean build --no-daemon
cp build/libs/jays-hack-client-*.jar /storage/emulated/0/Download/
```

Output: `build/libs/jays-hack-client-1.42.1.jar`

---

## Controls

| Key | Action |
|-----|--------|
| **Right Shift** | Open / close ClickGUI |
| **Delete** | Panic — all modules off |
| **P** | Cycle profile |
| **R** | KillAura |
| **J** | AimAssist |
| **T** | TriggerBot |
| **N** | Velocity |
| **X** | ESP |
| **@** | Freecam |

- **LMB** on module = toggle  
- **RMB** on module = settings (sliders, keybind, **Reset defaults**)  
- Drag panel headers to move (positions saved)

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
.jay baritone <args>          # or use #commands if Baritone is installed
.jay friend add|del|list <name>
.jay fav <ModuleName>         # pin favorite
.jay set aimrange|hitbox|reach|velh <n>
.jay config save|load|reset
.jay panic | off | unpanic
```

---

## Profiles

| Profile | Focus |
|---------|--------|
| **sword** | AimAssist, TriggerBot, ComboHit, AutoSword, ShieldBreak, Velocity, WTap, AutoGap, Hitboxes |
| **anarchy** | Finders, StorageESP, LogoutSpots, HoleESP, NoFall, AutoTotem, Step, Jesus |
| **scout** | Finders + radar + ESP (light combat) |
| **builder** | Scaffold, AutoTool, SafeWalk |
| **nethpot** | Pots / refill oriented |
| **utility** | Finders + QoL without rage combat |

Apply via GUI chips, **P**, or `.jay sword` / `.jay anarchy` / …

---

## Waypoints

```text
.jay wp save home
.jay wp list
.jay wp goto home
.jay wp del home
```

Saved into config. LogoutSpots can auto-save `lo_<player>` when **AutoWaypoint** is on.

---

## Scaffold modes

| Mode | Behavior |
|------|----------|
| **Normal** | Place under feet |
| **Telly** | Jump + air place (kit bridging) |
| **Godbridge** | Edge place style |
| **Tower** | Hold jump — place up |

RMB **Scaffold** → Mode setting.

---

## Playtest checklist

1. `.jay sword` — aim / trigger feel smooth  
2. Scaffold **Telly** — W + blocks, air places  
3. `.jay scout` or **anarchy** — StorageESP / BaseFinder markers  
4. `.jay wp save test` → `wp list` → `wp goto test`  
5. **Delete** panic clears modules  

Report crashes or hotbar fights with the compile/log if possible.

---

## Policy

- Finders scan **loaded chunks only**
- No seed cracking / Randar-style modules
- Use only where you are allowed to (private testing, own server, etc.)

---

## Links

- **Repo:** https://github.com/barnesjayren0-sudo/Jay-s-hack-client  
- **Version:** 1.42.1  
- **MC:** 1.21.11 (Fabric)
