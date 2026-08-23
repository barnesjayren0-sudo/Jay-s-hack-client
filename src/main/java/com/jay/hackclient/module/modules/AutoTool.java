package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.util.Humanizer;
import com.jay.hackclient.util.SlotLock;
import net.minecraft.block.BlockState;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;

/** Switch to best hotbar tool for the block you are mining. */
public class AutoTool extends Module {

    private long lastSwap;
    private int lastSlot = -1;

    public AutoTool() {
        super("AutoTool", "Best tool for mining target block", Category.WORLD);
    }

    @Override
    public void onDisable() {
        lastSlot = -1;
        SlotLock.release("AutoTool");
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (mc.currentScreen != null) return;
        if (!mc.options.attackKey.isPressed()) return;
        if (SlotLock.isLockedByOther("AutoTool")) return;

        if (mc.crosshairTarget == null || mc.crosshairTarget.getType() != HitResult.Type.BLOCK) return;
        BlockHitResult bhr = (BlockHitResult) mc.crosshairTarget;
        BlockState state = mc.world.getBlockState(bhr.getBlockPos());
        if (state.isAir()) return;

        long now = System.currentTimeMillis();
        if (now - lastSwap < Humanizer.delay(40, 10, 30, 80)) return;

        int best = -1;
        float bestSpeed = 1f;
        for (int i = 0; i < 9; i++) {
            ItemStack stack = mc.player.getInventory().getStack(i);
            if (stack.isEmpty()) continue;
            float speed = stack.getMiningSpeedMultiplier(state);
            if (speed > bestSpeed) {
                bestSpeed = speed;
                best = i;
            }
        }
        if (best < 0) return;

        int current = mc.player.getInventory().getSelectedSlot();
        if (best == current) return;
        if (!SlotLock.tryAcquire("AutoTool", 150)) return;

        try {
            mc.player.getInventory().setSelectedSlot(best);
            lastSlot = best;
            lastSwap = now;
        } catch (Exception ignored) {
            // 1.21 selectedSlot may need accessor — ignore soft fail
        } finally {
            SlotLock.release("AutoTool");
        }
    }
}
