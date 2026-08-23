# Jay's Hack Client v1.17.1

Fabric **Minecraft 1.21.11** · sword PvP · nethpot/UHC kits · light utility

Java + Kotlin hybrid · Vape-style ClickGUI · packet velocity · profiles · friends

> **Disclaimer:** Private / educational use only. Cheats on public servers can get you banned. You are responsible for use.

---

## What's new in 1.17.1 (polish)

- **AutoTool** — uses `getSelectedSlot()` / `setSelectedSlot()` (same API as AutoSword; no private field access)
- **SafeWalk** — non-sticky: only forces sneak on edge while moving; releases when safe or airborne; respects real sneak key
- **MiddleClickFriend** · **AutoTool** · **SafeWalk** from 1.17.0 kept

---

## Requirements

| Item | Version |
|------|---------|
| Minecraft | **1.21.11** |
| Loader | Fabric ≥ 0.16 |
| Java | **21+** |
| Mods | Fabric API + fabric-language-kotlin |

---

## Build (Termux / PC)

```bash
cd ~/Jay-s-hack-client
git fetch origin && git reset --hard origin/main
export GRADLE_OPTS="-Xmx1536m"
./gradlew clean build --no-daemon
cp build/libs/jays-hack-client-1.17.1.jar /sdcard/Download/
```

**Always `reset --hard origin/main` after GitHub updates** — do not merge in mobile Git apps.

---

## Controls

| Key | Action |
|-----|--------|
| **Right Shift** | ClickGUI |
| **Delete** | Panic (all off) |
| **P** | Cycle profile |
| **J** | AimAssist (module bind) |
| **Middle mouse** | Friend toggle (MiddleClickFriend) |

### ClickGUI
LMB toggle · RMB settings · Friends tab · search · favorites ★

---

## Commands (`.jay`)

```text
.jay gui | sword | aggro | nethpot | kit | profile [name]
.jay aimmode classic|silent
.jay priority closest|lowest_hp|crosshair
.jay velmode soft|medium|strong
.jay set aimrange|aimfov|miss|potmin|potmax|hitbox|velh <n>
.jay friend add|del|list <name>
.jay fav <Module>
.jay config save|load
.jay panic | unpanic | off | binds
```

Config: `.minecraft/config/jayhackclient.txt`

---

## Modules (high level)

**Combat:** AimAssist · TriggerBot · KillAura · AutoSword · ShieldBreak · Velocity · WTap/STap · JumpReset · Hitboxes · ComboAssist  
**Player:** AutoPot · PotRefill · PearlAssist · InvManager · AutoTotem · Refill  
**Movement:** AutoSprint · SafeWalk · NoSlow · Speed  
**World:** Scaffold · AutoTool · BaseFinder · …  
**Misc:** MiddleClickFriend · AntiBot · HUD

---

**Repo:** https://github.com/barnesjayren0-sudo/Jay-s-hack-client  
**Version:** 1.17.1  
