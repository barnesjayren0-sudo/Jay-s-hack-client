package com.jay.client18.module.modules;

import com.jay.client18.module.Module;
import com.jay.client18.util.CombatUtil;
import com.jay.client18.util.Humanizer;
import com.jay.client18.util.RotationUtil;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

/** Ghost AimAssist — soft FOV only, requires hold click by default. */
public class AimAssist extends Module {

    public float fov = 45f;
    public float smooth = 0.14f;
    public double range = 3.6;
    public boolean weaponsOnly = true;
    public boolean requireClick = true;

    private int tick;

    public AimAssist() {
        super("AimAssist", "Silent soft FOV aim", Category.COMBAT);
        setKeyBind(Keyboard.KEY_J);
    }

    @Override
    public void onTick() {
        if (mc.thePlayer == null || mc.theWorld == null) return;
        if (mc.currentScreen != null) return;
        if (weaponsOnly && !CombatUtil.isWeapon(mc.thePlayer.getHeldItem())) return;
        if (requireClick && !Mouse.isButtonDown(0)) return;
        if (Humanizer.shouldSkipTick()) return;

        tick++;
        // Only adjust every other tick — less obvious camera work
        if ((tick & 1) != 0) return;

        EntityPlayer t = CombatUtil.findTarget(range, fov);
        if (t == null) return;

        float diff = RotationUtil.yawDiff(t);
        if (diff > fov) return;
        // Don't pull hard when already close on target
        if (diff < 2.5f && Humanizer.chance(40)) return;

        float s = smooth;
        if (diff < 10f) s *= 0.65f;
        RotationUtil.softLook(t, s);
    }
}
