package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.BoolSetting;
import com.jay.hackclient.module.setting.NumberSetting;
import com.jay.hackclient.util.CombatManager;
import com.jay.hackclient.util.Humanizer;
import com.jay.hackclient.util.ItemUtil;
import net.minecraft.util.hit.HitResult;

/**
 * Humanized hold-click (LB AutoClicker idea, ghost CPS band).
 */
public class AutoClicker extends Module {

    public final NumberSetting minCps = new NumberSetting("MinCPS", "Min clicks/sec", 8, 4, 14, 1);
    public final NumberSetting maxCps = new NumberSetting("MaxCPS", "Max clicks/sec", 11, 5, 16, 1);
    public final BoolSetting weaponsOnly = new BoolSetting("WeaponsOnly", "Sword/axe only", true);
    public final BoolSetting onEntity = new BoolSetting("OnEntity", "Only when aiming entity", true);
    public final BoolSetting breakBlocks = new BoolSetting("Blocks", "Allow on blocks", false);

    private long lastClick;
    private int nextDelay = 110;

    public AutoClicker() {
        super("AutoClicker", "Humanized hold-click", Category.COMBAT);
        addSetting(minCps);
        addSetting(maxCps);
        addSetting(weaponsOnly);
        addSetting(onEntity);
        addSetting(breakBlocks);
    }

    @Override
    public void onEnable() {
        nextDelay = Humanizer.clickDelay();
        lastClick = 0;
    }

    @Override
    public void onTick() {
        if (!CombatManager.canCombatModulesRun()) return;
        if (mc.player == null || mc.options == null) return;
        if (!mc.options.attackKey.isPressed()) return;
        if (weaponsOnly.get() && !ItemUtil.isSwordOrAxe(mc.player.getMainHandStack())) return;
        if (mc.player.isUsingItem()) return;

        if (onEntity.get()) {
            if (mc.crosshairTarget == null) return;
            HitResult.Type t = mc.crosshairTarget.getType();
            if (t == HitResult.Type.ENTITY) {
                // ok
            } else if (t == HitResult.Type.BLOCK && breakBlocks.get()) {
                // ok
            } else {
                return;
            }
        }

        long now = System.currentTimeMillis();
        if (now - lastClick < nextDelay) return;

        // Micro-pause like a real hand
        if (Humanizer.chance(8)) {
            lastClick = now;
            nextDelay = 40 + Humanizer.delay(25, 10, 20, 80);
            return;
        }

        // Simulate click pulse via attack key press state is already held;
        // trigger a swing through interaction when entity targeted
        if (mc.crosshairTarget != null && mc.crosshairTarget.getType() == HitResult.Type.ENTITY
                && mc.interactionManager != null) {
            // Let TriggerBot/KA own entity hits if enabled — only fill gaps
            try {
                Module tb = com.jay.hackclient.JayHackClient.moduleManager.getModuleByName("TriggerBot");
                Module ka = com.jay.hackclient.JayHackClient.moduleManager.getModuleByName("KillAura");
                if ((tb != null && tb.isEnabled()) || (ka != null && ka.isEnabled())) {
                    lastClick = now;
                    nextDelay = Humanizer.clickDelay();
                    return;
                }
            } catch (Throwable ignored) {}
        }

        // Click via left-click simulation path used by client
        try {
            mc.options.attackKey.setPressed(true);
        } catch (Throwable ignored) {}

        lastClick = now;
        int min = Math.min(minCps.getInt(), maxCps.getInt());
        int max = Math.max(minCps.getInt(), maxCps.getInt());
        nextDelay = Humanizer.clickDelay();
        // blend with CPS settings
        int cps = min + (int) (Math.random() * (max - min + 1));
        nextDelay = Math.max(nextDelay, 1000 / Math.max(1, cps));
        setTag(String.valueOf(cps));
    }
}
