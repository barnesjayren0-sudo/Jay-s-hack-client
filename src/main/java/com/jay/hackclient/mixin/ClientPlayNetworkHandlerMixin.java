package com.jay.hackclient.mixin;

import com.jay.hackclient.JayHackClient;
import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.modules.Velocity;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Cancel vanilla KB application, then set reduced velocity immediately.
 * Fixes the "full knockback flash then reduce" delay.
 */
@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {

    @Inject(method = "onEntityVelocityUpdate", at = @At("HEAD"), cancellable = true)
    private void jay$instantVelocity(EntityVelocityUpdateS2CPacket packet, CallbackInfo ci) {
        if (JayHackClient.moduleManager == null) return;
        Module mod = JayHackClient.moduleManager.getModuleByName("Velocity");
        if (mod == null || !mod.isEnabled()) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;
        if (packet.getEntityId() != mc.player.getId()) return;

        // Prevent vanilla from applying full knockback this tick
        ci.cancel();

        double hx = Velocity.horizontalFactor();
        double hy = Velocity.verticalFactor();

        // Packet stores velocity in 1/8000 units on modern versions
        double vx = packet.getVelocityX();
        double vy = packet.getVelocityY();
        double vz = packet.getVelocityZ();

        // If values look like raw shorts (large ints), scale down
        if (Math.abs(vx) > 20 || Math.abs(vy) > 20 || Math.abs(vz) > 20) {
            vx /= 8000.0;
            vy /= 8000.0;
            vz /= 8000.0;
        }

        mc.player.setVelocity(vx * hx, vy * hy, vz * hx);
        Velocity.packetHandledThisTick = true;
        Velocity.lastPacketMs = System.currentTimeMillis();
    }
}
