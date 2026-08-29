package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

/** Place a block above your head (ceiling trap). */
public class SelfTrap extends Module {

    private long lastPlace;

    public SelfTrap() {
        super("SelfTrap", "Block above head for cover", Category.ANARCHY);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        long now = System.currentTimeMillis();
        if (now - lastPlace < 120) return;

        BlockPos above = mc.player.getBlockPos().up(2);
        if (!mc.world.getBlockState(above).isReplaceable()) return;

        int slot = findBlockSlot();
        if (slot < 0) return;

        int prev = mc.player.getInventory().getSelectedSlot();
        mc.player.getInventory().setSelectedSlot(slot);

        BlockHitResult hit = new BlockHitResult(
                Vec3d.ofCenter(above.down()),
                Direction.UP,
                above.down(),
                false
        );
        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
        mc.player.swingHand(Hand.MAIN_HAND);
        lastPlace = now;

        mc.player.getInventory().setSelectedSlot(prev);
    }

    private int findBlockSlot() {
        for (int i = 0; i < 9; i++) {
            ItemStack s = mc.player.getInventory().getStack(i);
            if (s.isEmpty() || !(s.getItem() instanceof BlockItem bi)) continue;
            Block b = bi.getBlock();
            if (b == Blocks.OBSIDIAN || b == Blocks.CRYING_OBSIDIAN
                    || b == Blocks.ENDER_CHEST || b == Blocks.ANCIENT_DEBRIS) {
                return i;
            }
        }
        return -1;
    }
}
