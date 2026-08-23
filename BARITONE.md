# JayBaritone (v1.18.0)

Jay's Hack Client does **not** re-ship Baritone source (LGPL).  
It drives **official Baritone** at runtime when the jar is in `mods/`.

## Install (1.21.11)

1. Fabric + Fabric API 1.21.11  
2. `jays-hack-client-1.18.0.jar`  
3. **`baritone-fabric-1.21.11.jar`** (the file you have) in the **same** `mods/` folder  

Restart Minecraft.

## Commands

```text
#goto 100 64 200
#goto 100 200
#mine iron_ore
#stop
#pause
#resume
#thisway 80
#status
#help

.b goto 100 64 200
.b mine diamond_ore
.b stop

.jay baritone
.jay path          (PathToBase + BaseFinder)
```

GUI: **WORLD → Baritone** module (enable once to confirm link).

## API used

Reflection on:
- `baritone.api.BaritoneAPI`
- CustomGoalProcess / PathingBehavior / MineProcess
- GoalBlock / GoalXZ

## Note

A full **source fork** of Baritone would mean cloning https://github.com/cabaletta/baritone and maintaining LGPL changes separately. This client uses the stock jar + Jay control layer instead.
