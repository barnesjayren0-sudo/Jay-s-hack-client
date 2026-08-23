package com.jay.hackclient.render;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.util.Identifier;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.Base64;

/** Jay logo texture for ClickGUI — loads base64 from mod resources. */
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
            NativeImageBackedTexture tex;
            try {
                tex = new NativeImageBackedTexture(() -> "jay_logo", image);
            } catch (Throwable t) {
                tex = new NativeImageBackedTexture(image);
            }
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
            ctx.drawTexture(
                    net.minecraft.client.render.RenderLayer::getGuiTextured,
                    tex, x, y, 0f, 0f, size, size, size, size);
        } catch (Throwable t) {
            try {
                ctx.drawTexture(tex, x, y, 0f, 0f, size, size, size, size);
            } catch (Throwable ignored) {
            }
        }
    }
}
