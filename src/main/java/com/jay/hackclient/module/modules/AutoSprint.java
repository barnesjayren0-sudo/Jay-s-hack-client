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

        boolean move = omni.get()
                ? mc.player.input.hasForwardMovement() || Math.abs(mc.player.sidewaysSpeed) > 0.01f
                : mc.player.input.hasForwardMovement();

        if (move && !mc.player.isSprinting()) {
            mc.player.setSprinting(true);
        }
    }
}
