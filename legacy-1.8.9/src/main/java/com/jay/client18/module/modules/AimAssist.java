package com.jay.client18.module.modules;

import com.jay.client18.module.Module;
import com.jay.client18.util.CombatUtil;
import com.jay.client18.util.RotationUtil;
import net.minecraft.entity.player.EntityPlayer;
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

public class AimAssist extends Module {

    public float fov = 60f;
    public float smooth = 0.22f;
    public double range = 4.2;
    public boolean weaponsOnly = true;
    public boolean requireClick = true;

    public AimAssist() {
        super("AimAssist", "Soft FOV aim", Category.COMBAT);
        setKeyBind(Keyboard.KEY_J);
    }

    @Override
    public void onTick() {
        if (mc.thePlayer == null || mc.theWorld == null) return;
        if (mc.currentScreen != null) return;
        if (weaponsOnly && !CombatUtil.isWeapon(mc.thePlayer.getHeldItem())) return;
        if (requireClick && !Mouse.isButtonDown(0)) return;

        EntityPlayer t = CombatUtil.findTarget(range, fov);
        if (t == null) return;
        if (RotationUtil.yawDiff(t) > fov) return;

        RotationUtil.softLook(t, smooth);
    }
}
