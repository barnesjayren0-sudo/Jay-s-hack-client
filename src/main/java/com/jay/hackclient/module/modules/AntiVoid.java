package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.NumberSetting;
import com.jay.hackclient.util.RealPackets;
import net.minecraft.util.math.Vec3d;

/** Save yourself when falling into the void (Y too low). */
public class AntiVoid extends Module {

    public final NumberSetting minY = new NumberSetting("MinY", "Trigger below this Y", -64.0, -128.0, 0.0, 1.0);

    private double lastSafeX, lastSafeY, lastSafeZ;
    private boolean hasSafe;
    private long lastRescue;
    private Object lastWorld;

    public AntiVoid() {
        super("AntiVoid", "Rescue when falling into void", Category.ANARCHY);
        addSetting(minY);
    }

    @Override
    public void onDisable() {
        hasSafe = false;
        lastRescue = 0;
        setTag(null);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) return;

        // A dimension change invalidates the recorded anchor
        if (lastWorld != mc.world) {
            lastWorld = mc.world;
            hasSafe = false;
            setTag(null);
        }

        if (mc.player.isOnGround() && mc.player.getY() > minY.getFloat() + 5) {
            lastSafeX = mc.player.getX();
            lastSafeY = mc.player.getY();
            lastSafeZ = mc.player.getZ();
            hasSafe = true;
        }

        if (mc.player.getY() >= minY.getFloat()) return;

        // Throttle: one rescue packet, not one per tick
        long now = System.currentTimeMillis();
        if (now - lastRescue < 250) return;
        lastRescue = now;

        if (hasSafe) {
            mc.player.setPosition(lastSafeX, lastSafeY + 0.2, lastSafeZ);
            mc.player.setVelocity(Vec3d.ZERO);
            // Tell the server where we ended up, otherwise the client desyncs
            // and the next server position correction snaps us back into the void.
            RealPackets.syncPosition();
            setTag("rescue");
        } else {
            // Soft upward boost if no safe pos recorded
            mc.player.setVelocity(0, 1.2, 0);
            setTag("lift");
        }
    }
}
