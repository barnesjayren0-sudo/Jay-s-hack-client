package com.jay.hackclient.mixin;

import com.jay.hackclient.module.modules.Reach;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.player.PlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Applies custom entity interaction range when Reach is enabled.
 * Only affects the local client player.
 */
@Mixin(PlayerEntity.class)
public class PlayerEntityMixin {

    @Inject(method = "getEntityInteractionRange", at = @At("RETURN"), cancellable = true)
    private void jay$entityRange(CallbackInfoReturnable<Double> cir) {
        if (!Reach.isActive()) return;

        PlayerEntity self = (PlayerEntity) (Object) this;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || self != mc.player) return;

        // Don't boost creative beyond what they already have
        if (self.isCreative()) return;

        double custom = Reach.getReach();
        double vanilla = cir.getReturnValue() != null ? cir.getReturnValue() : 3.0;
        // Never reduce below vanilla
        if (custom > vanilla) {
            cir.setReturnValue(custom);
        }
    }
}
