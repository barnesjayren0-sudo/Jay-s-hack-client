package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.util.Humanizer;
import com.jay.hackclient.util.SlotLock;
import net.minecraft.block.BlockState;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;

import java.util.Locale;

/**
 * Switch to best hotbar tool while mining.
 * Uses PlayerInventory getSelectedSlot / setSelectedSlot (same as AutoSword).
 */
public class AutoTool extends Module {

    private long lastSwap;

    public AutoTool() {
        super("AutoTool", "Best tool for mining target block", Category.WORLD);
    }

    @Override
    public void onDisable() {
        SlotLock.release("AutoTool");
        setTag(null);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) return;
        if (mc.currentScreen != null) return;
        if (!mc.options.attackKey.isPressed()) { SlotLock.release("AutoTool"); return; }
        if (SlotLock.isLockedByOther("AutoTool")) return;

        if (mc.crosshairTarget == null || mc.crosshairTarget.getType() != HitResult.Type.BLOCK) {
            setTag(null);
            return;
        }

        BlockHitResult bhr = (BlockHitResult) mc.crosshairTarget;
        BlockState state = mc.world.getBlockState(bhr.getBlockPos());
        if (state.isAir()) { setTag(null); return; }

        long now = System.currentTimeMillis();
        if (now - lastSwap < Humanizer.delay(40, 10, 30, 80)) return;

        PlayerInventory inv = mc.player.getInventory();
        int best = -1;
        float bestSpeed = 1.0f;

        for (int i = 0; i < 9; i++) {
            ItemStack stack = inv.getStack(i);
            if (stack.isEmpty()) continue;
            float speed = stack.getMiningSpeedMultiplier(state);
            if (speed > bestSpeed) {
                bestSpeed = speed;
                best = i;
            }
        }

        if (best < 0) { setTag(null); return; }
        if (inv.getSelectedSlot() == best) { setTag(itemName(inv, best)); return; }
        // The lock is deliberately held for the swap window: releasing it in a
        // finally block made every other SlotLock consumer see it as free.
        if (!SlotLock.tryAcquire("AutoTool", 220)) return;

        try {
            inv.setSelectedSlot(best);
            lastSwap = now;
            setTag(itemName(inv, best));
        } catch (Exception ignored) {
        }
    }

    /** Short label for the held tool, shown as the module tag. */
    private String itemName(PlayerInventory inv, int slot) {
        ItemStack stack = inv.getStack(slot);
        if (stack == null || stack.isEmpty()) return null;
        String n = stack.getItem().toString().toLowerCase(Locale.ROOT);
        int i = n.lastIndexOf('.');
        String shortName = i >= 0 ? n.substring(i + 1) : n;
        return shortName.isEmpty() ? null : shortName;
    }
}
