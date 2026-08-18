package com.jay.hackclient.mixin;

import com.jay.hackclient.JayHackClient;
import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.modules.Hitboxes;
import net.minecraft.client.MinecraftClient;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public class EntityMixin {

    @Inject(method = "getBoundingBox", at = @At("RETURN"), cancellable = true)
    private void jay$expandHitbox(CallbackInfoReturnable<Box> cir) {
        if (JayHackClient.moduleManager == null) return;

        Module mod = JayHackClient.moduleManager.getModuleByName("Hitboxes");
        if (mod == null || !mod.isEnabled()) return;

        Entity self = (Entity) (Object) this;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null) return;
        if (!(self instanceof PlayerEntity)) return;
        if (self == mc.player) return;

        double expand = Hitboxes.expand;
        if (expand <= 0) return;

        Box box = cir.getReturnValue();
        cir.setReturnValue(box.expand(expand, expand * 0.15, expand));
    }
}
