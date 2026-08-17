# Jay's Hack Client v1.3.0

Fabric **1.21.11** · Sword PvP + Base Finders

---

## Panic / turn everything off

| Action | How |
|--------|-----|
| **Panic (freeze)** | Press **Delete** or `.jay panic` |
| **Unfreeze** | `.jay unpanic` |
| **Disable all** | `.jay off` |
| **Toggle one** | `.jay toggle KillAura` |

While frozen, no module ticks and HUD hides.

---

## Profiles

```
.jay profile legit   # Sprint + HUD only
.jay profile semi    # TriggerBot, Velocity, ESP…
.jay profile rage    # Full combat stack
.jay profile scout   # Base/spawner/radar finders
```

---

## Quieter behavior (not magic undetection)

- Randomized CPS / velocity factors
- Soft KB reduction (not 0%)
- Smooth KillAura rotations
- Friends ignored
- Combat **off by default** (only HUD on)

Strong server anticheat can still ban. Play smart.

---

## Build

```bash
./gradlew build
```

`build/libs/jays-hack-client-1.3.0.jar`

---

https://github.com/barnesjayren0-sudo/Jay-s-hack-client
