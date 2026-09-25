# JAY CLIENT — Premium Visual Upgrade v2

## What was upgraded

### 1. TargetHUD (Ghost-tier)
- Smooth **scale + alpha** open/close using EaseOutBack (same family as Prestige / Elusive)
- **Health bar lerp** so the bar doesn’t jump
- Player head placeholder + name + HP% + distance + armor
- Soft purple **glow** matching your reference screenshot
- Modes: Modern / Compact / Legacy
- Fully draggable via X/Y settings (0–1 screen %)

### 2. Premium Theme
- Accent purple `#9B6BFF` (matches the crystal / “Ryaven” vibe in your photo)
- Near-black panels with blue undertone
- Accent left bar on panels
- Shared GUI open animation (EaseOutBack 280 ms)

### 3. Animation system
- `Animation.java` with LINEAR, EASE_OUT_CUBIC, EASE_IN_OUT_CUBIC, EASE_OUT_BACK, EASE_OUT_EXPO, EASE_OUT_QUAD
- Direction FORWARDS / BACKWARDS so open and close feel premium

### 4. Real packets only
- **No custom packet classes**
- All combat / movement still use vanilla Fabric / Minecraft packets:
  - `PlayerInteractEntityC2SPacket`
  - `PlayerActionC2SPacket`
  - `ClientCommandC2SPacket`
  - `PlayerMoveC2SPacket`
  - etc.
- TargetHUD only reads real `LivingEntity` data (health, armor, distance). Zero packet injection for the HUD.

## How to install

1. These files are already in the correct package paths under `src/main/java/com/jay/hackclient/`
2. In `HudRenderer` call TargetHUD.render when enabled
3. In `ClickGuiScreen`: on open → `PremiumTheme.onGuiOpen()`, on close → `PremiumTheme.onGuiClose()`
4. Rebuild the Fabric mod

## Reference clients
Prestige, Grave, Elusive, Vape, Breeze + user purple screenshot
