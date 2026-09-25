package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.BoolSetting;
import com.jay.hackclient.util.RealPackets;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

public class AutoSword extends Module {
    public final BoolSetting realPackets = new BoolSetting("Real Packets", "UpdateSelectedSlotC2SPacket", true);
    public final BoolSetting onlyPlayers = new BoolSetting("Players Only", "Only for players", true);
    private int restore = -1; private long restoreAt;
    public AutoSword() { super("AutoSword", "Auto switch to sword on target", Category.COMBAT); addSetting(realPackets); addSetting(onlyPlayers); }
    @Override public void onDisable() { if (restore >= 0 && mc.player != null) select(restore); restore = -1; }
    @Override public void onTick() {
        if (mc.player == null) return;
        if (restore >= 0 && System.currentTimeMillis() > restoreAt) { select(restore); restore = -1; }
        if (mc.crosshairTarget == null || mc.crosshairTarget.getType() != HitResult.Type.ENTITY) return;
        Entity e = ((EntityHitResult) mc.crosshairTarget).getEntity();
        if (onlyPlayers.get() && !(e instanceof PlayerEntity)) return;
        if (e == mc.player) return;
        int best = findSword(); if (best < 0) return;
        int cur = mc.player.getInventory().selectedSlot; if (best == cur) return;
        if (restore < 0) restore = cur; select(best); restoreAt = System.currentTimeMillis() + 600;
    }
    private int findSword() {
        int best = -1, score = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack s = mc.player.getInventory().getStack(i); if (s.isEmpty()) continue;
            String n = s.getItem().toString().toLowerCase(); int sc = 0;
            if (n.contains("netherite_sword")) sc = 50; else if (n.contains("diamond_sword")) sc = 40;
            else if (n.contains("iron_sword")) sc = 30; else if (n.contains("sword")) sc = 20;
            else if (n.contains("netherite_axe")) sc = 35; else if (n.contains("diamond_axe")) sc = 28;
            else if (n.contains("axe") && !n.contains("pickaxe")) sc = 15;
            if (sc > score) { score = sc; best = i; }
        }
        return best;
    }
    private void select(int slot) { if (realPackets.get()) RealPackets.selectSlot(slot); else mc.player.getInventory().selectedSlot = slot; }
}
