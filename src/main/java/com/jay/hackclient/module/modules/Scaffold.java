package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.BoolSetting;
import com.jay.hackclient.module.setting.ModeSetting;
import com.jay.hackclient.module.setting.NumberSetting;
import net.minecraft.item.BlockItem;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

/**
 * Scaffold — Normal / Telly / Godbridge / Tower.
 * Soft pitch restore so SafeWalk never sticks; no forced sneak.
 */
public class Scaffold extends Module {

    public final ModeSetting mode = new ModeSetting("Mode", "Place style",
            "Telly", "Normal", "Telly", "Godbridge", "Tower");
    public final NumberSetting delay = new NumberSetting("Delay", "Base place ms", 45, 25, 120, 5);
    public final BoolSetting autoJump = new BoolSetting("AutoJump", "Jump when grounded (Telly)", true);
    public final NumberSetting airTicks = new NumberSetting("AirTicks", "Min air ticks before place", 2, 0, 8, 1);
    public final BoolSetting rotate = new BoolSetting("Rotate", "Pitch down when placing", true);
    public final BoolSetting sprint = new BoolSetting("Sprint", "Keep sprint on Telly", true);
    public final BoolSetting retryFail = new BoolSetting("Retry", "Faster retry after failed place", true);
    public final BoolSetting onlyWhenForward = new BoolSetting("ForwardOnly", "Only place while moving forward", false);

    private long lastPlace;
    private int ticksInAir;
    private float savedPitch = Float.NaN;
    private int failStreak;
    private int towerHoldTicks;
    private int pitchHoldTicks;

    public Scaffold() {
        super("Scaffold", "Telly / Normal / Godbridge / Tower", Category.WORLD);
        addSetting(mode);
        addSetting(delay);
        addSetting(autoJump);
        addSetting(airTicks);
        addSetting(rotate);
        addSetting(sprint);
        addSetting(retryFail);
        addSetting(onlyWhenForward);
    }

    @Override
    public void onDisable() {
        forceRestorePitch();
        ticksInAir = 0;
        failStreak = 0;
        towerHoldTicks = 0;
        pitchHoldTicks = 0;
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (mc.currentScreen != null) {
            forceRestorePitch();
            return;
        }
        if (!holdingBlock()) {
            forceRestorePitch();
            return;
        }
        if (onlyWhenForward.get() && !mc.options.forwardKey.isPressed()) {
            restorePitchSoft();
            return;
        }

        String m = mode.get();

        if (mc.player.isOnGround()) {
            ticksInAir = 0;
            if (!"Tower".equals(m)) restorePitchSoft();
            if ("Telly".equals(m)) tellyGround();
            if ("Tower".equals(m)) towerGround();
        } else {
            ticksInAir++;
        }

        // Cap how long we hold aim-down
        if (pitchHoldTicks > 0) {
            pitchHoldTicks--;
            if (pitchHoldTicks == 0) restorePitchSoft();
        }

        long now = System.currentTimeMillis();
        long cd = (long) delay.get();
        if (retryFail.get() && failStreak > 0) {
            cd = Math.max(20, cd - failStreak * 8L);
        }
        if (now - lastPlace < cd) return;

        if ("Telly".equals(m) && ticksInAir < airTicks.getInt()) return;
        if ("Godbridge".equals(m) && !mc.player.isOnGround() && ticksInAir < 1) return;

        BlockPos below = BlockPos.ofFloored(mc.player.getX(), mc.player.getY() - 0.05, mc.player.getZ());
        if ("Tower".equals(m)) {
            below = mc.player.getBlockPos().down();
        }

        boolean ok = tryPlace(below);
        if (!ok && ("Telly".equals(m) || "Normal".equals(m))) {
            Vec3d look = mc.player.getRotationVector();
            BlockPos edge = below.add(
                    (int) Math.round(look.x),
                    0,
                    (int) Math.round(look.z)
            );
            ok = tryPlace(edge);
        }

        if (ok) {
            lastPlace = now;
            failStreak = 0;
            pitchHoldTicks = 4; // short hold then soft restore
        } else {
            failStreak = Math.min(5, failStreak + 1);
        }
    }

