package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.BoolSetting;
import com.jay.hackclient.module.setting.NumberSetting;
import com.jay.hackclient.util.RealPackets;
import net.minecraft.block.Blocks;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class HoleFill extends Module {
    public final NumberSetting range = new NumberSetting("Range", "Scan range", 5.0, 2.0, 8.0, 0.5);
    public final NumberSetting delay = new NumberSetting("Delay", "Ms between places", 60, 20, 200, 10);
    public final BoolSetting realPackets = new BoolSetting("Real Packets", "Slot packets", true);
    private long last;
    public HoleFill() { super("HoleFill", "Fill holes near enemies", Category.ANARCHY); addSetting(range); addSetting(delay); addSetting(realPackets); }
    @Override public void onTick() {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        long now = System.currentTimeMillis(); if (now - last < delay.get()) return;
        int slot = findBlock(); if (slot < 0) return;
        BlockPos feet = mc.player.getBlockPos(); int r = (int) range.get();
        for (int x = -r; x <= r; x++) for (int z = -r; z <= r; z++) {
            BlockPos pos = feet.add(x, -1, z);
            if (!isHole(pos)) continue;
            boolean near = false;
            for (PlayerEntity p : mc.world.getPlayers()) {
                if (p == mc.player || !p.isAlive()) continue;
                if (p.squaredDistanceTo(pos.getX()+0.5, pos.getY(), pos.getZ()+0.5) < 9) { near = true; break; }
            }
            if (!near) continue;
            int prev = mc.player.getInventory().selectedSlot;
            if (realPackets.get()) RealPackets.selectSlot(slot); else mc.player.getInventory().selectedSlot = slot;
            if (realPackets.get()) RealPackets.sendLook(mc.player.getYaw(), 80f, true);
            BlockHitResult hit = new BlockHitResult(Vec3d.ofCenter(pos.down()).add(0,0.5,0), Direction.UP, pos.down(), false);
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit); mc.player.swingHand(Hand.MAIN_HAND);
            if (realPackets.get()) RealPackets.selectSlot(prev); else mc.player.getInventory().selectedSlot = prev;
            last = now; return;
        }
    }
    private boolean isHole(BlockPos pos) {
        if (!mc.world.getBlockState(pos).isReplaceable()) return false;
        if (!mc.world.getBlockState(pos.up()).isReplaceable()) return false;
        int solid = 0;
        for (Direction d : new Direction[]{Direction.NORTH,Direction.SOUTH,Direction.EAST,Direction.WEST})
            if (!mc.world.getBlockState(pos.offset(d)).isReplaceable()) solid++;
        return solid >= 3;
    }
    private int findBlock() {
        for (int i = 0; i < 9; i++) {
            ItemStack s = mc.player.getInventory().getStack(i);
            if (s.isEmpty() || !(s.getItem() instanceof BlockItem bi)) continue;
            if (bi.getBlock() == Blocks.OBSIDIAN || bi.getBlock() == Blocks.ENDER_CHEST) return i;
        }
        return -1;
    }
}
