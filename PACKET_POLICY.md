# JAY CLIENT — Real Packet Policy (Orchard-style)

Inspired by **Orchard** (free Ghost / utility client for 1.21.11).

## Rule: No custom packets

Only vanilla C2S packets:

| Action | Packet |
|--------|--------|
| Move | `PlayerMoveC2SPacket` |
| Sprint/Sneak | `ClientCommandC2SPacket` |
| Attack | `PlayerInteractEntityC2SPacket` |
| Dig | `PlayerActionC2SPacket` |

Use `com.jay.hackclient.util.RealPackets`.

## Speed modes
Vanilla, BHop, LowHop, YPort, Strafe, OnGround, Timer, Custom

## GUI colors
`GuiColors.setAccentHex("#9B6BFF")` or `GuiColors.applyPreset("purple")`
Presets: purple, blue, red, green, orange, pink, gold, white
