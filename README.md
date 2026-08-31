# Jay's Hack Client

**Fabric · Minecraft 1.21.11 · v1.46.0**

PvP + server utility client — presets, theme engine, HUD editor, keybind manager, first-launch setup, performance dashboard.

> Loaded-chunk tools only. No seed / RNG locate modules.

## What's new in 1.46.0

1. **Presets** — Legit / PvP / Survival / Performance (`.jay preset` or GUI chips)
2. **Custom profiles** — save/load under `config/jayprofiles/` (GUI **Prof**)
3. **ClickGUI** — search, favorites (middle-click), descriptions side panel, tool bar
4. **HUD Editor** — drag FPS/ping/coords/arraylist/… (`.jay hud`)
5. **Notifications** — styled toasts (info/success/warn/error)
6. **PerfDashboard** module — FPS, memory, entities, ping
7. **Theme engine** — Cyan / Purple / Red / Green / Custom (`.jay theme`)
8. **Keybind manager** — conflict highlighting (`.jay keys`)
9. **Debug screen** — modules, FPS, memory, server (`.jay debug`)
10. **First-launch wizard** — theme + preset once
11. **Crash protection** — module auto-disable after 3 tick errors

## Quick commands

```text
.jay gui | cloth | preset legit|pvp|survival|performance
.jay profiles | hud | keys | debug | theme
.jay sword | anarchy | panic
```

## GUI toolbar

| Button | Opens |
|--------|--------|
| Prof | Profile / preset manager |
| Keys | Keybind manager |
| HUD | HUD editor |
| Dbg | Debug screen |
| Theme | Cycle theme |

Middle-click a module in the ClickGUI to ★ favorite it.

## Build

```bash
cd ~/Jay-s-hack-client
git fetch origin && git reset --hard origin/main
./gradlew clean build --no-daemon
cp build/libs/jays-hack-client-*.jar /storage/emulated/0/Download/
```

**Deps:** Fabric API + Fabric Language Kotlin + Cloth Config  
**Repo:** https://github.com/barnesjayren0-sudo/Jay-s-hack-client
