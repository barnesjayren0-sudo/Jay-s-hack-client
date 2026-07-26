package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import net.minecraft.client.MinecraftClient;

public class AutoSprint extends Module {

    private final MinecraftClient mc = MinecraftClient.getInstance();

    public AutoSprint() {
        super("AutoSprint", "Automatically sprints for better sword combos", Category.MOVEMENT);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;

        if (mc.player.forwardSpeed > 0 && !mc.player.isSprinting() && !mc.player.isUsingItem()) {
            mc.player.setSprinting(true);
        }
    }
}
