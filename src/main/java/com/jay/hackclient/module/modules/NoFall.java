package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.NumberSetting;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

/** Packet no-fall — onGround claim only when fallDistance is high enough. */
public class NoFall extends Module {

    public final NumberSetting minFall = new NumberSetting("MinFall", "Fall distance to trigger", 2.8, 2.0, 5.0, 0.1);

    private long lastPacket;

    public NoFall() {
        super("NoFall", "Packet no-fall when falling hard", Category.MOVEMENT);
        addSetting(minFall);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.getNetworkHandler() == null) return;
        if (mc.player.isTouchingWater() || mc.player.isInLava()) return;
        if (mc.player.getAbilities().flying || mc.player.getAbilities().allowFlying) return;
        if (mc.player.isGliding() || mc.player.hasVehicle()) return;

        if (mc.player.fallDistance < minFall.getFloat()) return;

        long now = System.currentTimeMillis();
        if (now - lastPacket < 80) return; // rate limit

        try {
            // OnGroundOnly — claims grounded so server resets fall distance
            mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(true, false));
            mc.player.fallDistance = Math.min(mc.player.fallDistance, 0.5f);
            lastPacket = now;
        } catch (Throwable ignored) {}
    }
}
