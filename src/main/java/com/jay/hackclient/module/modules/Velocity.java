package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.settings.ClientSettings;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

/**
 * Instant velocity — mixin cancels full KB packet, applies reduced same frame.
 * Tick side only cleans residual motion (no delayed "flash").
 *
 * .jay velmode soft|medium|strong
 * Key: N
 */
public class Velocity extends Module {

    public static boolean packetHandledThisTick = false;
    public static long lastPacketMs = 0;

    public Velocity() {
        super("Velocity", "Instant KB cut — key N", Category.COMBAT);
        setKeyBind(GLFW.GLFW_KEY_N);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;

        // If we just handled a packet this frame, don't double-scale
        if (packetHandledThisTick) {
            packetHandledThisTick = false;
            return;
        }

        int ht = mc.player.hurtTime;
        if (ht <= 0 || ht > 6) return;

        // Only residual cleanup when no packet was seen recently
        long now = System.currentTimeMillis();
        if (now - lastPacketMs < 50) return;

        boolean jumping = mc.options != null && mc.options.jumpKey.isPressed();
        double hx = horizontalFactor();
        double hy = jumping ? 1.0 : verticalFactor();

        Vec3d v = mc.player.getVelocity();
        double horiz = Math.sqrt(v.x * v.x + v.z * v.z);

        // Light residual only — main work is the mixin
        if (horiz > 0.25 && ht >= 5) {
            mc.player.setVelocity(v.x * Math.min(1.0, hx + 0.15), v.y * hy, v.z * Math.min(1.0, hx + 0.15));
        }
    }

    public static double horizontalFactor() {
        return Math.max(0.20, Math.min(0.95, ClientSettings.velocityHorizontal));
    }

    public static double verticalFactor() {
        return Math.max(0.65, Math.min(1.0, ClientSettings.velocityVertical));
    }
}
