package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.util.Humanizer;
import com.jay.hackclient.util.ItemUtil;
import net.minecraft.util.Hand;

public class AutoClicker extends Module {

    private long lastClick = 0;
    private int nextDelay = 125;

    public AutoClicker() {
        super("AutoClicker", "Humanized CPS", Category.COMBAT);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        if (!ItemUtil.isSwordOrAxe(mc.player.getMainHandStack())) return;
        if (mc.currentScreen != null) return;
        if (Humanizer.shouldSkipTick(4)) return;

        long now = System.currentTimeMillis();
        if (now - lastClick < nextDelay) return;

        mc.player.swingHand(Hand.MAIN_HAND);
        lastClick = now;
        nextDelay = Humanizer.clickDelay();
    }
}
