package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.BoolSetting;
import com.jay.hackclient.module.setting.NumberSetting;
import com.jay.hackclient.util.RealPackets;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

public class ShieldBreak extends Module {
    public final NumberSetting range = new NumberSetting("Range", "Target range", 3.2, 2.5, 4.5, 0.1);
    public final BoolSetting realPackets = new BoolSetting("Real Packets", "Attack + slot packets", true);
    private int restore = -1; private long restoreAt;
    public ShieldBreak() { super("ShieldBreak", "Axe-swap to disable shields", Category.COMBAT); addSetting(range); addSetting(realPackets); }
    @Override public void onDisable() { if (restore >= 0 && mc.player != null) select(restore); restore = -1; }
    @Override public void onTick() {
        if (mc.player == null || mc.world == null) return;
        if (restore >= 0 && System.currentTimeMillis() > restoreAt) { select(restore); restore = -1; }
        PlayerEntity target = null;
        if (mc.crosshairTarget != null && mc.crosshairTarget.getType() == HitResult.Type.ENTITY) {
            var e = ((EntityHitResult) mc.crosshairTarget).getEntity();
            if (e instanceof PlayerEntity p && p.isBlocking()) target = p;
        }
        if (target == null) {
            for (PlayerEntity p : mc.world.getPlayers()) {
                if (p == mc.player || !p.isAlive() || !p.isBlocking()) continue;
                if (mc.player.distanceTo(p) <= range.get()) { target = p; break; }
            }
        }
        if (target == null) return;
        int axe = findAxe(); if (axe < 0) return;
        int cur = mc.player.getInventory().selectedSlot;
        if (axe != cur) { if (restore < 0) restore = cur; select(axe); }
        if (realPackets.get()) RealPackets.attackEntity(target);
        else if (mc.interactionManager != null) { mc.interactionManager.attackEntity(mc.player, target); mc.player.swingHand(net.minecraft.util.Hand.MAIN_HAND); }
        restoreAt = System.currentTimeMillis() + 200;
    }
    private int findAxe() {
        for (int i = 0; i < 9; i++) {
            String n = mc.player.getInventory().getStack(i).getItem().toString().toLowerCase();
            if (n.contains("axe") && !n.contains("pickaxe")) return i;
        }
        return -1;
    }
    private void select(int slot) { if (realPackets.get()) RealPackets.selectSlot(slot); else mc.player.getInventory().selectedSlot = slot; }
}
