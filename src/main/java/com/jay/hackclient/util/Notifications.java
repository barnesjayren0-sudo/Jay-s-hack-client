package com.jay.hackclient.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.font.TextRenderer;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/** Lightweight toast notifications (inspired by modern client notification stacks). */
public final class Notifications {

    private static final List<Toast> toasts = new ArrayList<>();

    private Notifications() {}

    public static void push(String title, String message) {
        toasts.add(new Toast(title, message, System.currentTimeMillis()));
        if (toasts.size() > 6) toasts.remove(0);
    }

    public static void push(String message) {
        push("Jay", message);
    }

    public static void render(DrawContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null) return;
        TextRenderer tr = mc.textRenderer;
        long now = System.currentTimeMillis();
        int y = 30;
        Iterator<Toast> it = toasts.iterator();
        while (it.hasNext()) {
            Toast t = it.next();
            long age = now - t.born;
            if (age > 3500) { it.remove(); continue; }
            float fade = age > 2800 ? 1f - (age - 2800) / 700f : 1f;
            int alpha = (int) (fade * 220) << 24;
            int w = Math.max(tr.getWidth(t.title), tr.getWidth(t.msg)) + 16;
            int x = mc.getWindow().getScaledWidth() - w - 8;
            ctx.fill(x, y, x + w, y + 28, alpha | 0x0C0C14);
            ctx.fill(x, y, x + 2, y + 28, 0xFF3DDCFF);
            ctx.drawTextWithShadow(tr, t.title, x + 8, y + 4, 0xFF3DDCFF);
            ctx.drawTextWithShadow(tr, t.msg, x + 8, y + 15, 0xFFE0E0E8);
            y += 32;
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
