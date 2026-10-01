# Jay Utility Client

**Fabric · Minecraft 1.21.11 · v1.56.0**

PvP + server utility client — presets, theme engine, HUD editor, keybind manager, first-launch setup, performance dashboard.

> Loaded-chunk tools only. No seed / RNG locate modules.

## What's new in 1.56.0

1. **Vape-style ClickGUI** — dark minimal panels, category sidebar, module rows with accent status bars, inline expandable settings (toggles / sliders / mode cycles), smooth ease-out animations
2. **Legit-first module pass** — humanized delays, miss chance, packet throttling and capped defaults across combat, movement, world and utility modules
3. **AutoClicker CPS ranges** — randomized 8–12 CPS by default instead of a flat rate
4. **BackTrack defaults** — 120 ms Smart window (was 150 ms)
5. **Accent picker** — soft purple default with red / blue / green / gold presets
6. **Presets** — Legit / PvP / Survival / Performance (`.jay preset` or GUI chips)
7. **Custom profiles** — save/load under `config/jayprofiles/` (GUI **Prof**)
8. **HUD Editor** — drag FPS/ping/coords/arraylist/… (`.jay hud`)
9. **Theme engine** — Cyan / Purple / Red / Green / Custom (`.jay theme`)
10. **Keybind manager** — conflict highlighting (`.jay keys`)
11. **Crash protection** — module auto-disable after 3 tick errors

## Quick commands

```text
.jay gui | cloth | preset legit|pvp|survival|performance
.jay profiles | hud | keys | debug | theme
.jay sword | anarchy | panic
```

## ClickGUI controls

| Action | Result |
|--------|--------|
| Left-click module row | Toggle module |
| Right-click module row | Expand / collapse settings |
| Hold row 450 ms | Bind a key |
| Middle-click row | ★ favorite |
| Drag panel header | Move panel |
| Right-click sidebar category | Collapse panel |
| Search box (top right) | Filter modules |

## Build

```bash
cd Jay-s-hack-client
./gradlew clean build --no-daemon
cp build/libs/jays-hack-client-*.jar ~/.minecraft/mods/
```

**Deps:** Fabric API + Fabric Language Kotlin + Cloth Config
**Repo:** https://github.com/barnesjayren0-sudo/Jay-s-hack-client
