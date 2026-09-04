package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.BoolSetting;

/** Keep sprint while moving forward. */
public class AutoSprint extends Module {

    public final BoolSetting omni = new BoolSetting("Omni", "Sprint on strafe too", false);
    public final BoolSetting hungry = new BoolSetting("HungerCheck", "Stop under 6 hunger", true);

    public AutoSprint() {
        super("AutoSprint", "Always sprint when moving", Category.MOVEMENT);
        addSetting(omni);
        addSetting(hungry);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.options == null) return;
        if (mc.player.isSneaking() || mc.player.isUsingItem()) return;
        if (hungry.get() && mc.player.getHungerManager().getFoodLevel() <= 6) return;
        if (mc.player.horizontalCollision) return;

        boolean forward = mc.options.forwardKey.isPressed();
        boolean side = mc.options.leftKey.isPressed() || mc.options.rightKey.isPressed();
        boolean moving = omni.get() ? (forward || side || mc.options.backKey.isPressed()) : forward;

        if (moving && !mc.player.isSprinting()) {
            mc.player.setSprinting(true);
        }
    }
}
