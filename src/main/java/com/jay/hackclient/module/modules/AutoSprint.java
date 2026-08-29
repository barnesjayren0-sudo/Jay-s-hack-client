package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.BoolSetting;

/** Keep sprint when moving forward. */
public class AutoSprint extends Module {

    public final BoolSetting omni = new BoolSetting("Omni", "Sprint in all directions", false);
    public final BoolSetting hunger = new BoolSetting("HungerCheck", "Stop under 6 hunger", true);

    public AutoSprint() {
        super("AutoSprint", "Always sprint when moving", Category.MOVEMENT);
        addSetting(omni);
        addSetting(hunger);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.options == null) return;
        if (mc.player.getAbilities().flying) return;
        if (mc.player.isSneaking() || mc.player.isUsingItem()) return;
        if (hunger.get() && mc.player.getHungerManager().getFoodLevel() <= 6) return;

        boolean forward = mc.options.forwardKey.isPressed();
        boolean side = mc.options.leftKey.isPressed() || mc.options.rightKey.isPressed();
        boolean move = omni.get() ? (forward || side) : forward;

        if (move && !mc.player.isSprinting()) {
            mc.player.setSprinting(true);
        }
    }
}
