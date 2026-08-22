package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.util.Humanizer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

/** Sprint-reset style W-tap after hits for more KB. */
public class WTap extends Module {

    private long until = 0;
    private boolean reset;

    public WTap() {
        super("WTap", "Sprint reset after hits", Category.COMBAT);
    }

    @Override
    public void onDisable() {
        reset = false;
        until = 0;
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;

        long now = System.currentTimeMillis();
        if (reset && now < until) {
            mc.player.setSprinting(false);
            return;
        }
        if (reset && now >= until) {
            reset = false;
            if (mc.player.forwardSpeed > 0 || (mc.options != null && mc.options.forwardKey.isPressed())) {
                mc.player.setSprinting(true);
            }
        }

        if (mc.crosshairTarget != null && mc.crosshairTarget.getType() == HitResult.Type.ENTITY) {
            Entity e = ((EntityHitResult) mc.crosshairTarget).getEntity();
            if (e instanceof PlayerEntity && mc.player.handSwingTicks == 1) {
                if (Humanizer.chance(75)) {
                    reset = true;
                    until = now + Humanizer.tapResetMs();
                    mc.player.setSprinting(false);
                }
            }
        }
    }
}
