package com.jay.hackclient.util;

import com.jay.hackclient.gui.GuiTheme;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** Stacked toasts with info/success/warn/error styles. */
public final class Notifications {

    public enum Style { INFO, SUCCESS, WARN, ERROR }

    private static final List<Toast> TOASTS = new ArrayList<>();
    private static final int MAX = 6;

    private Notifications() {}

    public static void push(String title, String body) {
        push(title, body, Style.INFO, 2200);
    }

    public static void push(String title, String body, Style style) {
        push(title, body, style, 2200);
    }

    public static void push(String title, String body, Style style, long durationMs) {
        if (title == null) title = "";
        if (body == null) body = "";
        synchronized (TOASTS) {
            TOASTS.add(0, new Toast(title, body, style == null ? Style.INFO : style,
                    System.currentTimeMillis(), Math.max(800, durationMs)));
            while (TOASTS.size() > MAX) {
                TOASTS.remove(TOASTS.size() - 1);
            }
        }
    }

    public static void success(String title, String body) {
        push(title, body, Style.SUCCESS, 2000);
    }

    public static void warn(String title, String body) {
        push(title, body, Style.WARN, 2600);
    }

    public static void error(String title, String body) {
        push(title, body, Style.ERROR, 3200);
    }

    public static void moduleEnabled(String name) {
        success(name, "enabled");
    }

    public static void moduleDisabled(String name) {
        push(name, "disabled", Style.INFO, 1600);
    }

    public static void render(DrawContext ctx, int screenW, int screenH) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || ctx == null) return;
        TextRenderer fr = mc.textRenderer;
        long now = System.currentTimeMillis();

        synchronized (TOASTS) {
            Iterator<Toast> it = TOASTS.iterator();
            int y = 12;
            while (it.hasNext()) {
                Toast t = it.next();
                long age = now - t.created;
                if (age > t.duration) {
                    it.remove();
                    continue;
                }

                float life = 1f - (age / (float) t.duration);
                int alpha = (int) (Math.min(1f, life * 4f) * 220);
                int bg = (alpha << 24) | (colorFor(t.style) & 0xFFFFFF);
                int accent = 0xFF000000 | (accentFor(t.style) & 0xFFFFFF);

                int w = Math.max(120, fr.getWidth(t.title) + fr.getWidth(t.body) + 28);
                int x = screenW - w - 10;

                ctx.fill(x, y, x + w, y + 22, bg);
                ctx.fill(x, y, x + 3, y + 22, accent);
                // 1.21.x API: DrawContext.drawTextWithShadow(TextRenderer, ...)
                ctx.drawTextWithShadow(fr, t.title, x + 8, y + 3, 0xFFFFFFFF);
                ctx.drawTextWithShadow(fr, t.body, x + 8, y + 12, 0xFFCCCCCC);
                y += 26;
            }
        }
    }

    private static int colorFor(Style s) {
        return switch (s) {
            case SUCCESS -> 0x10261A;
            case WARN -> 0x2A2208;
            case ERROR -> 0x2A1010;
            default -> 0x12151C;
        };
    }

    private static int accentFor(Style s) {
        return switch (s) {
            case SUCCESS -> 0x3DFF8A;
            case WARN -> 0xFFC93D;
            case ERROR -> 0xFF5A5A;
            default -> GuiTheme.ACCENT;
        };
    }

    private static final class Toast {
        final String title;
        final String body;
        final Style style;
        final long created;
        final long duration;

        Toast(String title, String body, Style style, long created, long duration) {
            this.title = title;
            this.body = body;
            this.style = style;
            this.created = created;
            this.duration = duration;
        }
    }
}
