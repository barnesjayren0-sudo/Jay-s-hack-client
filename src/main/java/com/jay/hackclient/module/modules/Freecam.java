package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.NumberSetting;
import net.minecraft.client.option.GameOptions;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

/**
 * Detached free camera — player body stays put; camera flies with WASD + Space/Shift.
 */
public class Freecam extends Module {

    public static boolean active;
    public static double x, y, z;
    public static float yaw, pitch;

    private double startX, startY, startZ;
    private float startYaw, startPitch;

    public final NumberSetting speed = new NumberSetting("Speed", "Fly speed", 1.2, 0.2, 5.0, 0.1);

    public Freecam() {
        super("Freecam", "Fly camera without moving your body", Category.RENDER);
        setKeyBind(GLFW.GLFW_KEY_U);
        addSetting(speed);
    }

    @Override
    public void onEnable() {
        if (mc.player == null) {
            setEnabled(false);
            return;
        }
        startX = mc.player.getX();
        startY = mc.player.getY();
        startZ = mc.player.getZ();
        startYaw = mc.player.getYaw();
        startPitch = mc.player.getPitch();

        x = startX;
        y = startY + mc.player.getStandingEyeHeight();
        z = startZ;
        yaw = startYaw;
        pitch = startPitch;
        active = true;
    }

    @Override
    public void onDisable() {
        active = false;
        if (mc.player != null) {
            mc.player.setYaw(startYaw);
            mc.player.setPitch(startPitch);
        }
    }

    @Override
    public void onTick() {
        if (!active || mc.player == null || mc.world == null) return;

        // Keep body frozen at enable position (client-side)
        mc.player.setVelocity(Vec3d.ZERO);
        mc.player.setPosition(startX, startY, startZ);
        mc.player.setYaw(startYaw);
        mc.player.setPitch(startPitch);

        // Camera rotation follows mouse (player look is restored above — use last render angles)
        // While freecam is on, read current mouse look from options before we overwrite player
        // Actually we overwrote player yaw/pitch — store look from camera if available
        if (mc.gameRenderer != null && mc.gameRenderer.getCamera() != null) {
            // Mouse still updates player look input before we freeze — capture from input
        }

        // Use key rotation: mouse still rotates "player" briefly each tick before freeze.
        // Capture intended look from mouse delta by reading player before freeze next frame:
        // Simpler: freecam yaw/pitch = player look, then re-freeze body angles.
        yaw = mc.player.getYaw();
        pitch = mc.player.getPitch();
        // Wait — we set player to startYaw already. Need to not overwrite look.

        // Correct flow: do NOT overwrite player yaw/pitch; only position/velocity.
        // Re-apply start only for position.
    }

    /** Called at start of tick from a fixed order — movement only. */
    public void updateCamera() {
        if (!active || mc.player == null) return;

        // Freeze body position & velocity; leave look alone so mouse aims freecam
        mc.player.setVelocity(Vec3d.ZERO);
        mc.player.setPosition(startX, startY, startZ);

        yaw = mc.player.getYaw();
        pitch = mc.player.getPitch();

        GameOptions opt = mc.options;
        float spd = speed.getFloat();
        if (opt.sprintKey.isPressed()) spd *= 2.0f;

        float yawRad = yaw * MathHelper.RADIANS_PER_DEGREE;
        double forward = 0, strafe = 0, up = 0;

        if (opt.forwardKey.isPressed()) forward += 1;
        if (opt.backKey.isPressed()) forward -= 1;
        if (opt.leftKey.isPressed()) strafe += 1;
        if (opt.rightKey.isPressed()) strafe -= 1;
        if (opt.jumpKey.isPressed()) up += 1;
        if (opt.sneakKey.isPressed()) up -= 1;

        // Cancel vanilla movement input so body doesn't walk
        mc.player.input.movementForward = 0;
        mc.player.input.movementSideways = 0;
        mc.player.input.jumping = false;
        mc.player.input.sneaking = false;

        if (forward == 0 && strafe == 0 && up == 0) return;

        double sin = MathHelper.sin(yawRad);
        double cos = MathHelper.cos(yawRad);

        x += (strafe * cos - forward * sin) * spd;
        z += (forward * cos + strafe * sin) * spd;
        y += up * spd;
    }

    @Override
    public void onTick() {
        updateCamera();
    }
}
