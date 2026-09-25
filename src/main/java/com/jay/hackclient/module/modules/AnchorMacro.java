package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.BoolSetting;
import com.jay.hackclient.module.setting.NumberSetting;
import com.jay.hackclient.util.RealPackets;
import net.minecraft.block.Blocks;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.hit.HitResult;

public class AnchorMacro extends Module {
    public final NumberSetting delay = new NumberSetting("Delay", "Ms between clicks", 80, 30, 200, 10);
    public final BoolSetting realPackets = new BoolSetting("Real Packets", "Slot packets", true);
    private long last;
    public AnchorMacro() { super("AnchorMacro", "Fast charge/explode anchors", Category.COMBAT); addSetting(delay); addSetting(realPackets); }
    @Override public void onTick() {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (mc.currentScreen != null) return;
        if (mc.crosshairTarget == null || mc.crosshairTarget.getType() != HitResult.Type.BLOCK) return;
        BlockHitResult hit = (BlockHitResult) mc.crosshairTarget;
        if (!mc.world.getBlockState(hit.getBlockPos()).isOf(Blocks.RESPAWN_ANCHOR)) return;
        long now = System.currentTimeMillis(); if (now - last < delay.get()) return;
        int glow = -1;
        for (int i = 0; i < 9; i++) if (mc.player.getInventory().getStack(i).isOf(Items.GLOWSTONE)) { glow = i; break; }
        int prev = mc.player.getInventory().selectedSlot;
        if (glow >= 0) { if (realPackets.get()) RealPackets.selectSlot(glow); else mc.player.getInventory().selectedSlot = glow; }
        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit); mc.player.swingHand(Hand.MAIN_HAND);
        if (glow >= 0) { if (realPackets.get()) RealPackets.selectSlot(prev); else mc.player.getInventory().selectedSlot = prev; }
        last = now;
    }
}
