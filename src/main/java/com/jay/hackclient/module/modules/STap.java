package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.util.Humanizer;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

/**
 * S-Tap: brief backward / stop sprint after a hit for stronger KB reset.
 * Complements WTap — don't enable both aggressively at once.
 */
public class STap extends Module {

    private long until = 0;
    private boolean active;
    private boolean wasForward;

    public STap() {
        super("STap", "S-tap sprint reset after hits", Category.COMBAT);
    }

    @Override
    public void onDisable() {
        active = false;
        until = 0;
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.options == null) return;

        long now = System.currentTimeMillis();

        if (active && now < until) {
            // Release forward for a short window (S-tap feel without forcing S key packets)
            mc.player.setSprinting(false);
            if (mc.player.forwardSpeed > 0.01f) {
                // soft stop forward momentum contribution
                mc.player.forwardSpeed = 0;
            }
            return;
        }

        if (active && now >= until) {
            active = false;
            if (wasForward && mc.player.forwardSpeed >= 0) {
                mc.player.setSprinting(true);
            }
        }

        if (mc.crosshairTarget == null || mc.crosshairTarget.getType() != HitResult.Type.ENTITY) return;
        Entity e = ((EntityHitResult) mc.crosshairTarget).getEntity();
        if (!(e instanceof PlayerEntity)) return;

        // handSwingTicks == 1 ≈ just swung
        if (mc.player.handSwingTicks != 1) return;
        if (!Humanizer.chance(70)) return;

        wasForward = mc.player.forwardSpeed > 0 || mc.options.forwardKey.isPressed();
        active = true;
        until = now + Humanizer.tapResetMs();
        mc.player.setSprinting(false);
    }
}
