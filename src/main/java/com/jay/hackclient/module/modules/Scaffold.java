package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.BoolSetting;
import com.jay.hackclient.module.setting.ModeSetting;
import com.jay.hackclient.module.setting.NumberSetting;
import com.jay.hackclient.util.Humanizer;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

/** Scaffold — Normal / Telly / Godbridge / Tower with place-fail retry. */
public class Scaffold extends Module {

    public final ModeSetting mode = new ModeSetting("Mode", "Place style",
            "Telly", "Normal", "Telly", "Godbridge", "Tower");
    public final NumberSetting delay = new NumberSetting("Delay", "Base place ms", 45, 25, 120, 5);
    public final BoolSetting autoJump = new BoolSetting("AutoJump", "Jump when grounded (Telly)", true);
    public final NumberSetting airTicks = new NumberSetting("AirTicks", "Min air ticks before place", 2, 0, 8, 1);
    public final BoolSetting rotate = new BoolSetting("Rotate", "Pitch down when placing", true);
    public final BoolSetting sprint = new BoolSetting("Sprint", "Keep sprint on Telly", true);
    public final BoolSetting retryFail = new BoolSetting("Retry", "Faster retry after failed place", true);

    private long lastPlace;
    private int ticksInAir;
    private float savedPitch = Float.NaN;
    private int failStreak;

    public Scaffold() {
        super("Scaffold", "Telly / Normal / Godbridge / Tower", Category.WORLD);
        addSetting(mode);
        addSetting(delay);
        addSetting(autoJump);
        addSetting(airTicks);
        addSetting(rotate);
        addSetting(sprint);
        addSetting(retryFail);
    }

    @Override
    public void onDisable() {
        restorePitch();
        ticksInAir = 0;
        failStreak = 0;
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (mc.currentScreen != null) return;
        if (!holdingBlock()) return;

        String m = mode.get();

        if (mc.player.isOnGround()) {
            ticksInAir = 0;
            restorePitch();
            if ("Telly".equals(m)) tellyGround();
        } else {
            ticksInAir++;
        }

        long now = System.currentTimeMillis();
        int baseDelay = delay.getInt();
        if (retryFail.get() && failStreak > 0) {
            baseDelay = Math.max(25, baseDelay - 12 * Math.min(failStreak, 3));
        }

        if ("Tower".equals(m)) {
            if (now - lastPlace < Humanizer.delay(Math.max(30, baseDelay - 10), 8, 25, baseDelay + 20)) return;
            if (mc.options.jumpKey.isPressed() || !mc.player.isOnGround()) {
                if (mc.player.isOnGround()) mc.player.jump();
                BlockPos under = mc.player.getBlockPos().down();
                if (tryPlace(under)) {
                    lastPlace = now;
                    failStreak = 0;
                    if (rotate.get()) aimDown();
                } else {
                    failStreak++;
                }
            }
            return;
        }

        if ("Telly".equals(m)) {
            if (sprint.get() && mc.options.forwardKey.isPressed()) mc.player.setSprinting(true);
            if (ticksInAir < airTicks.getInt()) return;
            if (now - lastPlace < Humanizer.delay(baseDelay, 10, 30, baseDelay + 40)) return;
            BlockPos target = mc.player.getBlockPos().down();
            if (!mc.world.getBlockState(target).isReplaceable()) {
                target = aheadDown();
            }
            if (tryPlace(target)) {
                lastPlace = now;
                failStreak = 0;
            } else if (tryPlace(aheadDown())) {
                lastPlace = now;
                failStreak = 0;
            } else {
                failStreak++;
            }
            return;
        }

        if ("Godbridge".equals(m)) {
            if (now - lastPlace < Humanizer.delay(baseDelay + 15, 10, 25, baseDelay + 50)) return;
            if (rotate.get()) aimDown();
            BlockPos under = mc.player.getBlockPos().down();
            BlockPos ahead = aheadDown();
            if (tryPlace(under) || tryPlace(ahead)) {
                lastPlace = now;
                failStreak = 0;
            } else failStreak++;
            return;
        }

        // Normal
        if (now - lastPlace < Humanizer.delay(baseDelay, 10, 30, baseDelay + 35)) return;
        BlockPos under = mc.player.getBlockPos().down();
        if (tryPlace(under)) {
            lastPlace = now;
            failStreak = 0;
        } else if (tryPlace(aheadDown())) {
            lastPlace = now;
            failStreak = 0;
        } else {
            failStreak++;
        }
    }

    private void tellyGround() {
        if (!autoJump.get()) return;
        if (mc.options.forwardKey.isPressed()) {
            boolean edge = mc.world.getBlockState(aheadDown()).isReplaceable()
                    || mc.world.getBlockState(mc.player.getBlockPos().down()).isReplaceable();
            if (edge || mc.player.forwardSpeed > 0.08f) {
                mc.player.jump();
            }
        }
    }

    private BlockPos aheadDown() {
        return mc.player.getBlockPos().offset(mc.player.getHorizontalFacing()).down();
    }

    private boolean tryPlace(BlockPos target) {
        if (target == null) return false;
        if (!mc.world.getBlockState(target).isReplaceable()) return false;

        Direction[] order = {
                Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST,
                Direction.UP, Direction.DOWN
        };
        for (Direction dir : order) {
            BlockPos neighbor = target.offset(dir);
            if (mc.world.getBlockState(neighbor).isAir()) continue;
            if (mc.world.getBlockState(neighbor).isReplaceable()) continue;

            if (rotate.get() && ("Telly".equals(mode.get()) || "Tower".equals(mode.get()))) {
                aimDown();
            }
            if (placeAgainst(neighbor, dir.getOpposite())) return true;
        }
        return false;
    }

    private void aimDown() {
        if (Float.isNaN(savedPitch)) savedPitch = mc.player.getPitch();
        float target = 75f + (float) (Math.random() * 6.0);
        float cur = mc.player.getPitch();
        mc.player.setPitch(cur + (target - cur) * 0.45f);
    }

    private void restorePitch() {
        if (!Float.isNaN(savedPitch) && mc.player != null) {
            mc.player.setPitch(savedPitch);
            savedPitch = Float.NaN;
        }
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
