package com.jay.hackclient.module.modules;

import com.jay.hackclient.compat.BaritoneCompat;
import com.jay.hackclient.module.Module;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

/**
 * Uses Baritone if installed to path toward last BaseFinder chest-like pos.
 * Without Baritone, prints install hint.
 */
public class PathToBase extends Module {

    public static BlockPos lastTarget = null;

    public PathToBase() {
        super("PathToBase", "Baritone path to last found base (optional)", Category.WORLD);
    }

    @Override
    public void onEnable() {
        if (!BaritoneCompat.isPresent()) {
            if (mc.player != null) {
                mc.player.sendMessage(Text.literal(
                        "§8[§bJay§8] §cBaritone not installed — put Baritone jar in mods"), false);
            }
            setEnabled(false);
            return;
        }
        if (lastTarget == null) {
            if (mc.player != null) {
                mc.player.sendMessage(Text.literal(
                        "§8[§bJay§8] §eNo target — run BaseFinder scan first"), false);
            }
            setEnabled(false);
            return;
        }
        boolean ok = BaritoneCompat.pathTo(lastTarget);
        if (mc.player != null) {
            mc.player.sendMessage(Text.literal(ok
                    ? "§8[§bJay§8] §aPathing to " + lastTarget.toShortString()
                    : "§8[§bJay§8] §cBaritone path failed"), false);
        }
        // one-shot
        setEnabled(false);
    }
}
