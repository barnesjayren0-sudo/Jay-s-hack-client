package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;

public class ESP extends Module {

    public ESP() {
        super("ESP", "Glow outline on other players", Category.RENDER);
    }

    @Override
    public void onTick() {
        if (mc.world == null || mc.player == null) return;

        for (Entity entity : mc.world.getEntities()) {
            if (entity instanceof PlayerEntity player && player != mc.player) {
                player.setGlowing(true);
            }
        }
    }

    @Override
    public void onDisable() {
        if (mc.world == null || mc.player == null) return;
        for (Entity entity : mc.world.getEntities()) {
            if (entity instanceof PlayerEntity player && player != mc.player) {
                player.setGlowing(false);
            }
        }
    }
}
