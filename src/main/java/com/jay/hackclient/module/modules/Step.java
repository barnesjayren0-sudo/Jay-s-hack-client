package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.ModeSetting;
import com.jay.hackclient.module.setting.NumberSetting;
import com.jay.hackclient.util.RealPackets;

/** Step — step up blocks. Public setTag, throttled packet mode. */
public class Step extends Module {
    public final ModeSetting mode = new ModeSetting("Mode", "Style", "Vanilla", "Vanilla", "Packet");
    public final NumberSetting height = new NumberSetting("Height", "Step height", 1.0, 0.6, 2.5, 0.1);
    private float oldStep = 0.6f;
    private long lastPacketStep;
    private float appliedStep = -1f;

    public Step() { super("Step", "Step up blocks", Category.MOVEMENT); addSetting(mode); addSetting(height); }

    @Override public void onEnable() {
        appliedStep = -1f;
        if (mc.player != null) {
            try {
                var attr = mc.player.getAttributeInstance(net.minecraft.entity.attribute.EntityAttributes.STEP_HEIGHT);
                if (attr != null) oldStep = (float) attr.getBaseValue();
            } catch (Throwable ignored) {}
        }
    }

    @Override public void onDisable() {
        if (mc.player != null) try { applyStep(oldStep); } catch (Throwable ignored) {}
        appliedStep = -1f;
        setTag(null);
    }

    @Override public void onTick() {
        if (mc.player == null) return;
        float want = (float) height.get();
        setTag(String.format("%.1f", want));
        // Only touch the attribute when it actually changed
        if (Math.abs(want - appliedStep) > 0.001f) {
            try { applyStep(want); appliedStep = want; } catch (Throwable ignored) {}
        }
        if ("Packet".equals(mode.get()) && mc.player.horizontalCollision && mc.player.isOnGround()) {
            long now = System.currentTimeMillis();
            if (now - lastPacketStep >= 200) {
                lastPacketStep = now;
                RealPackets.sendPosition(mc.player.getX(), mc.player.getY() + want * 0.5, mc.player.getZ(), false);
                RealPackets.sendPosition(mc.player.getX(), mc.player.getY() + want, mc.player.getZ(), true);
                // Ground + position in one packet so the server keeps the new Y
                RealPackets.syncPosition();
            }
        }
    }

    /** 1.21.11: step height lives in the generic.step_height attribute. */
    private void applyStep(float h) {
        var attr = mc.player.getAttributeInstance(net.minecraft.entity.attribute.EntityAttributes.STEP_HEIGHT);
        if (attr != null) attr.setBaseValue(h);
    }
}
