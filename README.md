# Jay's Hack Client v1.14.0

Fabric **1.21.11** · **Java + Kotlin** hybrid · instant velocity · sword PvP

## Dependencies
- Fabric Loader + Fabric API
- **fabric-language-kotlin** (pulled by Gradle; also needed in `mods` if not nested)

Loom nests FLK into the jar in most setups; if the game says missing `fabric-language-kotlin`, download it from [Fabric Meta](https://meta.fabricmc.net/) into `mods`.

## Build (Termux)
```bash
cd ~/Jay-s-hack-client
git fetch origin && git reset --hard origin/main
export GRADLE_OPTS="-Xmx1536m"
./gradlew clean build --no-daemon
cp build/libs/jays-hack-client-*.jar /sdcard/Download/
```

First Kotlin build downloads the compiler — may take longer on phone.

## Commands
```
.jay sword
.jay swordaggro
.jay velmode medium
.jay settings
```
