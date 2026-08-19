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

    /**
     * After vanilla applies knockback, immediately scale it.
     * Stronger than the old single soft multiply.
     */
    @Inject(method = "onEntityVelocityUpdate", at = @At("TAIL"))
    private void jay$scaleVelocity(EntityVelocityUpdateS2CPacket packet, CallbackInfo ci) {
        if (JayHackClient.moduleManager == null) return;
        Module mod = JayHackClient.moduleManager.getModuleByName("Velocity");
        if (mod == null || !mod.isEnabled()) return;

        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        if (packet.getEntityId() != mc.player.getId()) return;

        double hx = Velocity.horizontalFactor();
        double hy = Velocity.verticalFactor();

        Vec3d v = mc.player.getVelocity();
        mc.player.setVelocity(v.x * hx, v.y * hy, v.z * hx);
        Velocity.packetHandledThisTick = true;
    }
}
