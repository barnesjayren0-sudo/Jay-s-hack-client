package com.jay.hackclient.module.modules;

import com.jay.hackclient.JayHackClient;
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

public class AutoTrap extends Module {
    public final NumberSetting range = new NumberSetting("Range", "Target range", 5.0, 2.0, 8.0, 0.5);
    public final NumberSetting delay = new NumberSetting("Delay", "Ms between places", 50, 20, 200, 10);
    public final BoolSetting realPackets = new BoolSetting("Real Packets", "Slot + look", true);
    private long last;
    public AutoTrap() { super("AutoTrap", "Trap nearest enemy", Category.ANARCHY); addSetting(range); addSetting(delay); addSetting(realPackets); }
    @Override public void onTick() {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        long now = System.currentTimeMillis(); if (now - last < delay.get()) return;
        PlayerEntity target = null; double best = range.get();
        for (PlayerEntity p : mc.world.getPlayers()) {
            if (p == mc.player || !p.isAlive()) continue;
            try { if (JayHackClient.friendManager != null && JayHackClient.friendManager.isFriend(p.getName().getString())) continue; } catch (Throwable ignored) {}
            double d = mc.player.distanceTo(p); if (d < best) { best = d; target = p; }
        }
        if (target == null) return;
        int slot = findBlock(); if (slot < 0) return;
        BlockPos feet = target.getBlockPos();
        BlockPos[] places = { feet.up(2), feet.north(), feet.south(), feet.east(), feet.west(),
            feet.up().north(), feet.up().south(), feet.up().east(), feet.up().west() };
        for (BlockPos pos : places) {
            if (!mc.world.getBlockState(pos).isReplaceable()) continue;
            int prev = mc.player.getInventory().selectedSlot;
            if (realPackets.get()) RealPackets.selectSlot(slot); else mc.player.getInventory().selectedSlot = slot;
            if (realPackets.get()) RealPackets.sendLook(mc.player.getYaw(), 40f, mc.player.isOnGround());
            BlockHitResult hit = new BlockHitResult(Vec3d.ofCenter(pos.down()).add(0,0.5,0), Direction.UP, pos.down(), false);
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit); mc.player.swingHand(Hand.MAIN_HAND);
            if (realPackets.get()) RealPackets.selectSlot(prev); else mc.player.getInventory().selectedSlot = prev;
            last = now; return;
        }
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
