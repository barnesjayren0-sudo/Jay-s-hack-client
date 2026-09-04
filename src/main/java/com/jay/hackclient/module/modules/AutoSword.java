package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.util.Humanizer;
import com.jay.hackclient.util.ItemUtil;
import com.jay.hackclient.util.SlotLock;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.EntityHitResult;

/** Best hotbar sword — lowest priority; yields to totem/shield/pot/inv. */
public class AutoSword extends Module {

    private long last;
    private int nextDelay = 150;

    public AutoSword() {
        super("AutoSword", "Best hotbar sword", Category.COMBAT);
    }

    @Override
    public void onEnable() {
        nextDelay = Humanizer.delay(150, 28, 90, 240);
        last = 0;
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        if (SlotLock.isLockedByOther("AutoSword")) return;
        String owner = SlotLock.currentOwner();
        if ("ShieldBreak".equals(owner) || "AutoTotem".equals(owner) || "InvManager".equals(owner))
            return;

        long now = System.currentTimeMillis();
        if (now - last < nextDelay) return;
        if (!shouldSwap()) return;

        PlayerInventory inv = mc.player.getInventory();
        int bestSlot = -1;
        int bestScore = -1;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = inv.getStack(i);
            if (!ItemUtil.isSword(stack)) continue;
            int score = ItemUtil.swordTier(stack) * 1000 + (stack.getMaxDamage() - stack.getDamage());
            if (score > bestScore) {
                bestScore = score;
                bestSlot = i;
            }
        }

        if (bestSlot >= 0 && inv.getSelectedSlot() != bestSlot) {
            if (SlotLock.tryAcquire("AutoSword", 160, SlotLock.PRIO_SWORD)) {
                inv.setSelectedSlot(bestSlot);
                last = now;
                nextDelay = Humanizer.delay(150, 28, 90, 240);
                SlotLock.release("AutoSword");
            }
        }
    }

    private boolean shouldSwap() {
        if (mc.crosshairTarget instanceof EntityHitResult ehr
                && ehr.getEntity() instanceof PlayerEntity) {
            return true;
        }
        if (mc.world == null || mc.player == null) return false;
        for (PlayerEntity p : mc.world.getPlayers()) {
            if (p == mc.player || !p.isAlive()) continue;
            if (mc.player.distanceTo(p) < 6.0) return true;
        }
        return false;
    }
}
