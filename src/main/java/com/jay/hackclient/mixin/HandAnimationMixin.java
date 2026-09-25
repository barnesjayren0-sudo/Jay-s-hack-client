package com.jay.hackclient.mixin;

import com.jay.hackclient.module.modules.HandAnimation;
import net.minecraft.client.render.item.HeldItemRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Arm;
import net.minecraft.util.math.MathHelper;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Applies HandAnimation swing/equip multipliers.
 * Real client-side render only — no packets.
 */
@Mixin(HeldItemRenderer.class)
public class HandAnimationMixin {

    @ModifyVariable(
            method = "renderFirstPersonItem",
            at = @At("HEAD"),
            argsOnly = true,
            ordinal = 3,
            require = 0
    )
    private float jay$modifySwingProgress(float swingProgress) {
        float mul = HandAnimation.getSwingMultiplier();
        float scale = HandAnimation.getProgressScale();
        if (mul == 1.0f && scale == 1.0f) return swingProgress;
        float slowed = swingProgress * mul;
        return MathHelper.clamp(slowed * scale, 0f, 1f);
    }

    @Inject(
            method = "applyEquipOffset",
            at = @At("HEAD"),
            cancellable = true,
            require = 0
    )
    private void jay$equipOffset(MatrixStack matrices, Arm arm, float equipProgress, CallbackInfo ci) {
        float mul = HandAnimation.getEquipMultiplier();
        if (mul == 1.0f) return;
        float ep = equipProgress * mul;
        int i = arm == Arm.RIGHT ? 1 : -1;
        matrices.translate(i * 0.56F, -0.52F + ep * -0.6F, -0.72F);
        ci.cancel();
    }

    @Inject(
            method = "applySwingOffset",
            at = @At("RETURN"),
            require = 0
    )
    private void jay$swingHeight(MatrixStack matrices, Arm arm, float swingProgress, CallbackInfo ci) {
        float h = HandAnimation.getSwingHeightOffset();
        if (h != 0f) {
            matrices.translate(0, h * 0.15f, 0);
        }
    }
}
