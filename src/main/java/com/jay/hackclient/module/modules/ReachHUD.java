package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

/** Tracks last hit / crosshair distance. */
public class ReachHUD extends Module {

    public static double lastReach = 0;
    public static double crosshairDist = 0;

    public ReachHUD() {
        super("ReachHUD", "Show reach / crosshair distance", Category.RENDER);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        if (mc.crosshairTarget != null && mc.crosshairTarget.getType() == HitResult.Type.ENTITY
                && mc.crosshairTarget instanceof EntityHitResult ehr) {
            crosshairDist = mc.player.getEyePos().distanceTo(ehr.getPos());
        }
    }

    public static void recordHit(double dist) {
        lastReach = dist;
    }

    public static double lastHitDist() {
        return lastReach;
    }
}
