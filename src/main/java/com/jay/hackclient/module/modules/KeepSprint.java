package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.NumberSetting;
import com.jay.hackclient.util.Humanizer;

/**
 * Soft keep-sprint after hits (LB KeepSprint concept, ghost-tuned).
 * Chance-based so it isn't 100% every hit.
 */
public class KeepSprint extends Module {

    public final NumberSetting chance = new NumberSetting("Chance", "% of hits to keep sprint", 70, 30, 100, 5);

    private boolean wasSwinging;
    private int reapplyTicks;

    public KeepSprint() {
        super("KeepSprint", "Soft sprint keep after hit", Category.MOVEMENT);
        addSetting(chance);
    }

    @Override
    public void onDisable() {
        reapplyTicks = 0;
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.options == null) return;
        if (!mc.options.forwardKey.isPressed()) {
            reapplyTicks = 0;
            return;
        }
        if (mc.player.isSneaking() || mc.player.horizontalCollision) return;
        if (mc.player.getHungerManager().getFoodLevel() <= 6) return;

        boolean swinging = mc.player.handSwinging || mc.player.handSwingTicks > 0;
        if (swinging && !wasSwinging) {
            if (Humanizer.chance(chance.getInt())) {
                reapplyTicks = 2 + (int) (Math.random() * 2);
            }
        }
        wasSwinging = swinging;

        if (reapplyTicks > 0) {
            reapplyTicks--;
            if (!mc.player.isSprinting()) {
                mc.player.setSprinting(true);
            }
        }
    }
}
