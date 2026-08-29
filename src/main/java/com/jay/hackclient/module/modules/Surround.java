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

/** Place protective blocks around feet (obsidian / crying / ender chest). */
public class Surround extends Module {

    private long lastPlace;

    public Surround() {
        super("Surround", "Box yourself with obsidian", Category.ANARCHY);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!mc.player.isOnGround()) return;
        long now = System.currentTimeMillis();
        if (now - lastPlace < 80) return;

        BlockPos feet = mc.player.getBlockPos();
        Direction[] dirs = { Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST };

        int slot = findBlockSlot();
        if (slot < 0) return;

        int prev = mc.player.getInventory().getSelectedSlot();
        for (Direction d : dirs) {
            BlockPos target = feet.offset(d);
            if (!mc.world.getBlockState(target).isReplaceable()) continue;

            mc.player.getInventory().setSelectedSlot(slot);
            BlockHitResult hit = new BlockHitResult(
                    Vec3d.ofCenter(target.down()),
                    Direction.UP,
                    target.down(),
                    false
            );
            // Prefer placing against floor under target
            if (mc.world.getBlockState(target.down()).isAir()) {
                hit = new BlockHitResult(Vec3d.ofCenter(feet), d, feet, false);
            }
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
            mc.player.swingHand(Hand.MAIN_HAND);
            lastPlace = now;
            break; // one per tick — looks less blatant
        }
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
