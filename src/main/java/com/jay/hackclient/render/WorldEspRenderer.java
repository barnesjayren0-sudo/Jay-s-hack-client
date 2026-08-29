package com.jay.hackclient.render;

import com.jay.hackclient.JayHackClient;
import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.modules.BaseFinder;
import com.jay.hackclient.module.modules.StorageESP;
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
 * World-space boxes + tracers for BaseFinder / StorageESP hits.
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
        boolean baseOn = bf != null && bf.isEnabled();
        boolean storageOn = se != null && se.isEnabled();
        if (!baseOn && !storageOn) return;
        if (BaseFinder.lastHits.isEmpty()) return;

        MatrixStack matrices = ctx.matrixStack();
        if (matrices == null) return;
        VertexConsumerProvider consumers = ctx.consumers();
        if (consumers == null) return;

        Vec3d cam = ctx.camera().getPos();
        VertexConsumer lines = consumers.getBuffer(RenderLayer.getLines());

        Vec3d playerEyes = mc.player.getEyePos();

        int drawn = 0;
        for (BaseFinder.Hit hit : BaseFinder.lastHits) {
            if (drawn > 60) break;

            // StorageESP-only mode: skip non-storage labels
            if (!baseOn && storageOn && !isStorageLabel(hit.label)) continue;

            BlockPos p = hit.pos;
            float r = ((hit.color >> 16) & 0xFF) / 255f;
            float g = ((hit.color >> 8) & 0xFF) / 255f;
            float b = (hit.color & 0xFF) / 255f;
            float a = 0.9f;

            Box box = new Box(p).expand(0.01);

            matrices.push();
            matrices.translate(-cam.x, -cam.y, -cam.z);

            if (BaseFinder.drawBoxes) {
                try {
                    VertexRendering.drawBox(matrices, lines, box, r, g, b, a);
                } catch (Throwable t) {
                    // API variance — skip box
                }
            }

            if (BaseFinder.drawTracers) {
                try {
                    drawTracer(matrices, lines, playerEyes, box.getCenter(), r, g, b, 0.65f);
                } catch (Throwable ignored) {}
            }

            matrices.pop();
            drawn++;
        }
    }

    private static boolean isStorageLabel(String label) {
        return label.startsWith("Chest") || label.startsWith("Barrel")
                || label.startsWith("Shulker") || label.startsWith("Hopper")
                || label.startsWith("Ender");
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
