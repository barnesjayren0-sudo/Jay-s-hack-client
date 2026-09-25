package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.BoolSetting;
import com.jay.hackclient.module.setting.NumberSetting;
import com.jay.hackclient.util.RealPackets;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class Burrow extends Module {
    public final NumberSetting height = new NumberSetting("Height", "Jump height packet", 0.42, 0.2, 1.0, 0.01);
    public final BoolSetting realPackets = new BoolSetting("Real Packets", "Position + slot", true);
    public final BoolSetting once = new BoolSetting("Once", "Disable after burrow", true);
    public Burrow() { super("Burrow", "Place block in your feet", Category.ANARCHY); addSetting(height); addSetting(realPackets); addSetting(once); }
    @Override public void onEnable() {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) { setEnabled(false); return; }
        int slot = findBlock(); if (slot < 0) { setEnabled(false); return; }
        BlockPos feet = mc.player.getBlockPos(); int prev = mc.player.getInventory().selectedSlot;
        if (realPackets.get()) {
            double x = mc.player.getX(), y = mc.player.getY(), z = mc.player.getZ();
            RealPackets.sendPosition(x, y + height.get(), z, false); RealPackets.selectSlot(slot);
        } else mc.player.getInventory().selectedSlot = slot;
        BlockHitResult hit = new BlockHitResult(Vec3d.ofCenter(feet.down()).add(0,0.5,0), Direction.UP, feet.down(), false);
        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit); mc.player.swingHand(Hand.MAIN_HAND);
        if (realPackets.get()) { RealPackets.selectSlot(prev); RealPackets.syncPosition(); }
        else mc.player.getInventory().selectedSlot = prev;
        if (once.get()) setEnabled(false);
    }
    private int findBlock() {
        for (int i = 0; i < 9; i++) {
            ItemStack s = mc.player.getInventory().getStack(i);
            if (s.isEmpty() || !(s.getItem() instanceof BlockItem bi)) continue;
            var b = bi.getBlock();
            if (b == Blocks.OBSIDIAN || b == Blocks.ENDER_CHEST || b == Blocks.CRYING_OBSIDIAN || b == Blocks.ANCIENT_DEBRIS) return i;
        }
        for (int i = 0; i < 9; i++) if (mc.player.getInventory().getStack(i).getItem() instanceof BlockItem) return i;
        return -1;
    }
}
