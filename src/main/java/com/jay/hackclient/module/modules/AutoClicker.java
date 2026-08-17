package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import net.minecraft.item.SwordItem;
import net.minecraft.util.Hand;

public class AutoClicker extends Module {

    private long lastClick = 0;
    private final int cps = 8; // clicks per second target

    public AutoClicker() {
        super("AutoClicker", "Timed sword clicks (~8 CPS)", Category.COMBAT);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.interactionManager == null) return;
        if (!(mc.player.getMainHandStack().getItem() instanceof SwordItem)) return;
        if (mc.currentScreen != null) return;

        long delay = 1000L / Math.max(1, cps);
        long now = System.currentTimeMillis();
        if (now - lastClick < delay) return;

        // Only swing — let vanilla / KillAura handle actual hits
        mc.player.swingHand(Hand.MAIN_HAND);
        lastClick = now;
    }
}
