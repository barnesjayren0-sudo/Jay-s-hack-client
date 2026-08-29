package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.NumberSetting;
import net.minecraft.util.math.Vec3d;
import org.lwjgl.glfw.GLFW;

/**
 * Instant vertical clip by a set amount (up on enable).
 * Toggle again to clip down the same amount.
 */
public class YClip extends Module {

    public final NumberSetting amount = new NumberSetting("Amount", "Blocks to clip", 3.0, 1.0, 20.0, 1.0);
    private boolean up = true;

    public YClip() {
        super("YClip", "Vertical clip up/down", Category.ANARCHY);
        setKeyBind(GLFW.GLFW_KEY_V);
        addSetting(amount);
    }

    @Override
    public void onEnable() {
        if (mc.player == null) {
            setEnabled(false);
            return;
        }
        double dy = up ? amount.getFloat() : -amount.getFloat();
        mc.player.setPosition(mc.player.getX(), mc.player.getY() + dy, mc.player.getZ());
        mc.player.setVelocity(Vec3d.ZERO);
        up = !up;
        // one-shot
        setEnabled(false);
    }
}
