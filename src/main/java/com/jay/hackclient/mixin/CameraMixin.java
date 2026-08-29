package com.jay.hackclient.mixin;

import com.jay.hackclient.module.modules.Freecam;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.Entity;
import net.minecraft.world.BlockView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Camera.class)
public abstract class CameraMixin {

    @Shadow
    protected abstract void setRotation(float yaw, float pitch);

    @Shadow
    protected abstract void setPos(double x, double y, double z);

    @Inject(method = "update", at = @At("TAIL"))
    private void jay$applyFreecam(BlockView area, Entity focusedEntity, boolean thirdPerson,
                                  boolean inverseView, float tickDelta, CallbackInfo ci) {
        if (!Freecam.active) return;
        setPos(Freecam.x, Freecam.y, Freecam.z);
        setRotation(Freecam.yaw, Freecam.pitch);
    }
}
