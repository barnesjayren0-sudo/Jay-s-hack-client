# JAY CLIENT 1.45.0 — Upgrade Notes

Polished ChatGPT upgrade pass for Minecraft 1.21.11, integrated into main.

## Included
- Premium palette refresh for ClickGUI / notifications
- Module manager search and deterministic name sorting
- Runtime module error isolation (auto-disable after 3 tick failures)
- NumberSetting step snapping + locale-stable formatting
- Safe empty ModeSetting handling
- Kotlin ComboAssist registered in KotlinBootstrap
- Version metadata **1.45.0**

## Dependencies
- Fabric API
- Fabric Language Kotlin
- Cloth Config API (`21.11.153`)

## CameraMixin
Still uses `World` (not `BlockView`) for 1.21.11 — required to avoid Freecam crash.

## Build
```bash
./gradlew clean build --no-daemon
```
