package com.jay.client18.module.modules;

import com.jay.client18.module.Module;

public class AutoSprint extends Module {

    public AutoSprint() {
        super("AutoSprint", "Sprint when moving forward", Category.MOVEMENT);
    }

    @Override
    public void onTick() {
        if (mc.thePlayer == null) return;
        if (mc.thePlayer.isSneaking() || mc.thePlayer.isUsingItem()) return;
        if (mc.thePlayer.isCollidedHorizontally) return;
        if (mc.thePlayer.getFoodStats().getFoodLevel() <= 6) return;
        if (mc.thePlayer.moveForward > 0.0f && !mc.thePlayer.isSprinting()) {
            mc.thePlayer.setSprinting(true);
        }
    }
}
