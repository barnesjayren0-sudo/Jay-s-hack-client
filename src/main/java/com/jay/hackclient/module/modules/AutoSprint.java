package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.BoolSetting;

/** Keep sprint while moving forward. */
public class AutoSprint extends Module {

    public final BoolSetting omni = new BoolSetting("Omni", "Sprint in all directions", false);
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

        boolean moving = omni.get()
                ? (mc.player.input.movementForward != 0 || mc.player.input.movementSideways != 0)
                : mc.player.input.movementForward > 0;

        if (moving && mc.player.isOnGround() || moving && !mc.player.isOnGround()) {
            if (!mc.player.isSprinting()) mc.player.setSprinting(true);
        }
    }
}
