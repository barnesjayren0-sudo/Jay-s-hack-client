package com.jay.client18.module.modules;

import com.jay.client18.module.Module;
import com.jay.client18.util.CombatUtil;
import com.jay.client18.util.Humanizer;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Mouse;

/** Humanized CPS — only while holding LMB + weapon. */
public class AutoClicker extends Module {

    public int minCps = 8;
    public int maxCps = 11;
    public boolean weaponsOnly = true;
    private long nextClick;

    public AutoClicker() {
        super("AutoClicker", "Humanized CPS", Category.COMBAT);
    }

    @Override
    public void onTick() {
        if (mc.thePlayer == null) return;
        if (mc.currentScreen != null) return;
        if (!Mouse.isButtonDown(0)) return;
        if (weaponsOnly && !CombatUtil.isWeapon(mc.thePlayer.getHeldItem())) return;
        if (mc.thePlayer.isUsingItem()) return;

        long now = System.currentTimeMillis();
        if (now < nextClick) return;

        if (Humanizer.chance(6)) {
            // micro-pause like a real hand
            nextClick = now + 40 + Humanizer.delay(30, 10);
            return;
        }

        KeyBinding.onTick(mc.gameSettings.keyBindAttack.getKeyCode());
        nextClick = now + Humanizer.clickDelay(minCps, maxCps);
    }
}
