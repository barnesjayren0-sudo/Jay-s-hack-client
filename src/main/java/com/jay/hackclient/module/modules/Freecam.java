package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.NumberSetting;
import net.minecraft.client.option.GameOptions;
import net.minecraft.util.PlayerInput;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

/**
 * Detached free camera — body stays put; camera flies with WASD + Space/Shift.
 * Default bind: F11.
 */
public class Freecam extends Module {

    public static boolean active;
    public static double x, y, z;
    public static float yaw, pitch;

    private double startX, startY, startZ;

    public final NumberSetting speed = new NumberSetting("Speed", "Fly speed", 1.2, 0.2, 5.0, 0.1);

    public Freecam() {
        super("Freecam", "Fly camera without moving your body", Category.RENDER);
        setKeyBind(GLFW.GLFW_KEY_F11);
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

        // Freeze body (client-side)
        mc.player.setVelocity(Vec3d.ZERO);
        mc.player.setPosition(startX, startY, startZ);

        // 1.21.11: Input uses PlayerInput record, not movementForward fields
        try {
            if (mc.player.input != null) {
                mc.player.input.playerInput = PlayerInput.DEFAULT;
            }
        } catch (Throwable ignored) {}

        yaw = mc.player.getYaw();
        pitch = mc.player.getPitch();

        GameOptions opt = mc.options;
        float spd = speed.getFloat();
        if (opt.sprintKey.isPressed()) spd *= 2.0f;

        float yawRad = yaw * ((float) Math.PI / 180f);
        double forward = 0, strafe = 0, up = 0;

        if (opt.forwardKey.isPressed()) forward += 1;
        if (opt.backKey.isPressed()) forward -= 1;
        if (opt.leftKey.isPressed()) strafe += 1;
        if (opt.rightKey.isPressed()) strafe -= 1;
        if (opt.jumpKey.isPressed()) up += 1;
        if (opt.sneakKey.isPressed()) up -= 1;

        if (forward == 0 && strafe == 0 && up == 0) return;

        double sin = MathHelper.sin(yawRad);
        double cos = MathHelper.cos(yawRad);

        x += (strafe * cos - forward * sin) * spd;
        z += (forward * cos + strafe * sin) * spd;
        y += up * spd;
    }
}
