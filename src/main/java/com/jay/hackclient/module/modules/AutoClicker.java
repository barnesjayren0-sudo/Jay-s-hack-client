package com.jay.hackclient.module.modules;

import com.jay.hackclient.JayHackClient;
import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.BoolSetting;
import com.jay.hackclient.module.setting.NumberSetting;
import com.jay.hackclient.util.Humanizer;
import com.jay.hackclient.util.RealPackets;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.EntityHitResult;
import net.minecraft.util.hit.HitResult;

/** AutoClicker — real swing/attack path, public setTag. */
public class AutoClicker extends Module {
    public final NumberSetting cpsMin = new NumberSetting("CPS Min", "Min clicks per second", 8, 1, 20, 1);
    public final NumberSetting cpsMax = new NumberSetting("CPS Max", "Max clicks per second", 12, 1, 20, 1);
    public final BoolSetting onlyEntity = new BoolSetting("Only Entity", "Only on entity", true);
    public final BoolSetting weaponsOnly = new BoolSetting("Weapons Only", "Sword/axe/mace only", true);
    public final BoolSetting cooldownCheck = new BoolSetting("Cooldown", "Respect attack cooldown", true);
    public final BoolSetting realPackets = new BoolSetting("Real Packets", "Vanilla attack", true);
    public final BoolSetting attributeSwap = new BoolSetting("Attr Swap", "Trigger AttributeSwap", true);
    private long lastClick;
    private double currentCps = 10;
    private long nextRollCps;

    public AutoClicker() {
        super("AutoClicker", "Auto attack clicks", Category.COMBAT);
        addSetting(cpsMin); addSetting(cpsMax); addSetting(onlyEntity); addSetting(weaponsOnly);
        addSetting(cooldownCheck); addSetting(realPackets); addSetting(attributeSwap);
    }

    private double rollCps() {
        long now = System.currentTimeMillis();
        if (now >= nextRollCps) {
            double lo = Math.min(cpsMin.get(), cpsMax.get());
            double hi = Math.max(cpsMin.get(), cpsMax.get());
            currentCps = lo + Math.random() * (hi - lo);
            // re-roll at human click-burst boundaries (~every 0.8-2.2s)
            nextRollCps = now + (long) (800 + Math.random() * 1400);
        }
        return currentCps;
    }

    @Override public void onDisable() {
        lastClick = 0;
        setTag(null);
    }

    @Override    public void onTick() {
        try { if (!com.jay.hackclient.util.CombatManager.canCombatModulesRun()) return; } catch (Throwable ignored) {}
        if (mc.player == null || !mc.options.attackKey.isPressed()) return;
        if (weaponsOnly.get()) {
            String n = mc.player.getMainHandStack().getItem().toString().toLowerCase();
            if (!n.contains("sword") && !n.contains("axe") && !n.contains("mace")) { setTag(null); return; }
        }
        long now = System.currentTimeMillis();
        if (now - lastClick < (long) (1000.0 / rollCps())) return;
        if (cooldownCheck.get() && mc.player.getAttackCooldownProgress(0.5f) < 0.9f) return;
        Entity target = null;
        if (mc.crosshairTarget != null && mc.crosshairTarget.getType() == HitResult.Type.ENTITY)
            target = ((EntityHitResult) mc.crosshairTarget).getEntity();
        if (onlyEntity.get() && target == null) { setTag(null); return; }
        if (target instanceof PlayerEntity p) {
            try { if (AntiBot.isBot(p)) return; } catch (Throwable ignored) {}
            try {
                if (JayHackClient.friendManager != null
                        && JayHackClient.friendManager.isFriend(p.getName().getString())) return;
            } catch (Throwable ignored) {}
        }
        if (attributeSwap.get() && target != null) {
            AttributeSwap as = AttributeSwap.get();
            if (as != null) as.trySwapForAttack(target);
        }
        try { Criticals c = Criticals.get(); if (c != null) c.doCritPackets(); } catch (Throwable ignored) {}
        if (target != null && realPackets.get()) {
            RealPackets.attackEntity(target);
        } else if (mc.interactionManager != null && target != null) {
            mc.interactionManager.attackEntity(mc.player, target);
            mc.player.swingHand(Hand.MAIN_HAND);
        } else {
            mc.player.swingHand(Hand.MAIN_HAND);
        }
        lastClick = now;
        setTag(String.format("%.1f", currentCps));
    }
}
