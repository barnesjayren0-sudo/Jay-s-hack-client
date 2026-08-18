# Jay's Hack Client v1.8.1 — Phone build

Optimized for **PoJav / Mojo / Termux** (Redmi-class devices).

## Phone optimizations
- No sources jar (smaller download)
- Gradle uses less RAM
- Full-screen GUI on small displays
- BaseFinder uses smaller radius on phones
- HUD arraylist capped on small screens
- World scans on-demand only (`.jay scan`)
- Defaults: only HUD + AntiBot on

## Install
1. Fabric 1.21.11 + Fabric API
2. `jays-hack-client-1.8.1.jar` in `mods`
3. Optional: Sodium (FPS), Baritone (pathing)

## Build on Termux
```bash
cd ~/Jay-s-hack-client
git fetch origin && git reset --hard origin/main
export GRADLE_OPTS="-Xmx1536m"
./gradlew clean build --no-daemon
cp build/libs/jays-hack-client-*.jar /sdcard/Download/
```

Use **`.jay profile semi`** or **`kit`** for duels.
