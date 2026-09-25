package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.BoolSetting;
import com.jay.hackclient.module.setting.ModeSetting;
import com.jay.hackclient.module.setting.NumberSetting;
import com.jay.hackclient.util.Humanizer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.hit.EntityHitResult;

/**
 * Sprint Reset — two variants:
 *   W-Tap  → briefly release forward on hit
 *   S-Tap  → briefly press back on hit
 * Real key simulation only — no custom packets.
 */
public class SprintReset extends Module {

    public final ModeSetting mode = new ModeSetting(
            "Mode", "Reset style", "W-Tap",
            "W-Tap", "S-Tap"
    );

    public final NumberSetting holdMs = new NumberSetting(
            "Hold", "How long the tap lasts (ms)", 55, 25, 150, 5
    );

    public final BoolSetting playersOnly = new BoolSetting(
            "Players Only", "Only reset when hitting players", true
    );

    public final BoolSetting requireForward = new BoolSetting(
            "Require W", "Only fire while holding forward", true
    );

    private long wResetUntil;
    private boolean wRestoring;
    private int lastSwing;
    private long sUntil;
    private boolean sHolding;

    public SprintReset() {
        super("SprintReset", "W-Tap or S-Tap sprint reset on hit", Category.COMBAT);
        addSetting(mode);
        addSetting(holdMs);
        addSetting(playersOnly);
        addSetting(requireForward);
    }

    @Override
    public void onDisable() {
        if (mc.options != null) {
            if (wRestoring) wRestoring = false;
            if (sHolding) {
                mc.options.backKey.setPressed(false);
                sHolding = false;
            }
        }
        wResetUntil = 0;
        sUntil = 0;
        lastSwing = 0;
        setTag(null);
    }

    @Override
    public void onEnable() {
        setTag(mode.get());
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.options == null) return;
        setTag(mode.get());
        if ("S-Tap".equals(mode.get())) tickSTap();
        else tickWTap();
    }

    private void tickWTap() {
        long now = System.currentTimeMillis();
        if (now < wResetUntil) {
            mc.options.forwardKey.setPressed(false);
            wRestoring = true;
            return;
        }
        if (wRestoring) wRestoring = false;

        int swing = mc.player.handSwingTicks;
        boolean newSwing = swing > 0 && swing != lastSwing && mc.player.handSwinging;
        lastSwing = swing;
        if (!newSwing) return;
        if (!isValidHit()) return;
        if (requireForward.get() && !mc.options.forwardKey.isPressed()) return;

        int ms = (int) holdMs.get();
        try { ms = Math.max(40, (Humanizer.tapResetMs() + ms) / 2); }
        catch (Throwable t) { ms = Math.max(40, ms); }
        wResetUntil = now + ms;
        mc.options.forwardKey.setPressed(false);
        wRestoring = true;
    }

    private void tickSTap() {
        long now = System.currentTimeMillis();
        if (now < sUntil) {
            mc.options.backKey.setPressed(true);
            sHolding = true;
            return;
        }
        if (sHolding) {
            mc.options.backKey.setPressed(false);
            sHolding = false;
        }
        if (!mc.player.handSwinging || mc.player.handSwingTicks > 3) return;
        if (!isValidHit()) return;
        if (requireForward.get() && !mc.options.forwardKey.isPressed()) return;

        int ms = Math.max(30, (int) holdMs.get());
        sUntil = now + ms;
        mc.options.forwardKey.setPressed(false);
        mc.options.backKey.setPressed(true);
        sHolding = true;
    }

    private boolean isValidHit() {
        if (!(mc.crosshairTarget instanceof EntityHitResult ehr)) return false;
        if (playersOnly.get())
            return ehr.getEntity() instanceof PlayerEntity p && p != mc.player;
        return ehr.getEntity() != mc.player;
    }

    private void setTag(String t) {
        try {
            var f = Module.class.getDeclaredField("tag");
            f.setAccessible(true);
            f.set(this, t);
        } catch (Throwable ignored) {}
    }
}
