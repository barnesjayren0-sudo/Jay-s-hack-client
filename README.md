# Jay's Hack Client v1.7.0

Fabric **1.21.11** · SMP kits · UHC · Nethpot · Sword PvP

Nice win — keep using **legit/semi/kit** profiles, not rage, if the server has AC.

---

## New SMP kit modules

| Module | What it does |
|--------|----------------|
| **AutoTotem** | Restocks offhand totem |
| **OffhandGap** | Gap in offhand when HP is safe |
| **ShieldBreak** | Axe-swaps when enemy is blocking |
| **Refill** | Pulls pots/pearls/gaps/crystals into empty hotbar |
| **AnchorMacro** | Faster respawn-anchor charge clicks |

## Profiles

```
.jay profile kit       # SMP kit PvP
.jay profile crystal   # totem + anchor + refill
.jay profile semi
.jay profile legit
.jay profile nethpot
.jay profile uhc
```

Or: `.jay kit` / `.jay crystal`

---

## Build

```bash
cd ~/Jay-s-hack-client
git fetch origin && git reset --hard origin/main
export GRADLE_OPTS="-Xmx1536m"
./gradlew clean build --no-daemon
cp build/libs/jays-hack-client-*.jar /sdcard/Download/
```

https://github.com/barnesjayren0-sudo/Jay-s-hack-client
