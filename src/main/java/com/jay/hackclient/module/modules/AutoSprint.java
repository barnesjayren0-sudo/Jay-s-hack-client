package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;

public class AutoSprint extends Module {

    public AutoSprint() {
        super("AutoSprint", "Keeps sprint for sword combos", Category.MOVEMENT);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        if (mc.player.forwardSpeed > 0
                && !mc.player.isSprinting()
                && !mc.player.isUsingItem()
                && !mc.player.isBlind()
                && mc.player.getHungerManager().getFoodLevel() > 6) {
            mc.player.setSprinting(true);
        }
    }
}
