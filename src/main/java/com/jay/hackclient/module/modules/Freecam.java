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

        x = startX;
        y = startY + mc.player.getStandingEyeHeight();
        z = startZ;
        yaw = mc.player.getYaw();
        pitch = mc.player.getPitch();
        active = true;
    }

    @Override
    public void onDisable() {
        active = false;
    }

    @Override
    public void onTick() {
        if (!active || mc.player == null || mc.world == null) return;

        // Freeze body at enable position; mouse still aims freecam via player look
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

        // Stop vanilla walking while freecam is on
        try {
            mc.player.input.movementForward = 0;
            mc.player.input.movementSideways = 0;
            mc.player.input.jumping = false;
            mc.player.input.sneaking = false;
        } catch (Throwable ignored) {}

        if (forward == 0 && strafe == 0 && up == 0) return;

        double sin = MathHelper.sin(yawRad);
        double cos = MathHelper.cos(yawRad);

        x += (strafe * cos - forward * sin) * spd;
        z += (forward * cos + strafe * sin) * spd;
        y += up * spd;
    }
}
