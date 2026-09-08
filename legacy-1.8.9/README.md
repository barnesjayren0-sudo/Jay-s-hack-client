# Jay Client 1.8.9

Ghost / BedWars utility client for **Minecraft 1.8.9 + Forge + OptiFine**.

> Separate from the Fabric 1.21.11 client in the repo root.

## Requirements

- **Java 8** (JDK 1.8) — required for ForgeGradle 2.1
- Minecraft **1.8.9**
- **Forge 1.8.9** (11.15.1.2318 recommended)
- **OptiFine 1.8.9** (optional, compatible)

## Build (PC with Java 8)

```bash
cd legacy-1.8.9
# Use Java 8
export JAVA_HOME=/path/to/jdk8
./gradlew setupDecompWorkspace
./gradlew build
```

Jar output: `build/libs/jayclient-1.8.9-*.jar`

Put the jar in `.minecraft/mods` with Forge 1.8.9.

## Default binds

| Key | Action |
|-----|--------|
| RShift | ClickGUI |
| J | AimAssist |
| T | TriggerBot |
| N | Velocity |
| G | Scaffold |
| Delete | Panic (all off) |

## Commands

```text
.jay help
.jay gui
.jay panic
.jay friend add|del|list <name>
```

## Modules (v0.1.0)

Combat: AimAssist, TriggerBot, Velocity, AutoClicker, AutoSword, WTap  
Move: AutoSprint, SafeWalk, Scaffold  
Render: ESP, FullBright, ArrayList  
Util: MiddleClickFriend, Panic

Ghost defaults: soft aim, horizontal velocity only, non-sticky SafeWalk/Scaffold.
