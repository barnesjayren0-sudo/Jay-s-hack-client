package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.util.Humanizer;

/** Jump as you take a hit to reduce KB (jump-reset). */
public class JumpReset extends Module {

    private int lastHurt = 0;

    public JumpReset() {
        super("JumpReset", "Jump on hit to reduce knockback", Category.COMBAT);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        if (!mc.player.isOnGround()) return;

        int ht = mc.player.hurtTime;
        // Rising edge of hurt
        if (ht > 0 && lastHurt == 0) {
            if (Humanizer.chance(88)) {
                mc.player.jump();
            }
        }
        lastHurt = ht;
    }
}
