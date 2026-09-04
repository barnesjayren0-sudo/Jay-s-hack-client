package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;

/** Reduce sprint cancel on hit — soft. */
public class KeepSprint extends Module {

    public KeepSprint() {
        super("KeepSprint", "Keep sprint after attacking", Category.MOVEMENT);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.options == null) return;
        if (!mc.options.forwardKey.isPressed()) return;
        if (mc.player.isSprinting()) return;
        if (mc.player.isSneaking() || mc.player.horizontalCollision) return;
        if (mc.player.getHungerManager().getFoodLevel() <= 6) return;
        // Re-apply sprint shortly after attack animation
        if (mc.player.handSwinging || mc.player.handSwingTicks > 0) {
            mc.player.setSprinting(true);
        }
    }
}
