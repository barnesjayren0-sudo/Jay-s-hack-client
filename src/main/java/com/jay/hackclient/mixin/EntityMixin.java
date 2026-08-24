package com.jay.hackclient.mixin;

import com.jay.hackclient.module.modules.Hitboxes;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Expands OTHER players' bounding boxes when Hitboxes is enabled.
 * Does not expand self, non-players, or spectators.
 */
@Mixin(Entity.class)
public class EntityMixin {

    @Inject(method = "getBoundingBox", at = @At("RETURN"), cancellable = true)
    private void jay$expandHitbox(CallbackInfoReturnable<Box> cir) {
        if (!Hitboxes.isActive()) return;

        Entity self = (Entity) (Object) this;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        if (!(self instanceof PlayerEntity player)) return;
        if (player == mc.player) return;
        if (!player.isAlive() || player.isSpectator()) return;

        double expand = Hitboxes.getExpand();
        if (expand <= 0.001) return;

        // Horizontal expand more, vertical less (looks more natural)
        Box box = cir.getReturnValue();
        cir.setReturnValue(box.expand(expand, expand * 0.12, expand));
    }
}
