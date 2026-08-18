package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import net.minecraft.entity.player.PlayerEntity;
import org.lwjgl.glfw.GLFW;

public class ESP extends Module {

    public ESP() {
        super("ESP", "Glow players", Category.RENDER);
        setKeyBind(GLFW.GLFW_KEY_X);
    }

    @Override
    public void onTick() {
        if (mc.world == null || mc.player == null) return;
        for (PlayerEntity p : mc.world.getPlayers()) {
            if (p == mc.player) continue;
            if (AntiBot.isBot(p)) {
                p.setGlowing(false);
                continue;
            }
            p.setGlowing(true);
        }
    }

    @Override
    public void onDisable() {
        if (mc.world == null) return;
        for (PlayerEntity p : mc.world.getPlayers()) {
            if (p != mc.player) p.setGlowing(false);
        }
    }
}
