package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.BoolSetting;
import com.jay.hackclient.module.setting.NumberSetting;
import com.jay.hackclient.util.CombatManager;
import com.jay.hackclient.util.Humanizer;
import com.jay.hackclient.util.ItemUtil;
import net.minecraft.entity.Entity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

/**
 * Humanized hold-click.
 * Does NOT force attackKey.setPressed(true) every tick (that stuck the key).
 */
public class AutoClicker extends Module {

    public final NumberSetting minCps = new NumberSetting("MinCPS", "Min clicks/sec", 8, 4, 14, 1);
    public final NumberSetting maxCps = new NumberSetting("MaxCPS", "Max clicks/sec", 11, 5, 16, 1);
    public final BoolSetting weaponsOnly = new BoolSetting("WeaponsOnly", "Sword/axe only", true);
    public final BoolSetting onEntity = new BoolSetting("OnEntity", "Only when aiming entity", true);
    public final BoolSetting breakBlocks = new BoolSetting("Blocks", "Allow mining clicks", false);

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
        if (mc.player == null || mc.options == null || mc.interactionManager == null) return;
        if (!mc.options.attackKey.isPressed()) return;
        if (weaponsOnly.get() && !ItemUtil.isSwordOrAxe(mc.player.getMainHandStack())) return;
        if (mc.player.isUsingItem()) return;

        if (onEntity.get()) {
            if (mc.crosshairTarget == null) return;
            HitResult.Type t = mc.crosshairTarget.getType();
            if (t == HitResult.Type.ENTITY) {
                // ok
            } else if (t == HitResult.Type.BLOCK && breakBlocks.get()) {
                // vanilla hold handles block breaking — don't spam
                return;
            } else {
                return;
            }
        }

        long now = System.currentTimeMillis();
        if (now - lastClick < nextDelay) return;

        if (Humanizer.chance(8)) {
            lastClick = now;
            nextDelay = 40 + Humanizer.delay(25, 10, 20, 80);
            return;
        }

        // Yield to TriggerBot / KillAura
        try {
            Module tb = com.jay.hackclient.JayHackClient.moduleManager.getModuleByName("TriggerBot");
            Module ka = com.jay.hackclient.JayHackClient.moduleManager.getModuleByName("KillAura");
            if ((tb != null && tb.isEnabled()) || (ka != null && ka.isEnabled())) {
                lastClick = now;
                nextDelay = Humanizer.clickDelay();
                return;
            }
        } catch (Throwable ignored) {}

        // Entity hit path only — no sticky key simulation
        if (mc.crosshairTarget instanceof EntityHitResult ehr) {
            Entity e = ehr.getEntity();
            if (e != null && e.isAlive()) {
                mc.interactionManager.attackEntity(mc.player, e);
                mc.player.swingHand(Hand.MAIN_HAND);
                CombatManager.onAttack();
            }
        }

        lastClick = now;
        int min = Math.min(minCps.getInt(), maxCps.getInt());
        int max = Math.max(minCps.getInt(), maxCps.getInt());
        int cps = min + (int) (Math.random() * (max - min + 1));
        nextDelay = Math.max(Humanizer.clickDelay(), 1000 / Math.max(1, cps));
        setTag(String.valueOf(cps));
    }
}
