package com.jay.hackclient.module.modules;

import com.jay.hackclient.compat.BaritoneCompat;
import com.jay.hackclient.module.Module;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

/** Path to last finder target via Baritone. */
public class PathToBase extends Module {

    public static BlockPos lastTarget;

    public PathToBase() {
        super("PathToBase", "Baritone path to last scan hit", Category.WORLD);
    }

    @Override
    public void onEnable() {
        runPath(lastTarget);
        setEnabled(false);
    }

    public static void runPath(BlockPos target) {
        if (target == null) {
            msg("§cNo target — run a finder first");
            return;
        }
        lastTarget = target;
        boolean ok = BaritoneCompat.pathTo(target);
        if (ok) {
            msg("§aPath → " + target.getX() + " " + target.getY() + " " + target.getZ());
        } else if (!BaritoneCompat.isPresent()) {
            msg("§cBaritone not installed");
        } else {
            msg("§cPath failed " + BaritoneCompat.lastError());
        }
    }

    public static void runPath(int x, int y, int z) {
        runPath(new BlockPos(x, y, z));
    }

    private static void msg(String s) {
        var mc = net.minecraft.client.MinecraftClient.getInstance();
        if (mc.player != null) {
            mc.player.sendMessage(Text.literal("§8[§bJay§8] " + s), false);
        }
    }
}
