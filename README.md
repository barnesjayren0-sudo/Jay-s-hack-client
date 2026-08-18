# Jay's Hack Client v1.8.0

Fabric **1.21.11** · EventBus · Optional Baritone · Kit / UHC / Nethpot

---

## Architecture (Meteor-inspired, no hard Meteor dep)

| Meteor piece | What we do |
|--------------|------------|
| **Fabric API** | Required (same as Meteor) |
| **Orbit** | Built-in `EventBus` (Orbit-style subscribe/post) |
| **Baritone** | Optional — install separately, we detect at runtime |
| **Sodium** | Optional — suggested for FPS |
| **Starscript** | Not bundled (keeps Termux builds simple) |
| **Full Meteor Client** | Not a dependency — would conflict as a second client |

Hard-depending on Meteor Client / ukulib breaks phone builds and double-loads clients. We mirror the useful patterns instead.

### Optional installs
1. Fabric API (required)
2. Jay jar (this mod)
3. **Baritone** Fabric jar → enables `.jay path` / PathToBase
4. Sodium (FPS)

---

## Commands

```
.jay profile kit|semi|legit|nethpot|uhc|crystal
.jay path          # Baritone to last base scan
.jay baritone      # check + path
.jay panic
```

## Build

```bash
cd ~/Jay-s-hack-client
git fetch origin && git reset --hard origin/main
export GRADLE_OPTS="-Xmx1536m"
./gradlew clean build --no-daemon
cp build/libs/jays-hack-client-*.jar /sdcard/Download/
```

https://github.com/barnesjayren0-sudo/Jay-s-hack-client
