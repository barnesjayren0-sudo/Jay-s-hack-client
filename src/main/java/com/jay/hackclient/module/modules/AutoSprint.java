package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import net.minecraft.entity.effect.StatusEffects;
import org.lwjgl.glfw.GLFW;

public class AutoSprint extends Module {

    public AutoSprint() {
        super("AutoSprint", "Keeps sprint on", Category.MOVEMENT);
        setKeyBind(GLFW.GLFW_KEY_G);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        if (mc.player.forwardSpeed > 0
                && !mc.player.isSprinting()
                && !mc.player.isUsingItem()
                && !mc.player.hasStatusEffect(StatusEffects.BLINDNESS)
                && mc.player.getHungerManager().getFoodLevel() > 6) {
            mc.player.setSprinting(true);
        }
    }
}
