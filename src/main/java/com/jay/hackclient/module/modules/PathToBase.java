package com.jay.hackclient.module.modules;

import com.jay.hackclient.compat.BaritoneCompat;
import com.jay.hackclient.module.Module;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

/**
 * Baritone path to last BaseFinder / StorageFinder hit, or set coords via .jay goto x y z
 */
public class PathToBase extends Module {

    public static BlockPos lastTarget = null;

    public PathToBase() {
        super("PathToBase", "Baritone → last find / coords", Category.WORLD);
    }

    @Override
    public void onEnable() {
        runPath(lastTarget);
        setEnabled(false);
    }

    public static boolean runPath(BlockPos target) {
        var mc = net.minecraft.client.MinecraftClient.getInstance();
        if (mc.player == null) return false;

        if (!BaritoneCompat.isPresent()) {
            mc.player.sendMessage(Text.literal(
                    "§8[§bPath§8] §cBaritone not installed — drop jar in mods/"), false);
            return false;
        }
        if (target == null) {
            mc.player.sendMessage(Text.literal(
                    "§8[§bPath§8] §eNo target — scan first or .jay goto x y z"), false);
            return false;
        }

        lastTarget = target;
        boolean ok = BaritoneCompat.pathTo(target);
        mc.player.sendMessage(Text.literal(ok
                ? "§8[§bPath§8] §aPathing to §f" + target.getX() + " " + target.getY() + " " + target.getZ()
                : "§8[§bPath§8] §cPath failed · " + BaritoneCompat.lastError()), false);
        return ok;
    }

    public static boolean runPath(int x, int y, int z) {
        return runPath(new BlockPos(x, y, z));
    }
}
