package com.jay.hackclient.render;

import com.mojang.blaze3d.pipeline.RenderPipeline;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gl.RenderPipelines;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/** Jay logo texture for ClickGUI — 1.21.11 texture + DrawContext APIs. */
public final class JayLogo {

    private static Identifier id;
    private static boolean failed;

    private JayLogo() {}

    public static Identifier texture() {
        if (id != null || failed) return id;
        try {
            InputStream in = JayLogo.class.getClassLoader()
                    .getResourceAsStream("assets/jayhackclient/logo.b64");
            if (in == null) {
                failed = true;
                return null;
            }
            String b64 = new String(in.readAllBytes(), StandardCharsets.UTF_8).replaceAll("\\s", "");
            in.close();
            byte[] data = Base64.getDecoder().decode(b64);
            NativeImage image = NativeImage.read(new ByteArrayInputStream(data));

            // 1.21.11 requires name supplier
            NativeImageBackedTexture tex =
                    new NativeImageBackedTexture(() -> "jay_logo", image);

            id = Identifier.of("jayhackclient", "logo_embedded");
            MinecraftClient.getInstance().getTextureManager().registerTexture(id, tex);
        } catch (Throwable t) {
            failed = true;
            System.err.println("[Jay] logo load failed: " + t.getMessage());
        }
        return id;
    }

    public static void draw(DrawContext ctx, int x, int y, int size) {
        Identifier tex = texture();
        if (tex == null) return;
        try {
            // 1.21.11: first arg is RenderPipeline (GUI_TEXTURED)
            RenderPipeline pipeline = RenderPipelines.GUI_TEXTURED;
            ctx.drawTexture(pipeline, tex, x, y, 0f, 0f, size, size, size, size);
        } catch (Throwable t) {
            // Logo is optional — GUI still works without it
            System.err.println("[Jay] logo draw failed: " + t.getMessage());
        }
    }
}
