package com.jay.hackclient.module.modules;

import com.jay.hackclient.compat.BaritoneCompat;
import com.jay.hackclient.module.Module;
import net.minecraft.text.Text;

/**
 * Toggle = cancel Baritone path when disabled.
 * Enable with commands: #goto / .b goto / .jay baritone
 */
public class BaritoneControl extends Module {

    public BaritoneControl() {
        super("Baritone", "JayBaritone control — needs Baritone jar in mods", Category.WORLD);
    }

    @Override
    public void onEnable() {
        if (!BaritoneCompat.isPresent()) {
            msg("§cInstall baritone-fabric-1.21.11.jar in mods/");
            setEnabled(false);
            return;
        }
        msg("§aBaritone linked · §f#goto #mine #stop  or  .b help");
    }

    @Override
    public void onDisable() {
        BaritoneCompat.cancel();
    }

    @Override
    public void onTick() {
        // pathing runs inside Baritone; module just tracks presence
        if (!BaritoneCompat.isPresent() && isEnabled()) {
            setEnabled(false);
        }
    }

    private void msg(String s) {
        if (mc.player != null) {
            mc.player.sendMessage(Text.literal("§8[§bJay§8] " + s), false);
        }
    }
}
