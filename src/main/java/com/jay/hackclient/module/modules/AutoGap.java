package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.BoolSetting;
import com.jay.hackclient.module.setting.NumberSetting;
import com.jay.hackclient.util.Humanizer;
import com.jay.hackclient.util.SlotLock;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

/** Eat gap when HP low — kit/UHC staple. */
public class AutoGap extends Module {

    public final NumberSetting health = new NumberSetting("Health", "Eat under HP", 12, 4, 20, 1);
    public final BoolSetting combatOnly = new BoolSetting("CombatOnly", "Only after damage", true);
    public final BoolSetting goldenOnly = new BoolSetting("Golden", "Prefer enchanted gap", true);

    private long lastEat;
    private long lastHurt;
    private int savedSlot = -1;

    public AutoGap() {
        super("AutoGap", "Eat gapple under HP", Category.PLAYER);
        addSetting(health);
        addSetting(combatOnly);
        addSetting(goldenOnly);
    }

    @Override
    public void onDisable() {
        restoreSlot();
    }

    private void restoreSlot() {
        if (savedSlot >= 0 && mc.player != null) {
            try { mc.player.getInventory().setSelectedSlot(savedSlot); } catch (Throwable ignored) {}
            savedSlot = -1;
            SlotLock.release("AutoGap");
        }
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.interactionManager == null) return;
        if (mc.currentScreen != null) return;
        if (mc.player.hurtTime > 0) lastHurt = System.currentTimeMillis();

        float hp = mc.player.getHealth() + mc.player.getAbsorptionAmount();
        if (hp > health.getFloat()) {
            if (mc.player.isUsingItem()) return;
            restoreSlot();
            return;
        }
        if (combatOnly.get() && System.currentTimeMillis() - lastHurt > 3500) return;
        if (mc.player.isUsingItem()) return;

        long now = System.currentTimeMillis();
        if (now - lastEat < Humanizer.swapDelay()) return;
        if (SlotLock.isLockedByOther("AutoGap")) return;

        int slot = findGap();
        if (slot < 0) return;
        if (!SlotLock.tryAcquire("AutoGap", 800, SlotLock.PRIO_POT)) return;

        if (savedSlot < 0) savedSlot = mc.player.getInventory().getSelectedSlot();
        mc.player.getInventory().setSelectedSlot(slot);
        try {
            mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
            lastEat = now;
        } catch (Throwable ignored) {
            restoreSlot();
        }
    }

    private int findGap() {
        int normal = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack s = mc.player.getInventory().getStack(i);
            if (s.isOf(Items.ENCHANTED_GOLDEN_APPLE)) return i;
            if (s.isOf(Items.GOLDEN_APPLE)) normal = i;
        }
        return goldenOnly.get() && normal < 0 ? -1 : normal;
    }
}
