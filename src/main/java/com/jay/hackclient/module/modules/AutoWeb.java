package com.jay.hackclient.module.modules;

import com.jay.hackclient.JayHackClient;
import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.BoolSetting;
import com.jay.hackclient.module.setting.NumberSetting;
import com.jay.hackclient.util.RealPackets;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

public class AutoWeb extends Module {
    public final NumberSetting range = new NumberSetting("Range", "Place range", 4.0, 2.0, 6.0, 0.1);
    public final NumberSetting delay = new NumberSetting("Delay", "Ms between places", 80, 30, 300, 10);
    public final BoolSetting realPackets = new BoolSetting("Real Packets", "Slot + look packets", true);
    private long last;
    public AutoWeb() { super("AutoWeb", "Place webs on targets", Category.COMBAT); addSetting(range); addSetting(delay); addSetting(realPackets); }
    @Override public void onTick() {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        long now = System.currentTimeMillis(); if (now - last < delay.get()) return;
        int slot = -1;
        for (int i = 0; i < 9; i++) if (mc.player.getInventory().getStack(i).isOf(Items.COBWEB)) { slot = i; break; }
        if (slot < 0) return;
        PlayerEntity target = null; double best = range.get();
        for (PlayerEntity p : mc.world.getPlayers()) {
            if (p == mc.player || !p.isAlive()) continue;
            try { if (JayHackClient.friendManager != null && JayHackClient.friendManager.isFriend(p.getName().getString())) continue; } catch (Throwable ignored) {}
            double d = mc.player.distanceTo(p); if (d < best) { best = d; target = p; }
        }
        if (target == null) return;
        BlockPos pos = target.getBlockPos();
        if (!mc.world.getBlockState(pos).isReplaceable()) return;
        int prev = mc.player.getInventory().selectedSlot;
        if (realPackets.get()) RealPackets.selectSlot(slot); else mc.player.getInventory().selectedSlot = slot;
        if (realPackets.get()) RealPackets.sendLook(mc.player.getYaw(), 70f, mc.player.isOnGround());
        BlockHitResult hit = new BlockHitResult(Vec3d.ofCenter(pos.down()).add(0,0.5,0), Direction.UP, pos.down(), false);
        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit); mc.player.swingHand(Hand.MAIN_HAND);
        if (realPackets.get()) RealPackets.selectSlot(prev); else mc.player.getInventory().selectedSlot = prev;
        last = now;
    }
}
