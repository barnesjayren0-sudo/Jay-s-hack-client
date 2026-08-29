package com.jay.hackclient.render;

import com.jay.hackclient.JayHackClient;
import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.modules.BaseFinder;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.VertexRendering;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

/**
 * Colored boxes + tracer lines for BaseFinder / StorageESP / FarmFinder hits.
 */
public final class WorldEspRenderer {

    private static boolean registered;

    private WorldEspRenderer() {}

    public static void register() {
        if (registered) return;
        registered = true;
        WorldRenderEvents.AFTER_ENTITIES.register(WorldEspRenderer::render);
    }

    private static void render(WorldRenderContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;
        if (JayHackClient.moduleManager == null) return;

        Module bf = JayHackClient.moduleManager.getModuleByName("BaseFinder");
        Module se = JayHackClient.moduleManager.getModuleByName("StorageESP");
        Module ff = JayHackClient.moduleManager.getModuleByName("FarmFinder");
        boolean baseOn = bf != null && bf.isEnabled();
        boolean storageOn = se != null && se.isEnabled();
        boolean farmOn = ff != null && ff.isEnabled();
        if (!baseOn && !storageOn && !farmOn) return;
        if (BaseFinder.lastHits.isEmpty()) return;

        MatrixStack matrices = ctx.matrixStack();
        if (matrices == null) return;
        VertexConsumerProvider consumers = ctx.consumers();
        if (consumers == null) return;

        Vec3d cam = ctx.camera().getPos();
        VertexConsumer lines = consumers.getBuffer(RenderLayer.getLines());

        // Tracer origin: slightly in front of camera (Meteor-style)
        Vec3d look = Vec3d.fromPolar(mc.player.getPitch(), mc.player.getYaw());
        Vec3d tracerFrom = mc.player.getEyePos().add(look.multiply(0.35));

        int drawn = 0;
        int maxDraw = BaseFinder.maxEsp == 0 ? 80 : BaseFinder.maxEsp;

        for (BaseFinder.Hit hit : BaseFinder.lastHits) {
            if (drawn >= maxDraw) break;

            if (!baseOn) {
                if (storageOn && farmOn) {
                    if (!isStorageLabel(hit.label) && !isFarmLabel(hit.label)) continue;
                } else if (storageOn && !isStorageLabel(hit.label)) continue;
                else if (farmOn && !isFarmLabel(hit.label)) continue;
            }

            BlockPos p = hit.pos;
            double dist = Math.sqrt(cam.squaredDistanceTo(p.getX() + 0.5, p.getY() + 0.5, p.getZ() + 0.5));
            if (dist > BaseFinder.espRange) continue;

            float r = ((hit.color >> 16) & 0xFF) / 255f;
            float g = ((hit.color >> 8) & 0xFF) / 255f;
            float b = (hit.color & 0xFF) / 255f;
            // Fade with distance
            float a = (float) Math.max(0.25, 0.95 - dist / Math.max(16.0, BaseFinder.espRange));

            Box box = new Box(p).expand(0.002);

            matrices.push();
            matrices.translate(-cam.x, -cam.y, -cam.z);

            if (BaseFinder.drawBoxes) {
                try {
                    VertexRendering.drawBox(matrices, lines, box, r, g, b, a);
                } catch (Throwable ignored) {
                    drawBoxManual(matrices, lines, box, r, g, b, a);
                }
            }

            if (BaseFinder.drawTracers) {
                drawTracer(matrices, lines, tracerFrom, box.getCenter(), r, g, b, a * 0.75f);
            }

            matrices.pop();
            drawn++;
        }
    }

    private static boolean isStorageLabel(String label) {
        String l = label.toLowerCase();
        return l.contains("chest") || l.contains("barrel") || l.contains("shulker")
                || l.contains("hopper") || l.contains("ender") || l.contains("dispenser")
                || l.contains("dropper");
    }

    private static boolean isFarmLabel(String label) {
        String l = label.toLowerCase();
        return l.contains("farm") || l.contains("kelp") || l.contains("cane")
                || l.contains("bamboo") || l.contains("wart") || l.contains("crop")
                || l.contains("melon") || l.contains("farmland") || l.contains("compost")
                || l.contains("cactus") || l.contains("cocoa") || l.contains("berry");
    }

    private static void drawBoxManual(MatrixStack ms, VertexConsumer vc, Box box,
                                      float r, float g, float b, float a) {
        float x0 = (float) box.minX, y0 = (float) box.minY, z0 = (float) box.minZ;
        float x1 = (float) box.maxX, y1 = (float) box.maxY, z1 = (float) box.maxZ;
        // Bottom
        line(ms, vc, x0, y0, z0, x1, y0, z0, r, g, b, a);
        line(ms, vc, x1, y0, z0, x1, y0, z1, r, g, b, a);
        line(ms, vc, x1, y0, z1, x0, y0, z1, r, g, b, a);
        line(ms, vc, x0, y0, z1, x0, y0, z0, r, g, b, a);
        // Top
        line(ms, vc, x0, y1, z0, x1, y1, z0, r, g, b, a);
        line(ms, vc, x1, y1, z0, x1, y1, z1, r, g, b, a);
        line(ms, vc, x1, y1, z1, x0, y1, z1, r, g, b, a);
        line(ms, vc, x0, y1, z1, x0, y1, z0, r, g, b, a);
        // Verticals
        line(ms, vc, x0, y0, z0, x0, y1, z0, r, g, b, a);
        line(ms, vc, x1, y0, z0, x1, y1, z0, r, g, b, a);
        line(ms, vc, x1, y0, z1, x1, y1, z1, r, g, b, a);
        line(ms, vc, x0, y0, z1, x0, y1, z1, r, g, b, a);
    }

    private static void line(MatrixStack ms, VertexConsumer vc,
                             float x0, float y0, float z0,
                             float x1, float y1, float z1,
                             float r, float g, float b, float a) {
        var entry = ms.peek();
        vc.vertex(entry, x0, y0, z0).color(r, g, b, a).normal(entry, 0, 1, 0);
        vc.vertex(entry, x1, y1, z1).color(r, g, b, a).normal(entry, 0, 1, 0);
    }

    private static void drawTracer(MatrixStack ms, VertexConsumer vc,
                                   Vec3d from, Vec3d to, float r, float g, float b, float a) {
        var entry = ms.peek();
        vc.vertex(entry, (float) from.x, (float) from.y, (float) from.z)
                .color(r, g, b, a).normal(entry, 0, 1, 0);
        vc.vertex(entry, (float) to.x, (float) to.y, (float) to.z)
                .color(r, g, b, a).normal(entry, 0, 1, 0);
    }
}
