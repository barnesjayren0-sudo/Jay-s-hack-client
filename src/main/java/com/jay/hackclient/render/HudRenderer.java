package com.jay.hackclient.render;

import com.jay.hackclient.JayHackClient;
import com.jay.hackclient.compat.BaritoneCompat;
import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.modules.TargetHUD;
import com.jay.hackclient.settings.ClientSettings;
import com.jay.hackclient.util.Mobile;
import com.jay.hackclient.util.Notifications;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

public final class HudRenderer {

    private static final int CYAN = 0xFF3DDCFF;
    /** Slide animation progress 0..1 per module name. */
    private static final Map<String, Float> slide = new HashMap<>();

    private HudRenderer() {}

    public static void render(DrawContext context) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || JayHackClient.moduleManager == null) return;
        if (mc.options.hudHidden) return;
        if (ClientSettings.hideHudInDebug && mc.getDebugHud().shouldShowDebugHud()) return;
        if (ClientSettings.hideHudOnScreenshot) {
            long h = mc.getWindow().getHandle();
            if (GLFW.glfwGetKey(h, GLFW.GLFW_KEY_F2) == GLFW.GLFW_PRESS) return;
        }

        WorldEspRenderer.drawHudOverlay(context);

        boolean phone = Mobile.isSmallScreen();
        List<Module> enabled = new ArrayList<>();
        for (Module m : JayHackClient.moduleManager.getModules()) {
            if (m.isEnabled() && m.isDrawn() && !m.getName().equalsIgnoreCase("HUD")) {
                enabled.add(m);
            }
        }
        enabled.sort(Comparator
                .comparing((Module m) -> !ClientSettings.isFavorite(m.getName()))
                .thenComparingInt(m -> -mc.textRenderer.getWidth(m.getName())));

        // animate slide
        for (Module m : enabled) {
            float cur = slide.getOrDefault(m.getName(), 0f);
            slide.put(m.getName(), Math.min(1f, cur + 0.18f));
        }
        Iterator<Map.Entry<String, Float>> it = slide.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Float> e = it.next();
            boolean still = false;
            for (Module m : enabled) {
                if (m.getName().equals(e.getKey())) { still = true; break; }
            }
            if (!still) {
                float v = e.getValue() - 0.2f;
                if (v <= 0) it.remove();
                else e.setValue(v);
            }
        }

        int maxList = phone ? 8 : 24;
        int screenW = mc.getWindow().getScaledWidth();
        int screenH = mc.getWindow().getScaledHeight();

        int accent = CYAN;
        if (ClientSettings.arrayListRainbow) {
            float hue = (System.currentTimeMillis() % 3000) / 3000f;
            accent = 0xFF000000 | java.awt.Color.HSBtoRGB(hue, 0.7f, 1f);
        }

        String ver = JayHackClient.VERSION;
        int ww = mc.textRenderer.getWidth("Jay " + ver) + 14;
        context.fill(4, 4, 4 + ww, 17, 0x990A0A10);
        context.fill(4, 4, 6, 17, accent);
        context.drawTextWithShadow(mc.textRenderer, "§bJ§fay §8" + ver, 10, 7, 0xFFFFFF);

        String prof = ClientSettings.lastProfile;
        context.drawTextWithShadow(mc.textRenderer, "§8" + prof, 10, 18, 0x888888);

        String bLine = BaritoneCompat.hudLine();
        if (bLine != null) {
            context.fill(4, 27, 4 + mc.textRenderer.getWidth(bLine) + 18, 38, 0x990A0A10);
            context.fill(4, 27, 6, 38, CYAN);
            context.drawTextWithShadow(mc.textRenderer, "§bB §f" + bLine, 10, 29, 0xFFFFFF);
        }

        int y = bLine != null ? 42 : 28;
        int shown = 0;
        for (Module m : enabled) {
            if (shown >= maxList) break;
            float s = slide.getOrDefault(m.getName(), 1f);
            String star = ClientSettings.isFavorite(m.getName()) ? "§e★ " : "";
            String label = star + m.getName();
            int textW = mc.textRenderer.getWidth(m.getName()) + (ClientSettings.isFavorite(m.getName()) ? 12 : 0);
            int slidePx = (int) ((1f - s) * (textW + 16));
            int x = screenW - textW - 10 + slidePx;
            context.fill(x - 5, y - 1, screenW - 2, y + 10, 0x990A0A10);
            context.fill(screenW - 2, y - 1, screenW, y + 10, accent);
            context.drawTextWithShadow(mc.textRenderer, label, x, y, 0xE8E8F0);
            y += 11;
            shown++;
        }

        Module th = JayHackClient.moduleManager.getModuleByName("TargetHUD");
        if (th != null && th.isEnabled() && TargetHUD.currentTarget != null) {
            drawTarget(context, mc, TargetHUD.currentTarget, screenW, screenH, phone);
        }

        Notifications.render(context);
    }

    private static void drawTarget(DrawContext context, MinecraftClient mc, PlayerEntity target,
                                   int sw, int sh, boolean phone) {
        String name = target.getName().getString();
        float hp = target.getHealth() + target.getAbsorptionAmount();
        float max = Math.max(1f, target.getMaxHealth());
        float pct = Math.min(1f, hp / max);
        int boxW = phone ? 110 : 130;
        int boxH = 32;
        int bx = sw / 2 - boxW / 2;
        int by = sh / 2 + (phone ? 22 : 28);

        context.fill(bx + 2, by + 2, bx + boxW + 2, by + boxH + 2, 0x44000000);
        context.fill(bx, by, bx + boxW, by + boxH, 0xEE0E0E14);
        context.fill(bx, by, bx + 2, by + boxH, CYAN);
        context.fill(bx, by, bx + boxW, by + 1, 0x553DDCFF);

        String info = String.format("%s  §7%.1fm", name, TargetHUD.currentDistance);
        context.drawTextWithShadow(mc.textRenderer, info, bx + 8, by + 5, 0xFFF0F0F8);

        int barW = boxW - 16;
        int barY = by + 18;
        context.fill(bx + 8, barY, bx + 8 + barW, barY + 5, 0xFF1A1A24);
        int fill = Math.max(1, (int) (barW * pct));
        int col = pct > 0.5f ? 0xFF3DDCFF : (pct > 0.25f ? 0xFFCCAA33 : 0xFFCC4444);
        context.fill(bx + 8, barY, bx + 8 + fill, barY + 5, col);

        String hpText = String.format("%.1f", hp);
        context.drawTextWithShadow(mc.textRenderer, hpText,
                bx + boxW - 8 - mc.textRenderer.getWidth(hpText), by + 5, CYAN);
    }
}
