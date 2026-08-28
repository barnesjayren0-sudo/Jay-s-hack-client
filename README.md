# Jay's Client v1.25.0

Fabric **Minecraft 1.21.11** · **PvP + server utility**

Sword / nethpot combat · loaded-chunk finders · Baritone pathing · QoL

> **Disclaimer:** Private / educational use only. Cheats on public servers can get you banned. You are responsible for use.

---

## Dual focus

| Side | Tools |
|------|--------|
| **PvP** | KillAura · AimAssist · TriggerBot · Velocity · WTap/STap · ShieldBreak · Hitboxes · Reach · profiles sword/nethpot/legit |
| **Server utility** | BaseFinder · StorageFinder · BuildFinder · BeaconFinder · PlayerRadar · PathToBase · `.jay goto` · ESP · FullBright |

Finders only scan **loaded chunks** / render distance — not seed/RNG locate.

---

## Profiles (GUI bottom or `.jay profile`)

| Profile | Use |
|---------|-----|
| **sword** | PvP sword stack |
| **nethpot** | Pot PvP |
| **scout** | Finders + radar + ESP |
| **builder** | Scaffold / AutoTool / SafeWalk |
| **explore** | Travel + radar + portals |

Cycle: **P** key · `scout → builder → explore → sword → nethpot → legit`

---

## Build

```bash
cd ~/Jay-s-hack-client
git fetch origin && git reset --hard origin/main
./gradlew clean build --no-daemon
cp build/libs/jays-hack-client-1.25.0.jar /sdcard/Download/
```

---

## Controls

| Key | Action |
|-----|--------|
| **Right Shift** | ClickGUI |
| **Delete** | Panic |
| **P** | Cycle profile |

LMB toggle · RMB settings/keybind · Friends tab

---

## Commands

```text
.jay gui
.jay sword | nethpot | scout | builder | explore
.jay scan | storage | build | radar
.jay path | goto x y z | stoppath
.jay friend add|del|list
.jay panic | off
```

**Repo:** https://github.com/barnesjayren0-sudo/Jay-s-hack-client  
**Version:** 1.25.0
