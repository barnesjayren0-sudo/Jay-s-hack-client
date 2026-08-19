package com.jay.hackclient.mixin;

import com.jay.hackclient.JayHackClient;
import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.modules.Velocity;
import net.minecraft.network.packet.s2c.play.EntityVelocityUpdateS2CPacket;
import net.minecraft.util.math.Vec3d;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(EntityVelocityUpdateS2CPacket.class)
public class EntityVelocityUpdateS2CPacketMixin {

    @Inject(
        method = "getVelocity",
        at = @At("RETURN"),
        cancellable = true
    )
    private void jay$modifyVelocity(
        CallbackInfoReturnable<Vec3d> cir
    ) {
        if (JayHackClient.moduleManager == null) {
            return;
        }

        Module mod =
            JayHackClient.moduleManager.getModuleByName("Velocity");

        if (mod == null || !mod.isEnabled()) {
            return;
        }

        EntityVelocityUpdateS2CPacket packet =
            (EntityVelocityUpdateS2CPacket) (Object) this;

        var mc = net.minecraft.client.MinecraftClient.getInstance();

        if (mc.player == null) {
            return;
        }

        if (packet.getEntityId() != mc.player.getId()) {
            return;
        }

        Vec3d original = cir.getReturnValue();
        if (original == null) {
            return;
        }

        double horizontal = Velocity.horizontalFactor();

        cir.setReturnValue(
            new Vec3d(
                original.x * horizontal,
                original.y,              // Y untouched
                original.z * horizontal
            )
        );

        Velocity.lastPacketMs = System.currentTimeMillis();
    }
}
