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
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class SelfTrap extends Module {
    public final NumberSetting delay = new NumberSetting("Delay", "Ms between places", 60, 20, 200, 10);
    public final BoolSetting realPackets = new BoolSetting("Real Packets", "Slot packets", true);
    public final BoolSetting autoDisable = new BoolSetting("Auto Disable", "Off when done", true);
    private long last;
    public SelfTrap() { super("SelfTrap", "Trap yourself (head block)", Category.ANARCHY); addSetting(delay); addSetting(realPackets); addSetting(autoDisable); }
    @Override public void onTick() {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        long now = System.currentTimeMillis(); if (now - last < delay.get()) return;
        var above = mc.player.getBlockPos().up(); var head = mc.player.getBlockPos().up(2);
        if (!mc.world.getBlockState(above).isReplaceable() && !mc.world.getBlockState(head).isReplaceable()) {
            if (autoDisable.get()) setEnabled(false); return;
        }
        int slot = findBlock(); if (slot < 0) return;
        int prev = mc.player.getInventory().selectedSlot;
        if (realPackets.get()) RealPackets.selectSlot(slot); else mc.player.getInventory().selectedSlot = slot;
        BlockHitResult hit = new BlockHitResult(Vec3d.ofCenter(mc.player.getBlockPos()).add(0,1,0), Direction.UP, mc.player.getBlockPos(), false);
        if (realPackets.get()) RealPackets.sendLook(mc.player.getYaw(), -60f, true);
        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit); mc.player.swingHand(Hand.MAIN_HAND);
        if (realPackets.get()) RealPackets.selectSlot(prev); else mc.player.getInventory().selectedSlot = prev;
        last = now;
    }
    private int findBlock() {
        for (int i = 0; i < 9; i++) {
            ItemStack s = mc.player.getInventory().getStack(i);
            if (s.isEmpty() || !(s.getItem() instanceof BlockItem bi)) continue;
            if (bi.getBlock() == Blocks.OBSIDIAN || bi.getBlock() == Blocks.ENDER_CHEST || bi.getBlock() == Blocks.CRYING_OBSIDIAN) return i;
        }
        for (int i = 0; i < 9; i++) if (mc.player.getInventory().getStack(i).getItem() instanceof BlockItem) return i;
        return -1;
    }
}
