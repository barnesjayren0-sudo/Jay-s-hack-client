package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.util.Humanizer;
import net.minecraft.item.BlockItem;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

/** Simple under-feet place when airborne / edge. Keep legit-ish delays. */
public class Scaffold extends Module {

    private long last;

    public Scaffold() {
        super("Scaffold", "Place blocks under feet", Category.WORLD);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (mc.currentScreen != null) return;
        if (!(mc.player.getMainHandStack().getItem() instanceof BlockItem)) return;

        long now = System.currentTimeMillis();
        if (now - last < Humanizer.delay(50, 15, 40, 90)) return;

        BlockPos below = mc.player.getBlockPos().down();
        if (!mc.world.getBlockState(below).isAir()) return;

        // find placeable neighbor
        for (Direction dir : Direction.values()) {
            BlockPos neighbor = below.offset(dir);
            if (mc.world.getBlockState(neighbor).isAir()) continue;
            Direction face = dir.getOpposite();
            Vec3d hit = Vec3d.ofCenter(neighbor).add(
                    face.getOffsetX() * 0.5, face.getOffsetY() * 0.5, face.getOffsetZ() * 0.5);
            BlockHitResult bhr = new BlockHitResult(hit, face, neighbor, false);
            try {
                mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, bhr);
                mc.player.swingHand(Hand.MAIN_HAND);
                last = now;
                return;
            } catch (Exception ignored) {}
        }
    }
}
