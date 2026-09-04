package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.util.ItemUtil;
import com.jay.hackclient.util.SlotLock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.AxeItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.EntityHitResult;

/** Switch to axe when target is blocking — higher priority than AutoSword. */
public class ShieldBreak extends Module {

    private int prevSlot = -1;
    private long swappedAt;

    public ShieldBreak() {
        super("ShieldBreak", "Axe when they block", Category.COMBAT);
    }

    @Override
    public void onDisable() {
        restore();
    }

    private void restore() {
        if (prevSlot >= 0 && mc.player != null) {
            try {
                mc.player.getInventory().setSelectedSlot(prevSlot);
            } catch (Throwable ignored) {}
            prevSlot = -1;
            SlotLock.release("ShieldBreak");
        }
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) return;

        PlayerEntity target = null;
        if (mc.crosshairTarget instanceof EntityHitResult ehr
                && ehr.getEntity() instanceof PlayerEntity p) {
            target = p;
        }
        if (target == null || !target.isAlive()) {
            if (System.currentTimeMillis() - swappedAt > 350) restore();
            return;
        }

        boolean blocking = target.isBlocking();
        if (!blocking) {
            if (System.currentTimeMillis() - swappedAt > 350) restore();
            return;
        }

        if (mc.player.getMainHandStack().getItem() instanceof AxeItem) return;

        if (!SlotLock.tryAcquire("ShieldBreak", 450, SlotLock.PRIO_SHIELD)) return;

        PlayerInventory inv = mc.player.getInventory();
        int axe = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack s = inv.getStack(i);
            if (s.getItem() instanceof AxeItem) {
                axe = i;
                break;
            }
        }
        if (axe < 0) {
            SlotLock.release("ShieldBreak");
            return;
        }

        if (prevSlot < 0) prevSlot = inv.getSelectedSlot();
        inv.setSelectedSlot(axe);
        swappedAt = System.currentTimeMillis();
    }
}
