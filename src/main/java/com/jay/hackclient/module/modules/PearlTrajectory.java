package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.BoolSetting;
import com.jay.hackclient.module.setting.NumberSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.item.Items;
import net.minecraft.util.hit.HitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.RaycastContext;

import java.util.ArrayList;
import java.util.List;

/**
 * Pearl trajectory — thin line + landing marker (kit QoL).
 * Rendered from HudRenderer / world overlay when holding pearl.
 */
public class PearlTrajectory extends Module {

    public final BoolSetting onlyWhenHolding = new BoolSetting("Holding", "Only when pearl in hand", true);
    public final NumberSetting steps = new NumberSetting("Steps", "Sim steps", 40, 20, 80, 5);
    public final BoolSetting landMark = new BoolSetting("LandMark", "Red square at land", true);

    public static final List<Vec3d> lastPath = new ArrayList<>();
    public static Vec3d lastLand = null;

    public PearlTrajectory() {
        super("PearlTrajectory", "Pearl arc + land mark", Category.RENDER);
        addSetting(onlyWhenHolding);
        addSetting(steps);
        addSetting(landMark);
    }

    @Override
    public void onTick() {
        lastPath.clear();
        lastLand = null;
        if (mc.player == null || mc.world == null) return;

        boolean holding = mc.player.getMainHandStack().isOf(Items.ENDER_PEARL)
                || mc.player.getOffHandStack().isOf(Items.ENDER_PEARL);
        if (onlyWhenHolding.get() && !holding) return;

        simulate();
    }

    private void simulate() {
        Vec3d pos = mc.player.getEyePos();
        Vec3d vel = mc.player.getRotationVector().multiply(1.5);

        int n = steps.getInt();
        for (int i = 0; i < n; i++) {
            Vec3d next = pos.add(vel);
            HitResult hit = mc.world.raycast(new RaycastContext(
                    pos, next,
                    RaycastContext.ShapeType.COLLIDER,
                    RaycastContext.FluidHandling.NONE,
                    mc.player
            ));
            lastPath.add(pos);
            if (hit.getType() != HitResult.Type.MISS) {
                lastLand = hit.getPos();
                lastPath.add(lastLand);
                break;
            }
            pos = next;
            vel = vel.multiply(0.99).add(0, -0.03, 0);
        }
    }

    /** 2D HUD fallback — land marker near crosshair when path exists. */
    public static void drawHud(DrawContext ctx, int screenW, int screenH) {
        if (lastLand == null || mc == null || mc.player == null || mc.gameRenderer == null) return;
        // Simple indicator text bottom-center
        String t = String.format("Pearl §c%.0f %.0f %.0f",
                lastLand.x, lastLand.y, lastLand.z);
        int tw = mc.textRenderer.getWidth(t);
        ctx.drawTextWithShadow(mc.textRenderer, t, screenW / 2 - tw / 2, screenH - 40, 0xFFAAAA);
    }

    private static net.minecraft.client.MinecraftClient mc =
            net.minecraft.client.MinecraftClient.getInstance();

    private static boolean isSolid(BlockPos p) {
        if (mc.world == null) return true;
        return !mc.world.getBlockState(p).isAir();
    }
}
