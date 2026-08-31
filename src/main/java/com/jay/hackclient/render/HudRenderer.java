package com.jay.hackclient.render;

import com.jay.hackclient.JayHackClient;
import com.jay.hackclient.compat.BaritoneCompat;
import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.modules.InfoHUD;
import com.jay.hackclient.module.modules.PerfDashboard;
import com.jay.hackclient.module.modules.ReachHUD;
import com.jay.hackclient.module.modules.TargetHUD;
import com.jay.hackclient.settings.ClientSettings;
import com.jay.hackclient.util.Mobile;
import com.jay.hackclient.util.Notifications;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
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

        // Screenshot-safe: F2 or debug HUD
        if (ClientSettings.hideHudInDebug && mc.getDebugHud().shouldShowDebugHud()) return;
        if (ClientSettings.hideHudOnScreenshot) {
            long h = mc.getWindow().getHandle();
            if (GLFW.glfwGetKey(h, GLFW.GLFW_KEY_F2) == GLFW.GLFW_PRESS) return;
            // F3+F2 style: while F3 held, suppress
            if (GLFW.glfwGetKey(h, GLFW.GLFW_KEY_F3) == GLFW.GLFW_PRESS) return;
        }

        try { WorldEspRenderer.drawHudOverlay(context); } catch (Throwable ignored) {}

        boolean phone = Mobile.isSmallScreen();
        int screenW = mc.getWindow().getScaledWidth();
        int screenH = mc.getWindow().getScaledHeight();

        int accent = CYAN;
        if (ClientSettings.arrayListRainbow) {
            float hue = (System.currentTimeMillis() % 3000) / 3000f;
            accent = 0xFF000000 | java.awt.Color.HSBtoRGB(hue, 0.7f, 1f);
        }

        // Watermark
        if (HudLayout.visible("watermark")) {
            HudLayout.Element wm = HudLayout.get("watermark");
            int wx = HudLayout.resolveX(wm, screenW);
            int wy = HudLayout.resolveY(wm, 4);
            String ver = JayHackClient.VERSION;
            int active = 0;
            for (Module m : JayHackClient.moduleManager.getActive()) {
                if (m.isDrawn()) active++;
            }
            String watermark = ClientSettings.showActiveCount
                    ? ("§bJ§fay §8" + ver + " §7[" + active + "]")
                    : ("§bJ§fay §8" + ver);
            int ww = mc.textRenderer.getWidth(strip(watermark)) + 14;
            context.fill(wx, wy, wx + ww, wy + 13, 0x990A0A10);
            context.fill(wx, wy, wx + 2, wy + 13, accent);
            context.drawTextWithShadow(mc.textRenderer, watermark, wx + 6, wy + 3, 0xFFFFFF);
            context.drawTextWithShadow(mc.textRenderer, "§8" + ClientSettings.lastProfile, wx + 6, wy + 14, 0x888888);
        }

        Module info = JayHackClient.moduleManager.getModuleByName("InfoHUD");
        if (info != null && info.isEnabled()) {
            if (InfoHUD.coords.get() && HudLayout.visible("coords")) {
                HudLayout.Element e = HudLayout.get("coords");
                int x = HudLayout.resolveX(e, screenW);
                int y = HudLayout.resolveY(e, 52);
                String c = String.format("§7XYZ §f%.1f §7%.0f §f%.1f",
                        mc.player.getX(), mc.player.getY(), mc.player.getZ());
                context.drawTextWithShadow(mc.textRenderer, c, x, y, 0xFFFFFF);
            }
            if (InfoHUD.fps.get() && HudLayout.visible("fps")) {
                HudLayout.Element e = HudLayout.get("fps");
                context.drawTextWithShadow(mc.textRenderer, "§7FPS §f" + mc.getCurrentFps(),
                        HudLayout.resolveX(e, screenW), HudLayout.resolveY(e, 28), 0xFFFFFF);
            }
            if (InfoHUD.speed.get() && HudLayout.visible("speed")) {
                HudLayout.Element e = HudLayout.get("speed");
                double dx = mc.player.getX() - mc.player.lastRenderX;
                double dz = mc.player.getZ() - mc.player.lastRenderZ;
                double bps = Math.sqrt(dx * dx + dz * dz) * 20.0;
                context.drawTextWithShadow(mc.textRenderer, String.format("§7BPS §f%.2f", bps),
                        HudLayout.resolveX(e, screenW), HudLayout.resolveY(e, 64), 0xFFFFFF);
            }
            if (InfoHUD.ping.get() && HudLayout.visible("ping")) {
                HudLayout.Element e = HudLayout.get("ping");
                int pingMs = 0;
                try {
                    if (mc.getNetworkHandler() != null) {
                        var pe = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
                        if (pe != null) pingMs = pe.getLatency();
                    }
                } catch (Throwable ignored) {}
                context.drawTextWithShadow(mc.textRenderer, "§7Ping §f" + pingMs,
                        HudLayout.resolveX(e, screenW), HudLayout.resolveY(e, 40), 0xFFFFFF);
            }
        }

        Module rh = JayHackClient.moduleManager.getModuleByName("ReachHUD");
        if (rh != null && rh.isEnabled()) {
            String r = String.format("§7Reach §f%.2f §8| §7XH §f%.2f", ReachHUD.lastReach, ReachHUD.crosshairDist);
            context.drawTextWithShadow(mc.textRenderer, r, 10, screenH - 24, 0xFFFFFF);
        }

        String bLine = BaritoneCompat.hudLine();
        if (bLine != null) {
            context.drawTextWithShadow(mc.textRenderer, "§bB §f" + bLine, 10, screenH - 36, 0xFFFFFF);
        }

        // ArrayList — combat vs utility split
        List<Module> combat = new ArrayList<>();
        List<Module> util = new ArrayList<>();
        for (Module m : JayHackClient.moduleManager.getActive()) {
            if (!m.isDrawn()) continue;
            String n = m.getName();
            if (n.equalsIgnoreCase("HUD") || n.equalsIgnoreCase("InfoHUD")) continue;
            if (m.getCategory() == Module.Category.COMBAT || m.getCategory() == Module.Category.ANARCHY) {
                combat.add(m);
            } else {
                util.add(m);
            }
        }
        combat.sort(listOrder(mc));
        util.sort(listOrder(mc));

        updateSlide(combat);
        updateSlide(util);

        if (HudLayout.visible("arraylist")) {
            drawArrayList(context, mc, combat, screenW, HudLayout.get("arraylist"), phone, accent, true);
        }
        if (HudLayout.visible("arraylist_util")) {
            drawArrayList(context, mc, util, screenW, HudLayout.get("arraylist_util"), phone, accent, false);
        }

        // Target HUD position from layout
        Module th = JayHackClient.moduleManager.getModuleByName("TargetHUD");
        if (th != null && th.isEnabled() && HudLayout.visible("target")) {
            HudLayout.Element te = HudLayout.get("target");
            int tx = HudLayout.resolveX(te, screenW);
            int ty = HudLayout.resolveY(te, 4);
            try {
                drawTargetAt(context, mc, tx, ty);
            } catch (Throwable ignored) {}
        }

        Module perf = JayHackClient.moduleManager.getModuleByName("PerfDashboard");
        if (perf instanceof PerfDashboard pd) {
            pd.renderOverlay(context, screenW);
        }

        Notifications.render(context);
    }

    private static Comparator<Module> listOrder(MinecraftClient mc) {
        return Comparator
                .comparing((Module m) -> !ClientSettings.isFavorite(m.getName()))
                .thenComparingInt(m -> -mc.textRenderer.getWidth(m.getName()));
    }

    private static void updateSlide(List<Module> enabled) {
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
            // only decay if not in either list — handled per call; skip aggressive remove here
            if (!still) {
                float v = e.getValue() - 0.12f;
                if (v <= 0) it.remove();
                else e.setValue(v);
            }
        }
    }

    private static void drawArrayList(DrawContext context, MinecraftClient mc, List<Module> list,
                                      int screenW, HudLayout.Element el, boolean phone, int accent,
                                      boolean combat) {
        int maxList = phone ? (combat ? 6 : 5) : 16;
        int y = HudLayout.resolveY(el, combat ? 28 : 120);
        int baseRight = el != null && el.x < 0 ? screenW + (int) el.x : screenW - 8;
        int shown = 0;
        for (Module m : list) {
            if (shown >= maxList) break;
            float s = slide.getOrDefault(m.getName(), 1f);
            String star = ClientSettings.isFavorite(m.getName()) ? "§e★ " : "";
            String label = star + m.getName();
            int textW = mc.textRenderer.getWidth(m.getName()) + (ClientSettings.isFavorite(m.getName()) ? 12 : 0);
            int slidePx = (int) ((1f - s) * (textW + 16));
            int x = baseRight - textW - 4 + slidePx;
            int rowAccent = ClientSettings.arrayListRainbow ? accent : m.getCategoryColor();
            context.fill(x - 5, y - 1, baseRight, y + 10, 0x990A0A10);
            context.fill(baseRight - 2, y - 1, baseRight, y + 10, rowAccent);
            context.drawTextWithShadow(mc.textRenderer, label, x, y, 0xE8E8F0);
            y += 11;
            shown++;
        }
    }

    private static void drawTargetAt(DrawContext context, MinecraftClient mc, int bx, int by) {
        if (TargetHUD.currentName == null || TargetHUD.currentName.isEmpty()) return;
        String name = TargetHUD.currentName;
        float hp = TargetHUD.currentHealth;
        float max = Math.max(1f, TargetHUD.currentMaxHealth);
        float pct = Math.max(0f, Math.min(1f, hp / max));

        int boxW = 120;
        int boxH = 28;
        context.fill(bx, by, bx + boxW, by + boxH, 0x990A0A10);
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

    private static String strip(String s) {
        return s.replace("§b", "").replace("§f", "").replace("§8", "").replace("§7", "").replace("§e", "");
    }
}
