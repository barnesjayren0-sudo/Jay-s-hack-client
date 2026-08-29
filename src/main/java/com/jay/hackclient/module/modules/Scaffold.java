package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.ModeSetting;
import com.jay.hackclient.module.setting.NumberSetting;
import com.jay.hackclient.util.Humanizer;
import net.minecraft.item.BlockItem;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

/** Scaffold with Normal / Telly / Godbridge modes. */
public class Scaffold extends Module {

    public final ModeSetting mode = new ModeSetting("Mode", "Place style",
            "Normal", "Normal", "Telly", "Godbridge");
    public final NumberSetting delay = new NumberSetting("Delay", "Base ms", 55, 30, 150, 5);

    private long last;
    private int tellyTicks;

    public Scaffold() {
        super("Scaffold", "Blocks under feet — modes", Category.WORLD);
        addSetting(mode);
        addSetting(delay);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (mc.currentScreen != null) return;
        if (!(mc.player.getMainHandStack().getItem() instanceof BlockItem)) return;

        long now = System.currentTimeMillis();
        int base = delay.getInt();
        String m = mode.get();

        if ("Telly".equals(m)) {
            // Walk a bit then place — fewer mid-air spam places
            tellyTicks++;
            if (mc.player.isOnGround()) tellyTicks = 0;
            if (tellyTicks < 3 && mc.player.isOnGround()) return;
            base += 15;
        } else if ("Godbridge".equals(m)) {
            // Tighter delay, prefer placing behind facing
            base = Math.max(35, base - 10);
        }

        if (now - last < Humanizer.delay(base, 12, Math.max(30, base - 15), base + 40)) return;

        BlockPos below = mc.player.getBlockPos().down();
        if (!mc.world.getBlockState(below).isAir()) return;

        if ("Godbridge".equals(m)) {
            Direction face = mc.player.getHorizontalFacing().getOpposite();
            BlockPos neighbor = below.offset(face);
            if (!mc.world.getBlockState(neighbor).isAir()) {
                if (placeAgainst(neighbor, face.getOpposite())) {
                    last = now;
                    return;
                }
            }
        }

        // Normal / Telly: any solid neighbor
        for (Direction dir : Direction.values()) {
            BlockPos neighbor = below.offset(dir);
            if (mc.world.getBlockState(neighbor).isAir()) continue;
            if (placeAgainst(neighbor, dir.getOpposite())) {
                last = now;
                // Soft pitch assist for godbridge feel
                if ("Godbridge".equals(m)) {
                    float pitch = MathHelper.clamp(mc.player.getPitch(), 75f, 85f);
                    mc.player.setPitch(pitch);
                }
                return;
            }
        }
    }

    private boolean placeAgainst(BlockPos neighbor, Direction face) {
        Vec3d hit = Vec3d.ofCenter(neighbor).add(
                face.getOffsetX() * 0.5, face.getOffsetY() * 0.5, face.getOffsetZ() * 0.5);
        BlockHitResult bhr = new BlockHitResult(hit, face, neighbor, false);
        try {
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, bhr);
            mc.player.swingHand(Hand.MAIN_HAND);
            return true;
        } catch (Exception e) {
            return false;
        }
    }
}
