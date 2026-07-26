package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import net.minecraft.client.MinecraftClient;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;

public class Criticals extends Module {

    private final MinecraftClient mc = MinecraftClient.getInstance();

    public Criticals() {
        super("Criticals", "Forces critical hits with packets", Category.COMBAT);
    }

    // This will be hooked into attack events via mixin later.
    // For now it provides the packet method.

    public void doCrit() {
        if (mc.player == null || mc.getNetworkHandler() == null) return;
        if (!mc.player.isOnGround()) return;

        // Packet criticals
        double x = mc.player.getX();
        double y = mc.player.getY();
        double z = mc.player.getZ();

        mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y + 0.0625, z, false));
        mc.getNetworkHandler().sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y, z, false));
    }
}
