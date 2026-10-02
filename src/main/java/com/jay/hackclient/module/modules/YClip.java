package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.BoolSetting;
import com.jay.hackclient.module.setting.NumberSetting;
import com.jay.hackclient.util.RealPackets;

/** YClip — vertical position clip. Null-safe. */
public class YClip extends Module {
    public final NumberSetting amount = new NumberSetting("Amount", "Y offset", -2.0, -10.0, 10.0, 0.5);
    public final BoolSetting realPackets = new BoolSetting("Real Packets", "Position packets", true);
    public final BoolSetting once = new BoolSetting("Once", "Disable after one clip", true);

    public YClip() { super("YClip", "Vertical position clip", Category.MOVEMENT); addSetting(amount); addSetting(realPackets); addSetting(once); }

    @Override public void onEnable() {
        if (mc.player == null) { setEnabled(false); return; }
        setTag("clip " + amount.get());
        double x = mc.player.getX(), y = mc.player.getY() + amount.get(), z = mc.player.getZ();
        mc.player.setPosition(x, y, z);
        if (realPackets.get()) {
            RealPackets.sendPosition(x, y, z, false);
            RealPackets.syncPosition();
        }
        if (once.get()) setEnabled(false);
    }

    @Override public void onDisable() {
        // Flush the clipped coordinate so the server and client agree again
        try {
            if (mc.player != null && realPackets.get()) RealPackets.syncPosition();
        } catch (Throwable ignored) {}
        setTag(null);
    }

    @Override public void onTick() { /* one-shot module */ }
}
