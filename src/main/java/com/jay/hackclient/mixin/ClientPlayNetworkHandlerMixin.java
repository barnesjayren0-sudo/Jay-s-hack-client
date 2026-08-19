package com.jay.hackclient.mixin;

import com.jay.hackclient.JayHackClient;
import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.modules.Velocity;
import com.jay.hackclient.settings.ClientSettings;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Do NOT cancel velocity packets — that freezes movement.
 * After vanilla applies, if we were just hurt, soft-scale horizontal KB only.
 */
@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {

    @Inject(method = "onEntityVelocityUpdate", at = @At("TAIL"))
    private void jay$softVelocity(EntityVelocityUpdateS2CPacket packet, CallbackInfo ci) {
        if (JayHackClient.moduleManager == null) return;
        Module mod = JayHackClient.moduleManager.getModuleByName("Velocity");
        if (mod == null || !mod.isEnabled()) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        if (packet.getEntityId() != mc.player.getId()) return;

        // Only touch knockback-like updates: player must be in hurt state
        if (mc.player.hurtTime <= 0) return;

        double hx = Velocity.horizontalFactor();
        double hy = Velocity.verticalFactor();

        // Never go too low — looks like 0-vel to AC
        if (hx < 0.35) hx = 0.35;
        if (hy < 0.80) hy = 0.80;

        Vec3d v = mc.player.getVelocity();
        mc.player.setVelocity(v.x * hx, v.y * hy, v.z * hx);
        Velocity.packetHandledThisTick = true;
        Velocity.lastPacketMs = System.currentTimeMillis();
    }
}
