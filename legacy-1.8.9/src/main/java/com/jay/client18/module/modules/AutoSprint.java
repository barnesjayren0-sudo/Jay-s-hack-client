package com.jay.client18.module.modules;

import com.jay.client18.module.Module;

public class AutoSprint extends Module {

    public AutoSprint() {
        super("AutoSprint", "Always sprint", Category.MOVEMENT);
    }

    @Override
    public void onTick() {
        if (mc.thePlayer == null) return;
        if (mc.thePlayer.isSneaking() || mc.thePlayer.isUsingItem()) return;
        if (mc.thePlayer.moveForward > 0 && !mc.thePlayer.isCollidedHorizontally) {
            mc.thePlayer.setSprinting(true);
        }
    }
}
