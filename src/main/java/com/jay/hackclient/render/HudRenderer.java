package com.jay.hackclient.render;

import com.jay.hackclient.JayHackClient;
import com.jay.hackclient.compat.BaritoneCompat;
import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.modules.TargetHUD;
import com.jay.hackclient.settings.ClientSettings;
import com.jay.hackclient.util.Mobile;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public final class HudRenderer {

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

        boolean phone = Mobile.isSmallScreen();
        List<Module> enabled = new ArrayList<>();
        for (Module m : JayHackClient.moduleManager.getModules()) {
            if (m.isEnabled() && !m.getName().equalsIgnoreCase("HUD")) enabled.add(m);
        }
        enabled.sort(Comparator
                .comparing((Module m) -> !ClientSettings.isFavorite(m.getName()))
                .thenComparingInt(m -> -mc.textRenderer.getWidth(m.getName())));

        int maxList = phone ? 8 : 24;
        int screenW = mc.getWindow().getScaledWidth();
        int screenH = mc.getWindow().getScaledHeight();

        int accent = 0xFF000000 | (ClientSettings.arrayListColor & 0xFFFFFF);
        if (ClientSettings.arrayListRainbow) {
            float hue = (System.currentTimeMillis() % 3000) / 3000f;
            accent = 0xFF000000 | java.awt.Color.HSBtoRGB(hue, 0.7f, 1f);
        }

        String ver = JayHackClient.VERSION;
        int ww = mc.textRenderer.getWidth("Jay " + ver) + 14;
        context.fill(4, 4, 4 + ww, 17, 0x990A0A10);
        context.fill(4, 4, 6, 17, accent);
        context.drawTextWithShadow(mc.textRenderer, "§dJ§fay §8" + ver, 10, 7, 0xFFFFFF);

        String prof = ClientSettings.lastProfile;
        context.drawTextWithShadow(mc.textRenderer, "§8" + prof, 10, 18, 0x888888);

        // Baritone path status under profile
        String bLine = BaritoneCompat.hudLine();
        if (bLine != null) {
            context.drawTextWithShadow(mc.textRenderer, "§bB §f" + bLine, 10, 28, 0xFFFFFF);
        }

        int y = bLine != null ? 40 : 28;
        int shown = 0;
        for (Module m : enabled) {
            if (shown >= maxList) break;
            String star = ClientSettings.isFavorite(m.getName()) ? "§e★ " : "";
            String label = star + m.getName();
            int w = mc.textRenderer.getWidth(m.getName()) + (ClientSettings.isFavorite(m.getName()) ? 12 : 0);
            int x = screenW - w - 10;
            context.fill(x - 5, y - 1, screenW - 2, y + 10, 0x990A0A10);
            context.fill(screenW - 2, y - 1, screenW, y + 10, accent);
            context.drawTextWithShadow(mc.textRenderer, label, x, y, 0xE8E8F0);
            y += 11;
            shown++;
        }

        Module th = JayHackClient.moduleManager.getModuleByName("TargetHUD");
        if (th != null && th.isEnabled() && TargetHUD.currentTarget != null) {
            drawTarget(context, mc, TargetHUD.currentTarget, screenW, screenH, phone, accent);
        }
    }

    private static void drawTarget(DrawContext context, MinecraftClient mc, PlayerEntity target,
                                   int sw, int sh, boolean phone, int accent) {
        String name = target.getName().getString();
        float hp = target.getHealth() + target.getAbsorptionAmount();
        float max = Math.max(1f, target.getMaxHealth());
        float pct = Math.min(1f, hp / max);
        int boxW = phone ? 100 : 120;
        int bx = sw / 2 - boxW / 2;
        int by = sh / 2 + (phone ? 24 : 30);
        context.fill(bx, by, bx + boxW, by + 28, 0xCC0A0A10);
        context.fill(bx, by, bx + 2, by + 28, accent);
        String info = String.format("%s  %.1fm", name, TargetHUD.currentDistance);
        context.drawTextWithShadow(mc.textRenderer, info, bx + 8, by + 4, 0xFFFFFF);
        int barW = boxW - 16;
        context.fill(bx + 8, by + 16, bx + 8 + barW, by + 22, 0xFF222228);
        int fill = (int) (barW * pct);
        int col = pct > 0.5f ? 0xFF44CC66 : (pct > 0.25f ? 0xFFCCAA33 : 0xFFCC4444);
        context.fill(bx + 8, by + 16, bx + 8 + fill, by + 22, col);
        // avoid hard dependency on ComboAssist class at runtime
        String hpText = String.format("%.1f HP", hp);
        context.drawTextWithShadow(mc.textRenderer, hpText, bx + 8, by + 24, 0xFFD0D0D8);
    }
}
