package com.jay.hackclient.util;

import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.util.math.MathHelper;

import java.awt.Color;

/**
 * Premium drawing helpers — rounded rects, glow, gradients, health bars.
 * Style inspired by Prestige / Grave / Elusive Ghost clients.
 */
public final class RenderUtil {

    private RenderUtil() {}

    /** Smooth lerp for health / animation values */
    public static float lerp(float current, float target, float speed) {
        return current + (target - current) * MathHelper.clamp(speed, 0f, 1f);
    }

    public static int withAlpha(int color, float alpha) {
        int a = MathHelper.clamp((int) (alpha * 255f), 0, 255);
        return (color & 0x00FFFFFF) | (a << 24);
    }

    public static int rgba(int r, int g, int b, int a) {
        return ((a & 0xFF) << 24) | ((r & 0xFF) << 16) | ((g & 0xFF) << 8) | (b & 0xFF);
    }

    public static Color interpolateColor(Color from, Color to, float progress) {
        progress = MathHelper.clamp(progress, 0f, 1f);
        int r = (int) (from.getRed() + (to.getRed() - from.getRed()) * progress);
        int g = (int) (from.getGreen() + (to.getGreen() - from.getGreen()) * progress);
        int b = (int) (from.getBlue() + (to.getBlue() - from.getBlue()) * progress);
        int a = (int) (from.getAlpha() + (to.getAlpha() - from.getAlpha()) * progress);
        return new Color(r, g, b, a);
    }

    /** Health color: green → yellow → red */
    public static int healthColor(float health, float maxHealth) {
        float pct = MathHelper.clamp(health / Math.max(0.1f, maxHealth), 0f, 1f);
        if (pct > 0.6f) {
            float t = (pct - 0.6f) / 0.4f;
            return rgba((int) (255 * (1 - t)), 220, 40, 255);
        } else {
            float t = pct / 0.6f;
            return rgba(255, (int) (220 * t), 40, 255);
        }
    }

    /** Accent purple matching the reference screenshot */
    public static final int ACCENT = 0xFF9B6BFF;
    public static final int ACCENT_DARK = 0xFF6B3FA0;
    public static final int BG_DARK = 0xE6121218;
    public static final int BG_PANEL = 0xF00E0E14;
    public static final int TEXT_WHITE = 0xFFFFFFFF;
    public static final int TEXT_DIM = 0xFFAAAAAA;
    public static final int BORDER = 0x40FFFFFF;

    public static void drawRoundedRect(DrawContext ctx, float x, float y, float w, float h, float radius, int color) {
        int ix = (int) x, iy = (int) y, iw = (int) w, ih = (int) h;
        ctx.fill(ix, iy, ix + iw, iy + ih, color);
        int edge = withAlpha(color, 0.35f);
        ctx.fill(ix, iy, ix + iw, iy + 1, edge);
        ctx.fill(ix, iy + ih - 1, ix + iw, iy + ih, edge);
    }

    public static void drawRect(DrawContext ctx, float x, float y, float w, float h, int color) {
        ctx.fill((int) x, (int) y, (int) (x + w), (int) (y + h), color);
    }

    public static void drawHealthBar(DrawContext ctx, float x, float y, float w, float h,
                                     float healthPct, float animatedPct, int bgColor) {
        drawRect(ctx, x, y, w, h, bgColor);
        float fillW = w * MathHelper.clamp(animatedPct, 0f, 1f);
        if (fillW > 0.5f) {
            int col = healthColor(animatedPct * 20f, 20f);
            drawRect(ctx, x, y, fillW, h, col);
            drawRect(ctx, x, y, fillW, Math.max(1, h * 0.3f), withAlpha(0xFFFFFFFF, 0.18f));
        }
    }

    public static void drawGlowRect(DrawContext ctx, float x, float y, float w, float h, int color, float strength) {
        for (int i = 3; i >= 1; i--) {
            float a = strength * (0.12f / i);
            drawRect(ctx, x - i, y - i, w + i * 2, h + i * 2, withAlpha(color, a));
        }
        drawRect(ctx, x, y, w, h, color);
    }
}
