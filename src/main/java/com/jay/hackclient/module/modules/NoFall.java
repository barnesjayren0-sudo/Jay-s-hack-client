package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.NumberSetting;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

/** Soft packet no-fall — only when fallDistance is high. */
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
        if (mc.player.isOnGround()) return;
        if (mc.player.hasVehicle()) return;
        if (mc.player.fallDistance < minFall.getFloat()) return;

        long now = System.currentTimeMillis();
        if (now - lastPacket < 250) return;

        try {
            // Claim on-ground for this tick only — soft / not every packet
            mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(true, false));
            mc.player.fallDistance = 0;
            lastPacket = now;
        } catch (Throwable ignored) {
            // Fallback: zero fall distance client-side only
            mc.player.fallDistance = Math.min(mc.player.fallDistance, minFall.getFloat() * 0.5f);
        }
    }
}
