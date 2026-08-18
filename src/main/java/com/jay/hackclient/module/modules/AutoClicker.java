package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.util.ItemUtil;
import com.jay.hackclient.util.MathUtil;
import net.minecraft.util.Hand;

public class AutoClicker extends Module {

    private long lastClick = 0;
    private int nextDelay = 120;

    public AutoClicker() {
        super("AutoClicker", "Humanized sword click timing", Category.COMBAT);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        if (!ItemUtil.isSwordOrAxe(mc.player.getMainHandStack())) return;
        if (mc.currentScreen != null) return;

        long now = System.currentTimeMillis();
        if (now - lastClick < nextDelay) return;

        mc.player.swingHand(Hand.MAIN_HAND);
        lastClick = now;
        nextDelay = MathUtil.randomDelay(100, 145);
    }
}
