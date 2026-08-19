package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.util.Humanizer;
import com.jay.hackclient.util.ItemUtil;
import net.minecraft.util.Hand;

public class AutoClicker extends Module {

    private long lastClick = 0;
    private int nextDelay = 120;

    public AutoClicker() {
        super("AutoClicker", "Humanized CPS while holding sword", Category.COMBAT);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        if (!ItemUtil.isSwordOrAxe(mc.player.getMainHandStack())) return;
        if (mc.currentScreen != null) return;
        // Only click when attack key held — more natural
        if (!mc.options.attackKey.isPressed()) return;
        if (Humanizer.shouldSkipTick(3)) return;

        long now = System.currentTimeMillis();
        if (now - lastClick < nextDelay) return;

        mc.player.swingHand(Hand.MAIN_HAND);
        lastClick = now;
        nextDelay = Humanizer.clickDelay();
    }
}
