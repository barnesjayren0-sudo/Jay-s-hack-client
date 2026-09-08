# Jay Client 1.8.9

Ghost / BedWars utility client for **Minecraft 1.8.9 + Forge + OptiFine**.

Version **0.1.1** — silent combat modules, BedESP, screenshare-safer HUD.

## Important: Termux vs PC

**Forge 1.8.9 (ForgeGradle 2.1) almost never builds cleanly on Termux.**  
It needs **Java 8**, old Gradle, and `setupDecompWorkspace` (heavy, flaky on phones).

| Platform | 1.8.9 Forge client | Fabric 1.21.11 client (repo root) |
|----------|--------------------|-------------------------------------|
| Termux | Very hard / often fails | Works (you already build this) |
| PC with JDK 8 | Recommended | Works with JDK 21 |

### Termux — try 1.8.9 (advanced)

```bash
# Java 8 is required. Termux usually has newer JDK only.
pkg update
pkg install openjdk-17 git wget unzip  # 17 may still fail FG 2.1

cd ~
git clone https://github.com/barnesjayren0-sudo/Jay-s-hack-client.git
cd Jay-s-hack-client/legacy-1.8.9

# ForgeGradle 2.1 expects Gradle wrapper from that era — if missing:
# install gradle 4.x manually or generate wrapper on a PC and copy it

chmod +x gradlew 2>/dev/null
./gradlew setupDecompWorkspace --stacktrace
./gradlew build --stacktrace
```

If you see `Unsupported class file major version` → you need **JDK 8**, not 17/21.

**Practical path on phone:** build the **1.21.11** client in the repo root (Fabric), or build `legacy-1.8.9` on a PC with JDK 8 and copy the jar to the phone.

### PC build (recommended for 1.8.9)

```bash
cd legacy-1.8.9
# JAVA_HOME must point to JDK 8
./gradlew setupDecompWorkspace
./gradlew build
```

Jar: `build/libs/jayclient-1.8.9-*.jar` → `.minecraft/mods` with Forge 1.8.9.

## Modules

Combat: AimAssist, TriggerBot, Velocity, AutoClicker, AutoSword, WTap  
Move: AutoSprint, SafeWalk, Scaffold  
Render: ESP, BedESP, FullBright, ArrayList  
Misc: MiddleClickFriend, Soursome  

Binds: RShift GUI · J Aim · T Trigger · N Velocity · G Scaffold · Del Panic

## Screenshare tips

- **Delete** = Panic (all off)
- HUD hides when **F3** debug is open or **F1** hideGUI
- Prefer soft modules only on screened servers
