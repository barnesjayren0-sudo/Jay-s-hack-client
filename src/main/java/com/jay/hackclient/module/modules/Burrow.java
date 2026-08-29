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
import org.lwjgl.glfw.GLFW;

/** Instant self-burrow — place block at feet and sit inside (client assist). */
public class Burrow extends Module {

    public Burrow() {
        super("Burrow", "Burrow into floor with obsidian", Category.ANARCHY);
        setKeyBind(GLFW.GLFW_KEY_B);
    }

    @Override
    public void onEnable() {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) {
            setEnabled(false);
            return;
        }

        int slot = findBlockSlot();
        if (slot < 0) {
            setEnabled(false);
            return;
        }

        BlockPos feet = mc.player.getBlockPos();

        // Jump assist then place under
        mc.player.setVelocity(mc.player.getVelocity().x, 0.42, mc.player.getVelocity().z);

        int prev = 0;
        try { prev = mc.player.getInventory().getSelectedSlot(); } catch (Throwable ignored) {}
        try { mc.player.getInventory().setSelectedSlot(slot); } catch (Throwable ignored) {}

        BlockHitResult hit = new BlockHitResult(
                Vec3d.ofCenter(feet.down()),
                Direction.UP,
                feet.down(),
                false
        );

        // Prefer replacing feet air after jump
        if (mc.world.getBlockState(feet).isReplaceable()) {
            hit = new BlockHitResult(Vec3d.ofCenter(feet.down()), Direction.UP, feet.down(), false);
        }

        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
        mc.player.swingHand(Hand.MAIN_HAND);

        // Clip slightly into block
        mc.player.setPosition(mc.player.getX(), feet.getY() + 0.01, mc.player.getZ());

        try { mc.player.getInventory().setSelectedSlot(prev); } catch (Throwable ignored) {}

        // one-shot
        setEnabled(false);
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
