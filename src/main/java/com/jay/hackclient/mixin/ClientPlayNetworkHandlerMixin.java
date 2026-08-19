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

/**
 * One-shot horizontal scale when hurt packet applies.
 * No cancel, no Y change, no every-tick follow-up.
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
        if (mc.player.hurtTime <= 0) return;

        double hx = Velocity.horizontalFactor();

        Vec3d v = mc.player.getVelocity();
        // Only X/Z once — never touch Y
        mc.player.setVelocity(v.x * hx, v.y, v.z * hx);

        Velocity.lastPacketMs = System.currentTimeMillis();
    }
}
