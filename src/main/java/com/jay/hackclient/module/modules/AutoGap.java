package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.BoolSetting;
import com.jay.hackclient.module.setting.NumberSetting;
import com.jay.hackclient.util.MathUtil;
import com.jay.hackclient.util.SlotLock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

/** Eat gap when HP low — optional combat-only. */
public class AutoGap extends Module {

    public final NumberSetting health = new NumberSetting("Health", "Eat under this HP", 14.0, 4.0, 20.0, 0.5);
    public final BoolSetting combatOnly = new BoolSetting("CombatOnly", "Only when enemy nearby", true);
    public final NumberSetting range = new NumberSetting("Range", "Enemy range for combat", 8.0, 3.0, 16.0, 0.5);

    private long lastEat;
    private int savedSlot = -1;

    public AutoGap() {
        super("AutoGap", "Gapple at low HP (UHC/kit)", Category.PLAYER);
        addSetting(health);
        addSetting(combatOnly);
        addSetting(range);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.interactionManager == null) return;
        if (mc.currentScreen != null) return;
        if (mc.player.isUsingItem()) return;
        if (SlotLock.isLockedByOther("AutoGap")) return;

        if (savedSlot >= 0) {
            try { mc.player.getInventory().setSelectedSlot(savedSlot); } catch (Throwable ignored) {}
            savedSlot = -1;
            SlotLock.release("AutoGap");
        }

        float hp = mc.player.getHealth() + mc.player.getAbsorptionAmount();
        if (hp > health.getFloat()) return;
        if (combatOnly.get() && !enemyNearby()) return;

        long now = System.currentTimeMillis();
        if (now - lastEat < MathUtil.randomDelay(700, 1100)) return;

        int slot = findGap();
        if (slot < 0) return;
        if (!SlotLock.tryAcquire("AutoGap", 1800, 25)) return;

        int prev = 0;
        try { prev = mc.player.getInventory().getSelectedSlot(); } catch (Throwable ignored) {}
        try { mc.player.getInventory().setSelectedSlot(slot); } catch (Throwable ignored) {}
        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);

        if (mc.player.isUsingItem()) {
            savedSlot = prev;
        } else {
            try { mc.player.getInventory().setSelectedSlot(prev); } catch (Throwable ignored) {}
            SlotLock.release("AutoGap");
        }
        lastEat = now;
    }

    private boolean enemyNearby() {
        if (mc.world == null) return false;
        double r = range.get();
        for (PlayerEntity p : mc.world.getPlayers()) {
            if (p == mc.player || !p.isAlive()) continue;
            try { if (AntiBot.isBot(p)) continue; } catch (Throwable ignored) {}
            if (mc.player.distanceTo(p) <= r) return true;
        }
        // also if recently hurt
        return mc.player.hurtTime > 0;
    }

    @Override
    public void onDisable() {
        if (mc.player != null && savedSlot >= 0) {
            try { mc.player.getInventory().setSelectedSlot(savedSlot); } catch (Throwable ignored) {}
        }
        savedSlot = -1;
        SlotLock.release("AutoGap");
    }

    private int findGap() {
        for (int i = 0; i < 9; i++) {
            ItemStack s = mc.player.getInventory().getStack(i);
            if (s.isOf(Items.GOLDEN_APPLE) || s.isOf(Items.ENCHANTED_GOLDEN_APPLE)) return i;
        }
        return -1;
    }
}
