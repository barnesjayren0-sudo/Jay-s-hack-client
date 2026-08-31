# Jay's Hack Client

**Fabric · Minecraft 1.21.11 · v1.45.0**

PvP + server utility client with compact ClickGUI, resilient module runtime, Cloth Config, Kotlin modules, and optional Baritone pathing.

> Loaded-chunk tools only. No seed / RNG locate modules.

## What's new in 1.45.0

- Premium GUI palette refresh
- Module manager search + sorted lists
- Runtime tick error isolation (auto-disable after repeated failures)
- Safer NumberSetting / ModeSetting
- Kotlin modules: SmartKeepSprint, LegitBridgeAssist, CompactWatermark, ComboAssist
- Cloth Config screen (`.jay cloth`)
- Atomic config save

## Dependencies

| Dependency | Role |
|------------|------|
| Fabric API | Core Fabric library |
| Fabric Language Kotlin | Kotlin runtime |
| Cloth Config API | Settings GUI (`21.11.153`) |

## Install

1. Minecraft **1.21.11** + Fabric Loader
2. Mods: Fabric API + Fabric Language Kotlin + Cloth Config + `jays-hack-client-1.45.0.jar`
3. Optional: Baritone Fabric 1.21.11

### Build

```bash
cd ~/Jay-s-hack-client
git fetch origin && git reset --hard origin/main
./gradlew clean build --no-daemon
cp build/libs/jays-hack-client-*.jar /storage/emulated/0/Download/
```

## Controls

| Key | Action |
|-----|--------|
| Right Shift | ClickGUI |
| Delete | Panic |
| P | Cycle profile |
| R / J / T / N / X / @ | Aura / Aim / Trigger / Vel / ESP / Freecam |

## Commands

```text
.jay help | gui | cloth
.jay sword | anarchy | scout | builder
.jay wp save|goto|list|del <name>
.jay scan | radar | panic | off
```

**Repo:** https://github.com/barnesjayren0-sudo/Jay-s-hack-client  
**Version:** 1.45.0
