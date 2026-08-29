package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.NumberSetting;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

/** Place obsidian trap around nearest enemy. */
public class AutoTrap extends Module {

    public final NumberSetting range = new NumberSetting("Range", "Target range", 5.0, 2.0, 8.0, 0.5);
    private long lastPlace;

    public AutoTrap() {
        super("AutoTrap", "Trap nearest player in obsidian", Category.ANARCHY);
        addSetting(range);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        long now = System.currentTimeMillis();
        if (now - lastPlace < 70) return;

        PlayerEntity target = nearest();
        if (target == null) return;

        int slot = findBlockSlot();
        if (slot < 0) return;

        BlockPos base = target.getBlockPos();
        BlockPos[] spots = {
                base.up(2), // head ceiling
                base.north(), base.south(), base.east(), base.west(),
                base.up().north(), base.up().south(), base.up().east(), base.up().west()
        };

        int prev = getSelected();
        setSelected(slot);

        for (BlockPos targetPos : spots) {
            if (!mc.world.getBlockState(targetPos).isReplaceable()) continue;
            Direction face = Direction.UP;
            BlockPos against = targetPos.down();
            if (mc.world.getBlockState(against).isAir()) {
                against = base;
                face = Direction.UP;
            }
            BlockHitResult hit = new BlockHitResult(Vec3d.ofCenter(against), face, against, false);
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
            mc.player.swingHand(Hand.MAIN_HAND);
            lastPlace = now;
            break;
        }

        setSelected(prev);
    }

    private PlayerEntity nearest() {
        PlayerEntity best = null;
        double bestD = range.get();
        for (PlayerEntity p : mc.world.getPlayers()) {
            if (p == mc.player || !p.isAlive()) continue;
            if (JayFriends.isFriend(p)) continue;
            double d = mc.player.distanceTo(p);
            if (d < bestD) {
                bestD = d;
                best = p;
            }
        }
        return best;
    }

    private int findBlockSlot() {
        for (int i = 0; i < 9; i++) {
            ItemStack s = mc.player.getInventory().getStack(i);
            if (s.isEmpty() || !(s.getItem() instanceof BlockItem bi)) continue;
            Block b = bi.getBlock();
            if (b == Blocks.OBSIDIAN || b == Blocks.CRYING_OBSIDIAN || b == Blocks.ENDER_CHEST) return i;
        }
        return -1;
    }

    private int getSelected() {
        try { return mc.player.getInventory().getSelectedSlot(); }
        catch (Throwable t) { return 0; }
    }

    private void setSelected(int slot) {
        try { mc.player.getInventory().setSelectedSlot(slot); } catch (Throwable ignored) {}
    }

    /** Local friend check without hard dependency issues. */
    private static final class JayFriends {
        static boolean isFriend(PlayerEntity p) {
            try {
                if (com.jay.hackclient.JayHackClient.friendManager == null) return false;
                return com.jay.hackclient.JayHackClient.friendManager.isFriend(p.getName().getString());
            } catch (Throwable t) {
                return false;
            }
        }
    }
}
