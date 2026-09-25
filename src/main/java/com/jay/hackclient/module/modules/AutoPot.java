package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.NumberSetting;
import com.jay.hackclient.util.RealPackets;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;

public class AutoPot extends Module {
    public final NumberSetting health = new NumberSetting("Health", "Pot below HP", 10, 1, 20, 0.5);
    public final NumberSetting delay = new NumberSetting("Delay", "Ms between pots", 400, 100, 1500, 50);
    private long lastPot;
    private int restoreSlot = -1;
    private int stage;
    public AutoPot() {
        super("AutoPot", "Auto splash heal pots", Category.COMBAT);
        addSetting(health); addSetting(delay);
    }
    @Override public void onTick() {
        if (mc.player == null || mc.interactionManager == null) return;
        if (restoreSlot >= 0 && stage == 2) {
            RealPackets.selectSlot(restoreSlot); restoreSlot = -1; stage = 0; return;
        }
        if (mc.player.getHealth() > health.get()) return;
        if (mc.player.hasStatusEffect(StatusEffects.REGENERATION) && mc.player.getHealth() > health.get()*0.7) return;
        if (System.currentTimeMillis() - lastPot < delay.get()) return;
        int pot = -1;
        for (int i = 0; i < 9; i++) {
            ItemStack s = mc.player.getInventory().getStack(i);
            if (s.isEmpty()) continue;
            if (s.isOf(Items.SPLASH_POTION)) { pot = i; break; }
            String n = s.getItem().toString().toLowerCase();
            if (n.contains("splash") && (n.contains("heal") || n.contains("regeneration"))) { pot = i; break; }
        }
        if (pot < 0) { setTag("none"); return; }
        restoreSlot = mc.player.getInventory().selectedSlot;
        RealPackets.selectSlot(pot);
        float oldPitch = mc.player.getPitch();
        RealPackets.sendLook(mc.player.getYaw(), 85f, mc.player.isOnGround());
        mc.player.setPitch(85f);
        mc.interactionManager.interactItem(mc.player, Hand.MAIN_HAND);
        mc.player.setPitch(oldPitch);
        RealPackets.sendLook(mc.player.getYaw(), oldPitch, mc.player.isOnGround());
        lastPot = System.currentTimeMillis(); stage = 2; setTag("pot");
    }
    private void setTag(String t) {
        try { var f = Module.class.getDeclaredField("tag"); f.setAccessible(true); f.set(this, t); } catch (Throwable ignored) {}
    }
}
