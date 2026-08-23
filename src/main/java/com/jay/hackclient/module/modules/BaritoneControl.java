package com.jay.hackclient.module.modules;

import com.jay.hackclient.compat.BaritoneCompat;
import com.jay.hackclient.module.Module;
import net.minecraft.text.Text;

/**
 * WORLD → Baritone. Enable to confirm link; disable cancels path.
 */
public class BaritoneControl extends Module {

    private boolean warnedMissing;

    public BaritoneControl() {
        super("Baritone", "JayBaritone — path with #goto / #mine (needs jar)", Category.WORLD);
    }

    @Override
    public void onEnable() {
        BaritoneCompat.resetDetection();
        if (!BaritoneCompat.isPresent()) {
            msg("§cMissing §fbaritone-fabric-1.21.11.jar §cin mods/");
            warnedMissing = true;
            setEnabled(false);
            return;
        }
        msg("§aLinked §7· §f#goto §8| §f#mine §8| §f#stop");
    }

    @Override
    public void onDisable() {
        if (BaritoneCompat.isPresent()) {
            BaritoneCompat.cancel();
        }
    }

    @Override
    public void onTick() {
        if (!isEnabled()) return;
        if (!BaritoneCompat.isPresent()) {
            if (!warnedMissing) {
                msg("§cBaritone unloaded");
                warnedMissing = true;
            }
            setEnabled(false);
        }
    }

    private void msg(String s) {
        if (mc.player != null) {
            mc.player.sendMessage(Text.literal("§8[§bJay§8] " + s), false);
        }
    }
}
