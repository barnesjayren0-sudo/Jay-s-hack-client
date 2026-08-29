package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

/** Packet-assisted no-fall — claims onGround before hard landing. */
public class NoFall extends Module {

    public NoFall() {
        super("NoFall", "Packet no-fall when falling hard", Category.MOVEMENT);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.getNetworkHandler() == null) return;
        if (mc.player.isTouchingWater() || mc.player.isInLava()) return;
        if (mc.player.getAbilities().flying) return;

        if (mc.player.fallDistance > 2.5f) {
            try {
                // 1.21.x OnGroundOnly(onGround, horizontalCollision)
                mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(true, false));
            } catch (Throwable t) {
                try {
                    mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(true));
                } catch (Throwable ignored) {}
            }
            mc.player.fallDistance = 0;
        }
    }
}
