package com.jay.hackclient.util;

import com.jay.hackclient.gui.GuiTheme;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** Stacked toasts with styles and durations. */
public final class Notifications {

    public enum Style { INFO, SUCCESS, WARN, ERROR }

    private static final List<Toast> toasts = new ArrayList<>();

    private Notifications() {}

    public static void push(String title, String message) {
        push(title, message, Style.INFO, 3200);
    }

    public static void push(String message) {
        push("Jay", message, Style.INFO, 3200);
    }

    public static void success(String title, String message) {
        push(title, message, Style.SUCCESS, 2800);
    }

    public static void warn(String title, String message) {
        push(title, message, Style.WARN, 4000);
    }

    public static void error(String title, String message) {
        push(title, message, Style.ERROR, 4500);
    }

    public static void push(String title, String message, Style style, int lifeMs) {
        toasts.add(0, new Toast(title, message, style, System.currentTimeMillis(), lifeMs));
        while (toasts.size() > 6) toasts.remove(toasts.size() - 1);
    }

    public static void render(DrawContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null) return;
        TextRenderer tr = mc.textRenderer;
        int y = 28;
        long now = System.currentTimeMillis();
        Iterator<Toast> it = toasts.iterator();
        while (it.hasNext()) {
            Toast t = it.next();
            long age = now - t.born;
            if (age > t.life) {
                it.remove();
                continue;
            }
            float alpha = 1f;
            int fade = 500;
            if (age > t.life - fade) alpha = (t.life - age) / (float) fade;
            if (age < 120) alpha = age / 120f;

            int aw = Math.min(200, Math.max(tr.getWidth(t.title), tr.getWidth(t.message)) + 16);
            int x = mc.getWindow().getScaledWidth() - aw - 8;
            int a = Math.max(0, Math.min(255, (int) (alpha * 230)));
            int bg = GuiTheme.withAlpha(0x121520, a);
            int accent = switch (t.style) {
                case SUCCESS -> GuiTheme.SUCCESS;
                case WARN -> 0xFFFFC857;
                case ERROR -> GuiTheme.DANGER;
                default -> GuiTheme.ACCENT;
            };
            ctx.fill(x, y, x + aw, y + 28, bg);
            ctx.fill(x, y, x + 2, y + 28, GuiTheme.withAlpha(accent & 0xFFFFFF, a));
            ctx.drawTextWithShadow(tr, t.title, x + 8, y + 4, GuiTheme.withAlpha(0xFFFFFF, a));
            ctx.drawTextWithShadow(tr, t.message, x + 8, y + 15, GuiTheme.withAlpha(0xAAAAAA, a));
            y += 32;
        }
    }

    private static final class Toast {
        final String title, message;
        final Style style;
        final long born;
        final int life;

        Toast(String title, String message, Style style, long born, int life) {
            this.title = title;
            this.message = message;
            this.style = style;
            this.born = born;
            this.life = life;
        }
    }
}
