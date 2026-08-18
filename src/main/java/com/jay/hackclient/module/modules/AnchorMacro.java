package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.util.MathUtil;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;

/** Helps charge/explode respawn anchors when looking at them (SMP crystal kits). */
public class AnchorMacro extends Module {

    private long last = 0;

    public AnchorMacro() {
        super("AnchorMacro", "Click anchors faster with glowstone", Category.COMBAT);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (mc.currentScreen != null) return;
        if (mc.crosshairTarget == null || mc.crosshairTarget.getType() != HitResult.Type.BLOCK) return;

        BlockHitResult hit = (BlockHitResult) mc.crosshairTarget;
        if (!mc.world.getBlockState(hit.getBlockPos()).isOf(Blocks.RESPAWN_ANCHOR)) return;

        long now = System.currentTimeMillis();
        if (now - last < MathUtil.randomDelay(50, 120)) return;

        // Prefer glowstone in hotbar for charge
        int glow = -1;
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).isOf(Items.GLOWSTONE)) {
                glow = i;
                break;
            }
        }
        int prev = mc.player.getInventory().getSelectedSlot();
        if (glow >= 0) mc.player.getInventory().setSelectedSlot(glow);

        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
        mc.player.swingHand(Hand.MAIN_HAND);

        if (glow >= 0) mc.player.getInventory().setSelectedSlot(prev);
        last = now;
    }
}
