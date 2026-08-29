package com.jay.hackclient.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.font.TextRenderer;
import net.minecraft.client.gui.DrawContext;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** Stacked toast notifications — slide + fade. */
public final class Notifications {

    private static final List<Toast> toasts = new ArrayList<>();
    private static final int LIFE = 3200;
    private static final int FADE = 500;

    private Notifications() {}

    public static void push(String title, String message) {
        toasts.add(0, new Toast(title, message, System.currentTimeMillis()));
        while (toasts.size() > 5) toasts.remove(toasts.size() - 1);
    }

    public static void push(String message) {
        push("Jay", message);
    }

    public static void render(DrawContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null) return;
        TextRenderer tr = mc.textRenderer;
        long now = System.currentTimeMillis();
        int y = 28;
        int screenW = mc.getWindow().getScaledWidth();

        Iterator<Toast> it = toasts.iterator();
        while (it.hasNext()) {
            Toast t = it.next();
            long age = now - t.born;
            if (age > LIFE) { it.remove(); continue; }

            float fade = age > LIFE - FADE ? (LIFE - age) / (float) FADE : 1f;
            float slide = age < 150 ? age / 150f : 1f;
            int alpha = Math.max(0, Math.min(255, (int) (fade * 230)));

            int textW = Math.max(tr.getWidth(t.title), tr.getWidth(t.msg));
            int w = textW + 20;
            int h = 30;
            int x = screenW - w - 8 + (int) ((1f - slide) * (w + 12));

            int bg = (alpha << 24) | 0x0C0C14;
            int accent = (alpha << 24) | 0x3DDCFF;
            int border = (alpha << 24) | 0x222833;

            // shadow
            ctx.fill(x + 2, y + 2, x + w + 2, y + h + 2, (alpha / 3) << 24);
            // body
            ctx.fill(x, y, x + w, y + h, bg);
            ctx.fill(x, y, x + w, y + 1, border);
            ctx.fill(x, y + h - 1, x + w, y + h, border);
            // accent bar
            ctx.fill(x, y, x + 2, y + h, accent);
            // progress
            int prog = (int) (w * (1f - age / (float) LIFE));
            ctx.fill(x, y + h - 2, x + prog, y + h - 1, accent);

            int tc = (alpha << 24) | 0x3DDCFF;
            int mc2 = (alpha << 24) | 0xE8E8F0;
            ctx.drawTextWithShadow(tr, t.title, x + 8, y + 5, tc);
            ctx.drawTextWithShadow(tr, t.msg, x + 8, y + 16, mc2);
            y += h + 4;
        }
    }

    private static final class Toast {
        final String title, msg;
        final long born;
        Toast(String title, String msg, long born) {
            this.title = title;
            this.msg = msg;
            this.born = born;
        }
    }
}
