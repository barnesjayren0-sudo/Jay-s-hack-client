package com.jay.hackclient.render;

import com.jay.hackclient.JayHackClient;
import com.jay.hackclient.compat.BaritoneCompat;
import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.modules.InfoHUD;
import com.jay.hackclient.module.modules.ReachHUD;
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
        int screenW = mc.getWindow().getScaledWidth();
        int screenH = mc.getWindow().getScaledHeight();

        int accent = CYAN;
        if (ClientSettings.arrayListRainbow) {
            float hue = (System.currentTimeMillis() % 3000) / 3000f;
            accent = 0xFF000000 | java.awt.Color.HSBtoRGB(hue, 0.7f, 1f);
        }

        String ver = JayHackClient.VERSION;
        int active = 0;
        try {
            for (Module m : JayHackClient.moduleManager.getActive()) {
                if (m.isDrawn()) active++;
            }
        } catch (Throwable ignored) {}
        String watermark = ClientSettings.showActiveCount
                ? ("§bJ§fay §8" + ver + " §7[" + active + "]")
                : ("§bJ§fay §8" + ver);
        int ww = mc.textRenderer.getWidth(watermark.replace("§b", "").replace("§f", "")
                .replace("§8", "").replace("§7", "")) + 14;
        context.fill(4, 4, 4 + ww, 17, 0x990A0A10);
        context.fill(4, 4, 6, 17, accent);
        context.drawTextWithShadow(mc.textRenderer, watermark, 10, 7, 0xFFFFFF);
        context.drawTextWithShadow(mc.textRenderer, "§8" + ClientSettings.lastProfile, 10, 18, 0x888888);

        int leftY = 28;

        Module info = JayHackClient.moduleManager.getModuleByName("InfoHUD");
        if (info != null && info.isEnabled()) {
            if (InfoHUD.coords.get()) {
                String c = String.format("§7XYZ §f%.1f §7%.0f §f%.1f",
                        mc.player.getX(), mc.player.getY(), mc.player.getZ());
                context.drawTextWithShadow(mc.textRenderer, c, 10, leftY, 0xFFFFFF);
                leftY += 10;
            }
            if (InfoHUD.fps.get()) {
                context.drawTextWithShadow(mc.textRenderer, "§7FPS §f" + mc.getCurrentFps(), 10, leftY, 0xFFFFFF);
                leftY += 10;
            }
            if (InfoHUD.speed.get()) {
                double dx = mc.player.getX() - mc.player.lastRenderX;
                double dz = mc.player.getZ() - mc.player.lastRenderZ;
                double bps = Math.sqrt(dx * dx + dz * dz) * 20.0;
                context.drawTextWithShadow(mc.textRenderer, String.format("§7BPS §f%.2f", bps), 10, leftY, 0xFFFFFF);
                leftY += 10;
            }
            if (InfoHUD.ping.get()) {
                int pingMs = 0;
                try {
                    if (mc.getNetworkHandler() != null) {
                        var pe = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
                        if (pe != null) pingMs = pe.getLatency();
                    }
                } catch (Throwable ignored) {}
                context.drawTextWithShadow(mc.textRenderer, "§7Ping §f" + pingMs, 10, leftY, 0xFFFFFF);
                leftY += 10;
            }
        }

        Module rh = JayHackClient.moduleManager.getModuleByName("ReachHUD");
        if (rh != null && rh.isEnabled()) {
            String r = String.format("§7Reach §f%.2f §8| §7XH §f%.2f", ReachHUD.lastReach, ReachHUD.crosshairDist);
            context.drawTextWithShadow(mc.textRenderer, r, 10, leftY, 0xFFFFFF);
            leftY += 10;
        }

        String bLine = BaritoneCompat.hudLine();
        if (bLine != null) {
            context.fill(4, leftY, 4 + mc.textRenderer.getWidth(bLine) + 18, leftY + 11, 0x990A0A10);
            context.fill(4, leftY, 6, leftY + 11, CYAN);
            context.drawTextWithShadow(mc.textRenderer, "§bB §f" + bLine, 10, leftY + 2, 0xFFFFFF);
            leftY += 14;
        }

        List<Module> enabled = new ArrayList<>();
        for (Module m : JayHackClient.moduleManager.getActive()) {
            if (m.isDrawn() && !m.getName().equalsIgnoreCase("HUD") && !m.getName().equalsIgnoreCase("InfoHUD"))
                enabled.add(m);
        }
        enabled.sort(Comparator
                .comparing((Module m) -> !ClientSettings.isFavorite(m.getName()))
                .thenComparingInt(m -> -mc.textRenderer.getWidth(m.getName())));

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
        int y = 28;
        int shown = 0;
        for (Module m : enabled) {
            if (shown >= maxList) break;
            float s = slide.getOrDefault(m.getName(), 1f);
            String star = ClientSettings.isFavorite(m.getName()) ? "§e★ " : "";
            String label = star + m.getName();
            int textW = mc.textRenderer.getWidth(m.getName()) + (ClientSettings.isFavorite(m.getName()) ? 12 : 0);
            int slidePx = (int) ((1f - s) * (textW + 16));
            int x = screenW - textW - 10 + slidePx;
            int rowAccent = ClientSettings.arrayListRainbow ? accent : m.getCategoryColor();
            context.fill(x - 5, y - 1, screenW - 2, y + 10, 0x990A0A10);
            context.fill(screenW - 2, y - 1, screenW, y + 10, rowAccent);
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
