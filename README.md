# Jay's Hack Client v1.6.0

Fabric **1.21.11** · Sword / Nethpot / UHC

---

## Detection note

Nothing is fully undetectable. After getting flagged:

- Prefer **`.jay profile legit`** or **semi** — not rage
- Avoid KillAura; use **AimAssist + TriggerBot**
- Keep **Hitboxes** expand low (default ~0.15–0.18)
- Press **Delete** panic between fights if needed
- Don't enable Speed + KillAura + big Hitboxes together

---

## New modules

| Module | Use |
|--------|-----|
| **Hitboxes** | Expand enemy boxes (mixin) |
| **AutoPot** | Splash heal when low (nethpot) |
| **AutoGap** | Eat gaps when low (UHC) |
| **AutoHead** | Golden heads (UHC) |
| **PearlCatch** | Look at nearby pearls |

## Profiles

```
.jay profile legit
.jay profile semi
.jay profile nethpot
.jay profile uhc
.jay profile rage
```

---

## Build (Termux)

```bash
cd ~/Jay-s-hack-client
git fetch origin && git reset --hard origin/main
export GRADLE_OPTS="-Xmx1536m"
./gradlew clean build --no-daemon
cp build/libs/jays-hack-client-*.jar /sdcard/Download/
```

https://github.com/barnesjayren0-sudo/Jay-s-hack-client
