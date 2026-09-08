package com.jay.client18.module.modules;

import com.jay.client18.module.Module;
import com.jay.client18.util.CombatUtil;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.input.Mouse;

public class AutoClicker extends Module {

    public int minCps = 9;
    public int maxCps = 13;
    public boolean weaponsOnly = true;
    private long nextClick;

    public AutoClicker() {
        super("AutoClicker", "Randomized CPS", Category.COMBAT);
    }

    @Override
    public void onTick() {
        if (mc.thePlayer == null) return;
        if (mc.currentScreen != null) return;
        if (!Mouse.isButtonDown(0)) return;
        if (weaponsOnly && !CombatUtil.isWeapon(mc.thePlayer.getHeldItem())) return;

        long now = System.currentTimeMillis();
        if (now < nextClick) return;

        KeyBinding.onTick(mc.gameSettings.keyBindAttack.getKeyCode());
        int cps = minCps + (int) (Math.random() * (maxCps - minCps + 1));
        nextClick = now + (1000 / Math.max(1, cps)) + (long) (Math.random() * 15);
    }
}
