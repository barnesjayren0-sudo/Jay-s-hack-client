package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

/** Stores current target for HUD renderer. */
public class TargetHUD extends Module {

    public static PlayerEntity currentTarget = null;

    public TargetHUD() {
        super("TargetHUD", "Shows target health on screen", Category.RENDER);
    }

    @Override
    public void onTick() {
        currentTarget = null;
        if (mc.player == null || mc.world == null) return;

        if (mc.crosshairTarget != null && mc.crosshairTarget.getType() == HitResult.Type.ENTITY) {
            if (mc.crosshairTarget instanceof EntityHitResult ehr
                    && ehr.getEntity() instanceof PlayerEntity p
                    && p != mc.player) {
                currentTarget = p;
                return;
            }
        }

        // Fallback: nearest in 6 blocks
        double best = 6;
        for (PlayerEntity p : mc.world.getPlayers()) {
            if (p == mc.player || !p.isAlive()) continue;
            double d = mc.player.distanceTo(p);
            if (d < best) {
                best = d;
                currentTarget = p;
            }
        }
    }

    @Override
    public void onDisable() {
        currentTarget = null;
    }
}
