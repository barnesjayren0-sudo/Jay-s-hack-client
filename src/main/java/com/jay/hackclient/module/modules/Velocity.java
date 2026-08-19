package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.settings.ClientSettings;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

/**
 * Horizontal KB only — never scales Y (no slow fall).
 * .jay velmode soft|medium|strong
 * Key: N
 */
public class Velocity extends Module {

    public static boolean packetHandledThisTick = false;
    public static long lastPacketMs = 0;

    public Velocity() {
        super("Velocity", "Horizontal KB only — key N", Category.COMBAT);
        setKeyBind(GLFW.GLFW_KEY_N);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;

        if (packetHandledThisTick) {
            packetHandledThisTick = false;
            return;
        }

        int ht = mc.player.hurtTime;
        if (ht <= 0 || ht > 8) return;

        long now = System.currentTimeMillis();
        if (now - lastPacketMs < 80) return;

        // Don't touch velocity while falling hard / in air unless clearly KB
        Vec3d v = mc.player.getVelocity();
        double horiz = Math.sqrt(v.x * v.x + v.z * v.z);
        if (horiz < 0.22) return;

        // If mostly falling (big negative Y, small XZ), leave alone
        if (v.y < -0.3 && horiz < 0.35) return;

        double hx = Math.max(0.40, horizontalFactor());
        mc.player.setVelocity(v.x * hx, v.y, v.z * hx); // Y untouched
    }

    public static double horizontalFactor() {
        return Math.max(0.40, Math.min(0.95, ClientSettings.velocityHorizontal));
    }

    /** Kept for API compat — always 1.0 (no vertical scaling). */
    public static double verticalFactor() {
        return 1.0;
    }
}
