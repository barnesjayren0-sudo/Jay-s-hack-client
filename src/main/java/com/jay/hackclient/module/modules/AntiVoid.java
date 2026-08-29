package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.NumberSetting;
import net.minecraft.util.math.Vec3d;

/** Save yourself when falling into the void (Y too low). */
public class AntiVoid extends Module {

    public final NumberSetting minY = new NumberSetting("MinY", "Trigger below this Y", -64.0, -128.0, 0.0, 1.0);

    private double lastSafeX, lastSafeY, lastSafeZ;
    private boolean hasSafe;

    public AntiVoid() {
        super("AntiVoid", "Rescue when falling into void", Category.ANARCHY);
        addSetting(minY);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) return;

        if (mc.player.isOnGround() && mc.player.getY() > minY.getFloat() + 5) {
            lastSafeX = mc.player.getX();
            lastSafeY = mc.player.getY();
            lastSafeZ = mc.player.getZ();
            hasSafe = true;
        }

        if (mc.player.getY() < minY.getFloat()) {
            if (hasSafe) {
                mc.player.setPosition(lastSafeX, lastSafeY + 0.2, lastSafeZ);
                mc.player.setVelocity(Vec3d.ZERO);
            } else {
                // Soft upward boost if no safe pos recorded
                mc.player.setVelocity(0, 1.2, 0);
            }
        }
    }
}
