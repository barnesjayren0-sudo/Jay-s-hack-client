package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;

public class NoFall extends Module {

    public NoFall() {
        super("NoFall", "Reduces fall damage risk (soft)", Category.MOVEMENT);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        // Soft: reset fall distance early when sneaking near ground — full nofall needs packets/mixins
        if (mc.player.fallDistance > 2.5f && mc.player.isOnGround()) {
            mc.player.fallDistance = 0;
        }
    }
}
