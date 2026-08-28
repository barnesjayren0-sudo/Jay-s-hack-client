# Jay's Utility Client v1.24.0

Fabric **Minecraft 1.21.11** · **utility-first** (world find, pathing, QoL)

Combat modules still exist but are optional — defaults and profiles target exploration / server utility, not PvP.

> **Disclaimer:** Private / educational use only. Unauthorized cheats on public servers can get you banned. You are responsible for use.

---

## Focus (1.24)

| Area | Tools |
|------|--------|
| **World** | BaseFinder · StorageFinder · BuildFinder · BeaconFinder · SpawnerFinder · PortalFinder · PlayerRadar |
| **Path** | PathToBase · Baritone bridge · `.jay goto x y z` |
| **QoL** | FullBright · ESP · Nametags · AutoTool · Scaffold · SafeWalk · AutoSprint · Inv helpers |
| **Optional** | Combat tab still available if you enable it |

**Not included:** Randar / seed-RNG locate — only **loaded chunks** and render-distance players.

---

## Requirements

| Item | Version |
|------|---------|
| Minecraft | **1.21.11** |
| Loader | Fabric ≥ 0.16 |
| Java | **21+** |
| Mods | Fabric API + fabric-language-kotlin |
| Optional | Baritone (pathing) |

---

## Build

```bash
cd ~/Jay-s-hack-client
git fetch origin && git reset --hard origin/main
export GRADLE_OPTS="-Xmx1536m"
./gradlew clean build --no-daemon
cp build/libs/jays-hack-client-1.24.0.jar /sdcard/Download/
```

---

## Controls

| Key | Action |
|-----|--------|
| **Right Shift** | ClickGUI (opens on **World** tab) |
| **Delete** | Panic (all off) |
| **P** | Cycle utility profiles |

### ClickGUI
Bottom profiles: **scout** · **builder** · **explore**  
LMB toggle · RMB settings/keybind · Friends · search

---

## Commands

```text
.jay gui
.jay scan | storage | build | radar
.jay path | goto <x> <y> <z> | stoppath
.jay profile scout|builder|explore|utility
.jay friend add|del|list <name>
.jay config save|load
.jay panic | unpanic | off
```

Config: `.minecraft/config/jayhackclient.txt`

---

**Repo:** https://github.com/barnesjayren0-sudo/Jay-s-hack-client  
**Version:** 1.24.0 (utility client)
