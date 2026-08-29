package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.NumberSetting;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

/** Fill nearby 1x1 holes with blocks from hotbar. */
public class HoleFill extends Module {

    public final NumberSetting range = new NumberSetting("Range", "Scan radius", 4.0, 1.0, 6.0, 0.5);
    public final NumberSetting delay = new NumberSetting("Delay", "Ms between places", 60, 0, 200, 5);

    private long last;

    public HoleFill() {
        super("HoleFill", "Fill nearby safe holes", Category.ANARCHY);
        addSetting(range);
        addSetting(delay);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        long now = System.currentTimeMillis();
        if (now - last < delay.getInt()) return;

        int slot = findBlock();
        if (slot < 0) return;

        BlockPos origin = mc.player.getBlockPos();
        int r = range.getInt();

        int prev = 0;
        try { prev = mc.player.getInventory().getSelectedSlot(); } catch (Throwable ignored) {}
        try { mc.player.getInventory().setSelectedSlot(slot); } catch (Throwable ignored) {}

        outer:
        for (int x = -r; x <= r; x++) {
            for (int z = -r; z <= r; z++) {
                for (int y = -2; y <= 1; y++) {
                    BlockPos feet = origin.add(x, y, z);
                    if (!isFillableHole(feet)) continue;
                    if (feet.equals(origin)) continue;

                    BlockHitResult hit = new BlockHitResult(
                            Vec3d.ofCenter(feet.down()), Direction.UP, feet.down(), false);
                    mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
                    mc.player.swingHand(Hand.MAIN_HAND);
                    last = now;
                    break outer;
                }
            }
        }

        try { mc.player.getInventory().setSelectedSlot(prev); } catch (Throwable ignored) {}
    }

    private boolean isFillableHole(BlockPos feet) {
        if (!mc.world.getBlockState(feet).isAir()) return false;
        if (!mc.world.getBlockState(feet.up()).isAir()) return false;
        if (!isSolid(feet.down())) return false;
        return isSolid(feet.north()) && isSolid(feet.south())
                && isSolid(feet.east()) && isSolid(feet.west());
    }

    private boolean isSolid(BlockPos p) {
        return !mc.world.getBlockState(p).getCollisionShape(mc.world, p).isEmpty();
    }

    private int findBlock() {
        for (int i = 0; i < 9; i++) {
            ItemStack s = mc.player.getInventory().getStack(i);
            if (s.isEmpty() || !(s.getItem() instanceof BlockItem bi)) continue;
            Block b = bi.getBlock();
            if (b == Blocks.OBSIDIAN || b == Blocks.CRYING_OBSIDIAN
                    || b == Blocks.ENDER_CHEST || b == Blocks.COBBLESTONE
                    || b == Blocks.NETHERRACK) return i;
        }
        return -1;
    }
}
