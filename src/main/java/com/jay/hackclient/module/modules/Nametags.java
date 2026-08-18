package com.jay.hackclient.module.modules;

import com.jay.hackclient.JayHackClient;
import com.jay.hackclient.module.Module;
import net.minecraft.entity.player.PlayerEntity;

/** Keeps players glowing + always visible name plate via glow flag. */
public class Nametags extends Module {

    public Nametags() {
        super("Nametags", "Highlight player names / glow", Category.RENDER);
    }

    @Override
    public void onTick() {
        if (mc.world == null || mc.player == null) return;
        for (PlayerEntity p : mc.world.getPlayers()) {
            if (p == mc.player) continue;
            boolean friend = JayHackClient.friendManager != null
                    && JayHackClient.friendManager.isFriend(p.getName().getString());
            p.setGlowing(true);
            // Friends stay glowing too — color differentiation needs render mixin
            if (friend) {
                // no-op visual distinction without mixin
            }
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
