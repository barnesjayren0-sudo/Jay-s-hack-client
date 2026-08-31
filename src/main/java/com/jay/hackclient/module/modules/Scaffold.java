package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.BoolSetting;
import com.jay.hackclient.module.setting.ModeSetting;
import com.jay.hackclient.module.setting.NumberSetting;
import com.jay.hackclient.util.Humanizer;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ActionResult;
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
    public final BoolSetting rotate = new BoolSetting("Rotate", "Face block when placing", true);
    public final BoolSetting sprint = new BoolSetting("Sprint", "Keep sprint on Telly", true);
    public final BoolSetting retryFail = new BoolSetting("Retry", "Faster retry after failed place", true);
    public final BoolSetting safeEdge = new BoolSetting("SafeEdge", "Extra edge check before place", true);

    private long lastPlace;
    private int ticksInAir;
    private int failStreak;
    private float savedPitch = 0;

    public Scaffold() {
        super("Scaffold", "Telly / Normal / Godbridge / Tower", Category.WORLD);
        addSetting(mode);
        addSetting(delay);
        addSetting(autoJump);
        addSetting(airTicks);
        addSetting(rotate);
        addSetting(sprint);
        addSetting(retryFail);
        addSetting(safeEdge);
    }

    @Override
    public void onEnable() {
        ticksInAir = 0;
        failStreak = 0;
        lastPlace = 0;
    }

    @Override
    public void onDisable() {
        restorePitch();
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (!hasBlocks()) return;

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
                } else {
                    failStreak++;
                }
            }
            return;
        }

        if ("Telly".equals(m)) {
            if (ticksInAir < airTicks.getInt()) return;
            if (now - lastPlace < Humanizer.delay(baseDelay, 10, 30, baseDelay + 35)) return;
            BlockPos target = mc.player.getBlockPos().down();
            if (!mc.world.getBlockState(target).isReplaceable()) {
                // extend forward when standing on placed block mid-bridge
                target = aheadDown();
            }
            if (safeEdge.get() && mc.player.isOnGround()) return;
            if (tryPlace(target)) {
                lastPlace = now;
                failStreak = 0;
                if (sprint.get() && mc.options.forwardKey.isPressed()) mc.player.setSprinting(true);
            } else {
                failStreak++;
                // one recovery attempt on adjacent edge
                if (tryPlace(aheadDown())) {
                    lastPlace = now;
                    failStreak = 0;
                }
            }
            return;
        }

        if ("Godbridge".equals(m)) {
            if (now - lastPlace < Humanizer.delay(baseDelay, 8, 20, baseDelay + 25)) return;
            savedPitch = mc.player.getPitch();
            mc.player.setPitch(75f);
            BlockPos target = aheadDown();
            if (tryPlace(target) || tryPlace(mc.player.getBlockPos().down())) {
                lastPlace = now;
                failStreak = 0;
            } else failStreak++;
            return;
        }

        // Normal
        if (now - lastPlace < Humanizer.delay(baseDelay, 10, 28, baseDelay + 40)) return;
        BlockPos under = mc.player.getBlockPos().down();
        if (mc.world.getBlockState(under).isReplaceable()) {
            if (tryPlace(under)) {
                lastPlace = now;
                failStreak = 0;
            } else failStreak++;
        } else if (safeEdge.get()) {
            boolean edge = mc.world.getBlockState(aheadDown()).isReplaceable()
                    || mc.world.getBlockState(under).isReplaceable();
            if (edge && tryPlace(aheadDown())) {
                lastPlace = now;
                failStreak = 0;
            }
        }
    }

    private void tellyGround() {
        if (autoJump.get() && mc.options.forwardKey.isPressed() && mc.player.isOnGround()) {
            boolean edge = mc.world.getBlockState(aheadDown()).isReplaceable();
            if (edge) mc.player.jump();
        }
    }

    private BlockPos aheadDown() {
        float yaw = mc.player.getYaw();
        double rad = Math.toRadians(yaw);
        int fx = (int) Math.round(-Math.sin(rad));
        int fz = (int) Math.round(Math.cos(rad));
        return mc.player.getBlockPos().add(fx, -1, fz);
    }

    private boolean tryPlace(BlockPos target) {
        if (target == null || mc.world == null) return false;
        if (!mc.world.getBlockState(target).isReplaceable()) return false;
        selectBlockSlot();

        for (Direction dir : Direction.values()) {
            BlockPos neighbor = target.offset(dir);
            if (mc.world.getBlockState(neighbor).isReplaceable()) continue;
            if (rotate.get() && ("Telly".equals(mode.get()) || "Tower".equals(mode.get()))) {
                lookAt(neighbor);
            }
            if (placeAgainst(neighbor, dir.getOpposite())) return true;
        }
        return false;
    }

    private void lookAt(BlockPos pos) {
        Vec3d eyes = mc.player.getEyePos();
        Vec3d center = Vec3d.ofCenter(pos);
        Vec3d d = center.subtract(eyes);
        double dist = Math.sqrt(d.x * d.x + d.z * d.z);
        float yaw = (float) (Math.toDegrees(Math.atan2(d.z, d.x)) - 90.0);
        float pitch = (float) (-Math.toDegrees(Math.atan2(d.y, dist)));
        mc.player.setYaw(yaw);
        mc.player.setPitch(pitch);
    }

    private boolean placeAgainst(BlockPos neighbor, Direction face) {
        Vec3d hit = Vec3d.ofCenter(neighbor).add(Vec3d.of(face.getVector()).multiply(0.5));
        BlockHitResult bhr = new BlockHitResult(hit, face, neighbor, false);
        ActionResult r = mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, bhr);
        if (r.isAccepted()) {
            mc.player.swingHand(Hand.MAIN_HAND);
            return true;
        }
        return false;
    }

    private boolean hasBlocks() {
        ItemStack s = mc.player.getMainHandStack();
        if (s.getItem() instanceof BlockItem) return true;
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() instanceof BlockItem) return true;
        }
        return false;
    }

    private void selectBlockSlot() {
        if (mc.player.getMainHandStack().getItem() instanceof BlockItem) return;
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).getItem() instanceof BlockItem) {
                mc.player.getInventory().setSelectedSlot(i);
                return;
            }
        }
    }

    private void restorePitch() {
        // no-op soft restore — avoid fighting other rotation owners
    }
}
