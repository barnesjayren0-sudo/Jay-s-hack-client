# Jay's Client v1.40.0

Fabric **Minecraft 1.21.11** · dual **PvP + server utility**  
Built for desktop Fabric and mobile launchers (Mojo / Pojav).

> **Disclaimer:** For private worlds / education. Cheating on public servers can get you banned. You are responsible for how you use this.

---

## What's in 1.40

- **Sword profile** — AimAssist, TriggerBot, ComboHit, ShieldBreak, Velocity, AutoGap
- **Anarchy profile** — finders, StorageESP, LogoutSpots, HoleESP, NoFall, AutoTotem
- **Scaffold Telly** — air-place telly bridge
- **ClickGUI** — floating panels, search, RMB settings (saved)
- **Config** — module settings + keybinds + friends persist

---

## Install (Mojo / Pojav / PC)

1. Minecraft **1.21.11** + **Fabric Loader**
2. Install **Fabric API**
3. Put `jays-hack-client-1.40.0.jar` in `mods/`
4. Optional: [Baritone Fabric 1.21.11](https://github.com/cabaletta/baritone) for pathing

### Build from source (Termux)

```bash
cd ~/Jay-s-hack-client
git fetch origin && git reset --hard origin/main
./gradlew clean build --no-daemon
cp build/libs/jays-hack-client-1.40.0.jar /storage/emulated/0/Download/
```

---

## Default profiles

| Profile | Enables (summary) |
|---------|-------------------|
| **sword** | AimAssist · TriggerBot · ComboHit · AutoSword · ShieldBreak · Velocity · WTap · JumpReset · AutoGap · Hitboxes · HUD · TargetHUD · AntiBot |
| **anarchy** | BaseFinder · StorageESP · LogoutSpots · HoleESP · PlayerRadar · ESP · NoFall · AutoTotem · AutoLog · Step · Jesus · MiddleClickPearl · HUD |
| **scout** | Finders + radar + ESP (no combat) |
| **builder** | Scaffold · AutoTool · SafeWalk |

**GUI:** profile chips on the top bar · or **P** to cycle · or chat:

```text
.jay sword
.jay anarchy
.jay scout
```

---

## Controls

| Key | Action |
|-----|--------|
| **Right Shift** | ClickGUI |
| **Delete** | Panic (all off) |
| **P** | Cycle profile |
| **R** | KillAura |
| **J** | AimAssist |
| **T** | TriggerBot |
| **N** | Velocity |
| **X** | ESP |

LMB = toggle · **RMB** = settings / keybind · drag panel headers to move (saved)

---

## Commands

```text
.jay help
.jay gui
.jay sword | anarchy | scout | builder | nethpot
.jay toggle <module>
.jay scan | storage | radar
.jay goto <x> <y> <z> | stoppath
.jay friend add|del|list
.jay config save|load|reset
.jay panic | off
```

---

## Playtest checklist (sword + telly + finders)

1. `.jay sword` → duel dummy / friend — aim + trigger feel OK  
2. Scaffold **Mode=Telly** → hold W + blocks — jumps and places in air  
3. `.jay scout` or anarchy → StorageESP / BaseFinder markers on HUD  
4. LogoutSpots — leave alt, confirm marker  
5. **Delete** panic clears modules  

Report anything that crashes or desyncs hotbar.

---

## Policy

Finders scan **loaded chunks only**. No seed/RNG locate modules.

**Repo:** https://github.com/barnesjayren0-sudo/Jay-s-hack-client  
**Version:** 1.40.0
