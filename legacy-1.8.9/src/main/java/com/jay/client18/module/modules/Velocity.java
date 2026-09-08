package com.jay.client18.module.modules;

import com.jay.client18.module.Module;
import com.jay.client18.util.Humanizer;
import org.lwjgl.input.Keyboard;

/**
 * Soft horizontal KB only — never 0%, never touch Y.
 * Slight random so it doesn't look like a flat multiplier every hit.
 */
public class Velocity extends Module {

    /** Base keep fraction (0.62 ≈ soft ghost). */
    public double horizontal = 0.62;
    private int lastHurtTime;

    public Velocity() {
        super("Velocity", "Soft horizontal velocity", Category.COMBAT);
        setKeyBind(Keyboard.KEY_N);
    }

    @Override
    public void onTick() {
        if (mc.thePlayer == null) return;
        // Apply once at start of hurt cycle
        if (mc.thePlayer.hurtTime == 9 && lastHurtTime != 9) {
            double keep = horizontal + (Math.random() * 0.08 - 0.04);
            if (keep < 0.50) keep = 0.50;
            if (keep > 0.85) keep = 0.85;
            // Occasional near-vanilla tick so pattern isn't constant
            if (Humanizer.chance(12)) keep = Math.min(0.92, keep + 0.15);
            mc.thePlayer.motionX *= keep;
            mc.thePlayer.motionZ *= keep;
        }
        lastHurtTime = mc.thePlayer.hurtTime;
    }
}