    private void tellyGround() {
        if (autoJump.get() && mc.options.forwardKey.isPressed() && mc.player.isOnGround()) {
            mc.player.jump();
        }
        if (sprint.get() && mc.options.forwardKey.isPressed()) {
            mc.player.setSprinting(true);
        }
    }

    private void towerGround() {
        if (mc.options.jumpKey.isPressed() || autoJump.get()) {
            if (mc.player.isOnGround()) {
                mc.player.jump();
                towerHoldTicks = 4;
            }
        }
        if (towerHoldTicks > 0) {
            towerHoldTicks--;
            if (mc.player.getVelocity().y < 0.2) {
                mc.player.setVelocity(mc.player.getVelocity().x, 0.42, mc.player.getVelocity().z);
            }
        }
    }

    private boolean tryPlace(BlockPos target) {
        if (target == null) return false;
        if (!mc.world.getBlockState(target).isReplaceable()) return false;

        Direction[] order = {
                Direction.DOWN,
                Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST,
                Direction.UP
        };
        for (Direction dir : order) {
            BlockPos neighbor = target.offset(dir);
            if (mc.world.getBlockState(neighbor).isAir()) continue;
            if (mc.world.getBlockState(neighbor).isReplaceable()) continue;

            if (rotate.get()) aimDown();
            if (placeAgainst(neighbor, dir.getOpposite())) return true;
        }
        return false;
    }

    private void aimDown() {
        if (mc.player == null) return;
        if (Float.isNaN(savedPitch)) savedPitch = mc.player.getPitch();
        float target = 76f + (float) (Math.random() * 4.0);
        float cur = mc.player.getPitch();
        mc.player.setPitch(cur + (target - cur) * 0.28f);
    }

    private void restorePitchSoft() {
        if (Float.isNaN(savedPitch) || mc.player == null) return;
        float cur = mc.player.getPitch();
        float next = cur + (savedPitch - cur) * 0.4f;
        mc.player.setPitch(next);
        if (Math.abs(next - savedPitch) < 1.5f) {
            mc.player.setPitch(savedPitch);
            savedPitch = Float.NaN;
        }
    }

    private void forceRestorePitch() {
        if (!Float.isNaN(savedPitch) && mc.player != null) {
            mc.player.setPitch(savedPitch);
        }
        savedPitch = Float.NaN;
        pitchHoldTicks = 0;
    }

    private boolean placeAgainst(BlockPos neighbor, Direction face) {
        Hand hand = Hand.MAIN_HAND;
        if (!(mc.player.getMainHandStack().getItem() instanceof BlockItem)
                && mc.player.getOffHandStack().getItem() instanceof BlockItem) {
            hand = Hand.OFF_HAND;
        }
        Vec3d hit = Vec3d.ofCenter(neighbor).add(
                face.getOffsetX() * 0.5,
                face.getOffsetY() * 0.5,
                face.getOffsetZ() * 0.5
        );
        BlockHitResult bhr = new BlockHitResult(hit, face, neighbor, false);
        try {
            var result = mc.interactionManager.interactBlock(mc.player, hand, bhr);
            mc.player.swingHand(hand);
            return result != null && result.isAccepted();
        } catch (Exception e) {
            try {
                mc.interactionManager.interactBlock(mc.player, hand, bhr);
                mc.player.swingHand(hand);
                return true;
            } catch (Exception e2) {
                return false;
            }
        }
    }

    private boolean holdingBlock() {
        return mc.player.getMainHandStack().getItem() instanceof BlockItem
                || mc.player.getOffHandStack().getItem() instanceof BlockItem;
    }
}
