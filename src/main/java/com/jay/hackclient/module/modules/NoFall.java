package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.ModeSetting;
import com.jay.hackclient.module.setting.NumberSetting;
import com.jay.hackclient.util.RealPackets;

/** NoFall — prevent fall damage. Packet mode throttled to avoid spam. Public setTag. */
public class NoFall extends Module {
    public final ModeSetting mode = new ModeSetting("Mode", "Style", "Packet", "Packet", "NoGround", "Spoof");
    public final NumberSetting minFall = new NumberSetting("Min Fall", "Start below this fall dist", 2.5, 0.5, 10.0, 0.5);
    private boolean lastSentGround = false;

    public NoFall() {
        super("NoFall", "Prevent fall damage", Category.MOVEMENT);
        addSetting(mode); addSetting(minFall);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        float fd = (float) mc.player.fallDistance;
        if (fd < (float) minFall.get()) { setTag(null); return; }
        switch (mode.get()) {
            case "NoGround" -> {
                if (!lastSentGround) {
                    RealPackets.sendOnGround(false);
                    lastSentGround = true;
                }
            }
            default -> { // Packet / Spoof
                if (!lastSentGround) {
                    RealPackets.sendOnGround(true);
                    mc.player.fallDistance = 0.0f;
                    lastSentGround = true;
                }
            }
        }
        setTag(String.format("%s %.1f", mode.get(), fd));
    }

    @Override
    public void onDisable() {
        lastSentGround = false;
        setTag(null);
    }
}
