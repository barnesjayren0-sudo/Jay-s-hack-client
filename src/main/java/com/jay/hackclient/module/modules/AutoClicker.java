package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.BoolSetting;
import com.jay.hackclient.module.setting.NumberSetting;
import com.jay.hackclient.util.RealPackets;
import net.minecraft.entity.Entity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

public class AutoClicker extends Module {
    public final NumberSetting cps = new NumberSetting("CPS", "Clicks per second", 10, 1, 20, 1);
    public final BoolSetting onlyEntity = new BoolSetting("Only Entity", "Only on entity", true);
    public final BoolSetting weaponsOnly = new BoolSetting("Weapons Only", "Sword/axe only", true);
    public final BoolSetting realPackets = new BoolSetting("Real Packets", "Vanilla attack", true);
    public final BoolSetting attributeSwap = new BoolSetting("Attr Swap", "Trigger AttributeSwap", true);
    private long lastClick;
    public AutoClicker() {
        super("AutoClicker", "Auto attack clicks", Category.COMBAT);
        addSetting(cps); addSetting(onlyEntity); addSetting(weaponsOnly); addSetting(realPackets); addSetting(attributeSwap);
    }
    @Override public void onTick() {
        if (mc.player == null || !mc.options.attackKey.isPressed()) return;
        if (weaponsOnly.get()) {
            String n = mc.player.getMainHandStack().getItem().toString().toLowerCase();
            if (!n.contains("sword") && !n.contains("axe") && !n.contains("mace")) return;
        }
        long interval = (long)(1000.0 / cps.get());
        long now = System.currentTimeMillis();
        if (now - lastClick < interval) return;
        Entity target = null;
        if (mc.crosshairTarget != null && mc.crosshairTarget.getType() == HitResult.Type.ENTITY)
            target = ((EntityHitResult)mc.crosshairTarget).getEntity();
        if (onlyEntity.get() && target == null) return;
        if (attributeSwap.get()) {
            AttributeSwap as = AttributeSwap.get();
            if (as != null) as.trySwapForAttack(target);
        }
        try { Criticals c = Criticals.get(); if (c != null) c.doCritPackets(); } catch (Throwable ignored) {}
        if (target != null && realPackets.get()) RealPackets.attackEntity(target);
        else if (mc.interactionManager != null && target != null) {
            mc.interactionManager.attackEntity(mc.player, target);
            mc.player.swingHand(Hand.MAIN_HAND);
        } else mc.player.swingHand(Hand.MAIN_HAND);
        lastClick = now;
        setTag(String.valueOf((int)cps.get()));
    }
    private void setTag(String t) {
        try { var f = Module.class.getDeclaredField("tag"); f.setAccessible(true); f.set(this, t); } catch (Throwable ignored) {}
    }
}
