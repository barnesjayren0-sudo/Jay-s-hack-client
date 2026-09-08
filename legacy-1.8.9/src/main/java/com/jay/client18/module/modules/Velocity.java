package com.jay.client18.module.modules;

import com.jay.client18.module.Module;
import org.lwjgl.input.Keyboard;

/**
 * Horizontal velocity reduce applied when hurt.
 * Mixin/packet version can replace this later; tick soft reduce works as baseline.
 */
public class Velocity extends Module {

    /** Keep this fraction of horizontal KB (0.55 = soft). */
    public double horizontal = 0.55;
    private int lastHurtTime;

    public Velocity() {
        super("Velocity", "Reduce horizontal knockback", Category.COMBAT);
        setKeyBind(Keyboard.KEY_N);
    }

    @Override
    public void onTick() {
        if (mc.thePlayer == null) return;
        if (mc.thePlayer.hurtTime != lastHurtTime && mc.thePlayer.hurtTime == 9) {
            mc.thePlayer.motionX *= horizontal;
            mc.thePlayer.motionZ *= horizontal;
            // Y untouched
        }
        lastHurtTime = mc.thePlayer.hurtTime;
    }
}
