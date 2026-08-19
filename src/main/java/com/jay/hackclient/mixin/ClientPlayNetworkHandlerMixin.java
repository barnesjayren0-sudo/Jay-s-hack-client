package com.jay.hackclient.mixin;

import com.jay.hackclient.JayHackClient;
import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.modules.Velocity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {

    @Inject(
        method = "onEntityVelocityUpdate",
        at = @At("TAIL")
    )
    private void jay$applyVelocity(
        EntityVelocityUpdateS2CPacket packet,
        CallbackInfo ci
    ) {
        if (JayHackClient.moduleManager == null) {
            return;
        }

        Module mod = JayHackClient.moduleManager.getModuleByName("Velocity");

        if (mod == null || !mod.isEnabled()) {
            return;
        }

        MinecraftClient mc = MinecraftClient.getInstance();

        if (mc.player == null) {
            return;
        }

        // Only modify velocity packets belonging to the local player
        if (packet.getEntityId() != mc.player.getId()) {
            return;
        }

        // Prefer only during hurt — avoids scaling random movement velocity packets
        if (mc.player.hurtTime <= 0) {
            return;
        }

        double horizontal = Velocity.horizontalFactor();
        // Y never reduced (verticalFactor is 1.0)
        double vertical = Velocity.verticalFactor();

        Vec3d velocity = mc.player.getVelocity();

        mc.player.setVelocity(
            velocity.x * horizontal,
            velocity.y * vertical,
            velocity.z * horizontal
        );

        Velocity.lastPacketMs = System.currentTimeMillis();
    }
}
