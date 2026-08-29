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
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

/**
 * Scaffold — Normal / Telly / Godbridge.
 *
 * Telly: sprint + auto-jump, place only while airborne (classic telly bridge).
 */
public class Scaffold extends Module {

    public final ModeSetting mode = new ModeSetting("Mode", "Place style",
            "Telly", "Normal", "Telly", "Godbridge");
    public final NumberSetting delay = new NumberSetting("Delay", "Base place ms", 45, 25, 120, 5);
    public final BoolSetting autoJump = new BoolSetting("AutoJump", "Jump when grounded (Telly)", true);
    public final NumberSetting airTicks = new NumberSetting("AirTicks", "Min air ticks before place", 2, 0, 8, 1);
    public final BoolSetting rotate = new BoolSetting("Rotate", "Pitch down when placing", true);
    public final BoolSetting sprint = new BoolSetting("Sprint", "Keep sprint on Telly", true);

    private long lastPlace;
    private int ticksInAir;
    private float savedPitch = Float.NaN;

    public Scaffold() {
        super("Scaffold", "Telly / Normal / Godbridge scaffold", Category.WORLD);
        addSetting(mode);
        addSetting(delay);
        addSetting(autoJump);
        addSetting(airTicks);
        addSetting(rotate);
        addSetting(sprint);
    }

    @Override
    public void onDisable() {
        restorePitch();
        ticksInAir = 0;
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
            if ("Telly".equals(m)) {
                tellyGround();
            }
        } else {
            ticksInAir++;
        }

        long now = System.currentTimeMillis();
        int baseDelay = delay.getInt();

        if ("Telly".equals(m)) {
            // Only place in air after a few ticks (the telly gap)
            if (mc.player.isOnGround()) return;
            if (ticksInAir < airTicks.getInt()) return;
            baseDelay = Math.max(30, baseDelay - 5);
        } else if ("Godbridge".equals(m)) {
            baseDelay = Math.max(30, baseDelay - 10);
        }

        if (now - lastPlace < Humanizer.delay(baseDelay, 10, Math.max(25, baseDelay - 12), baseDelay + 35)) {
            return;
        }

        BlockPos below = mc.player.getBlockPos().down();

        // Telly: also try one block ahead under trajectory
        if ("Telly".equals(m)) {
            if (tryPlace(below) || tryPlace(aheadDown())) {
                lastPlace = now;
                return;
            }
        } else if ("Godbridge".equals(m)) {
            Direction back = mc.player.getHorizontalFacing().getOpposite();
            BlockPos neighbor = below.offset(back);
            if (!mc.world.getBlockState(neighbor).isAir()) {
                if (placeAgainst(neighbor, back.getOpposite())) {
                    lastPlace = now;
                    if (rotate.get()) {
                        mc.player.setPitch(MathHelper.clamp(mc.player.getPitch(), 75f, 86f));
                    }
                    return;
                }
            }
            if (tryPlace(below)) {
                lastPlace = now;
                return;
            }
        } else {
            // Normal — place under feet whenever air
            if (tryPlace(below)) {
                lastPlace = now;
            }
        }
    }

    private void tellyGround() {
        if (mc.options == null) return;

        boolean forward = mc.options.forwardKey.isPressed();
        if (!forward) return;

        if (sprint.get() && !mc.player.isSprinting() && mc.player.getHungerManager().getFoodLevel() > 6) {
            mc.player.setSprinting(true);
        }

        // Auto-jump for telly rhythm
        if (autoJump.get() && mc.player.isOnGround()) {
            // Edge: about to walk into air OR always jump while holding forward
            BlockPos frontDown = mc.player.getBlockPos()
                    .offset(mc.player.getHorizontalFacing()).down();
            boolean edge = mc.world.getBlockState(frontDown).isAir()
                    || mc.world.getBlockState(mc.player.getBlockPos().down()).isAir();
            if (edge || mc.player.forwardSpeed > 0.08f) {
                mc.player.jump();
            }
        }
    }

    private BlockPos aheadDown() {
        Direction face = mc.player.getHorizontalFacing();
        return mc.player.getBlockPos().offset(face).down();
    }

    private boolean tryPlace(BlockPos target) {
        if (target == null) return false;
        if (!mc.world.getBlockState(target).isReplaceable()) return false;

        // Prefer solid neighbor to click against
        for (Direction dir : Direction.values()) {
            BlockPos neighbor = target.offset(dir);
            if (mc.world.getBlockState(neighbor).isAir()) continue;
            if (mc.world.getBlockState(neighbor).getBlock().getDefaultState().isReplaceable()) continue;

            if (rotate.get() && "Telly".equals(mode.get())) {
                aimDown();
            }
            if (placeAgainst(neighbor, dir.getOpposite())) {
                return true;
            }
        }
        return false;
    }

    private void aimDown() {
        if (Float.isNaN(savedPitch)) {
            savedPitch = mc.player.getPitch();
        }
        // Look down enough to place under feet while moving
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
        Vec3d hit = Vec3d.ofCenter(neighbor).add(
                face.getOffsetX() * 0.5,
                face.getOffsetY() * 0.5,
                face.getOffsetZ() * 0.5
        );
        BlockHitResult bhr = new BlockHitResult(hit, face, neighbor, false);
        try {
            var result = mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, bhr);
            mc.player.swingHand(Hand.MAIN_HAND);
            return result != null && result.isAccepted();
        } catch (Exception e) {
            try {
                mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, bhr);
                mc.player.swingHand(Hand.MAIN_HAND);
                return true;
            } catch (Exception e2) {
                return false;
            }
        }
    }

    private boolean holdingBlock() {
        ItemStack main = mc.player.getMainHandStack();
        if (main.getItem() instanceof BlockItem) return true;
        ItemStack off = mc.player.getOffHandStack();
        return off.getItem() instanceof BlockItem;
    }
}
