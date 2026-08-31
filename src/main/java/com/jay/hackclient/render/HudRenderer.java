package com.jay.hackclient.render;

import com.jay.hackclient.JayHackClient;
import com.jay.hackclient.compat.BaritoneCompat;
import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.modules.InfoHUD;
import com.jay.hackclient.module.modules.PearlTrajectory;
import com.jay.hackclient.module.modules.PerfDashboard;
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
                    ? ("§bJAY §f" + ver + " §8· §7" + active)
                    : ("§bJAY §f" + ver);
            int ww = mc.textRenderer.getWidth(strip(watermark)) + 14;
            context.fill(wx, wy, wx + ww, wy + 24, 0x990A0A10);
            context.fill(wx, wy, wx + 2, wy + 24, accent);
            context.drawTextWithShadow(mc.textRenderer, watermark, wx + 6, wy + 3, 0xFFFFFF);
            context.drawTextWithShadow(mc.textRenderer, "§8" + ClientSettings.lastProfile, wx + 6, wy + 14, 0x888888);
        }

        Module info = JayHackClient.moduleManager.getModuleByName("InfoHUD");
        if (info != null && info.isEnabled()) {
            if (InfoHUD.coords.get() && HudLayout.visible("coords")) {
                HudLayout.Element e = HudLayout.get("coords");
                int x = HudLayout.resolveX(e, screenW);
                int y = HudLayout.resolveY(e, 52);
                String c = String.format("§7%d %d %d",
                        (int) mc.player.getX(), (int) mc.player.getY(), (int) mc.player.getZ());
                context.drawTextWithShadow(mc.textRenderer, c, x, y, 0xCCCCCC);
            }
        }

        Module reach = JayHackClient.moduleManager.getModuleByName("ReachHUD");
        if (reach != null && reach.isEnabled()) {
            try { ReachHUD.draw(context); } catch (Throwable ignored) {}
        }

        Module perf = JayHackClient.moduleManager.getModuleByName("PerfDashboard");
        if (perf != null && perf.isEnabled() && HudLayout.visible("perf")) {
            try { PerfDashboard.draw(context); } catch (Throwable ignored) {}
        }

        Module pearl = JayHackClient.moduleManager.getModuleByName("PearlTrajectory");
        if (pearl != null && pearl.isEnabled()) {
            try { PearlTrajectory.drawHud(context, screenW, screenH); } catch (Throwable ignored) {}
        }

        try { Notifications.render(context); } catch (Throwable ignored) {}

        try {
            if (BaritoneCompat.isPathing()) {
                context.drawTextWithShadow(mc.textRenderer, "§bBaritone §7pathing",
                        4, screenH - 14, 0xAAAAAA);
            }
        } catch (Throwable ignored) {}

        List<Module> combat = new ArrayList<>();
        List<Module> util = new ArrayList<>();
        for (Module m : JayHackClient.moduleManager.getActive()) {
            if (!m.isDrawn()) continue;
            if (m.getCategory() == Module.Category.COMBAT || m.getCategory() == Module.Category.ANARCHY) {
                combat.add(m);
            } else {
                util.add(m);
            }
        }
        sortModules(combat);
        sortModules(util);

        if (HudLayout.visible("arraylist")) {
            drawArrayList(context, mc, combat, screenW, HudLayout.get("arraylist"), phone, accent);
        }
        if (HudLayout.visible("arraylist_util")) {
            drawArrayList(context, mc, util, screenW, HudLayout.get("arraylist_util"), phone, accent);
        }

        Module th = JayHackClient.moduleManager.getModuleByName("TargetHUD");
        if (th != null && th.isEnabled() && HudLayout.visible("target")) {
            HudLayout.Element te = HudLayout.get("target");
            drawTargetAt(context, mc, HudLayout.resolveX(te, screenW), HudLayout.resolveY(te, 4));
        }
    }

    private static void sortModules(List<Module> list) {
        list.sort(Comparator
                .comparing((Module m) -> !ClientSettings.isFavorite(m.getName()))
                .thenComparing(m -> m.getName().toLowerCase()));
    }

    private static void drawArrayList(DrawContext context, MinecraftClient mc, List<Module> list,
                                      int screenW, HudLayout.Element el, boolean phone, int accent) {
        if (list == null || list.isEmpty() || el == null) return;
        int y = HudLayout.resolveY(el, 28);
        int baseRight = HudLayout.resolveX(el, screenW);
        if (el.x >= 0) baseRight = el.x > 0 ? (int) el.x + 80 : screenW - 4;

        List<Module> a = new ArrayList<>(list);
        List<Module> b = new ArrayList<>();
        for (Module m : a) slide.put(m.getName(), Math.min(1f, slide.getOrDefault(m.getName(), 0f) + 0.18f));
        for (Module m : b) slide.put(m.getName(), Math.min(1f, slide.getOrDefault(m.getName(), 0f) + 0.18f));
        Iterator<Map.Entry<String, Float>> it = slide.entrySet().iterator();
        while (it.hasNext()) {
            Map.Entry<String, Float> e = it.next();
            boolean still = false;
            for (Module m : list) if (m.getName().equals(e.getKey())) { still = true; break; }
            if (!still) {
                float v = e.getValue() - 0.2f;
                if (v <= 0) it.remove();
                else e.setValue(v);
            }
        }

        int shown = 0;
        int max = phone ? 8 : 14;
        for (Module m : list) {
            if (shown >= max) break;
            float s = slide.getOrDefault(m.getName(), 1f);
            String star = ClientSettings.isFavorite(m.getName()) ? "§e★ " : "";
            String label = star + m.getName();
            int textW = mc.textRenderer.getWidth(m.getName()) + (ClientSettings.isFavorite(m.getName()) ? 12 : 0);
            int slidePx = (int) ((1f - s) * (textW + 16));
            int x = baseRight - textW - 4 + slidePx;
            int rowAccent = ClientSettings.arrayListRainbow ? accent : m.getCategoryColor();
            context.fill(baseRight - 2, y - 1, baseRight, y + 10, rowAccent);
            context.drawTextWithShadow(mc.textRenderer, label, x, y, 0xE8E8F0);
            y += 11;
            shown++;
        }
    }

    private static void drawTargetAt(DrawContext context, MinecraftClient mc, int bx, int by) {
        PlayerEntity t = TargetHUD.currentTarget;
        if (t == null || !t.isAlive()) return;

        float hp = TargetHUD.currentHp > 0 ? TargetHUD.currentHp : (t.getHealth() + t.getAbsorptionAmount());
        float max = TargetHUD.currentMaxHp > 0 ? TargetHUD.currentMaxHp : Math.max(1f, t.getMaxHealth());
        float pct = Math.max(0f, Math.min(1f, hp / max));
        int armor = TargetHUD.armorPoints;

        int boxW = 128;
        int boxH = 36;
        context.fill(bx, by, bx + boxW, by + boxH, 0x990A0A10);
        context.fill(bx, by, bx + 2, by + boxH, CYAN);

        String name = t.getName().getString();
        String info = String.format("%s  §7%.1fm", name, TargetHUD.currentDistance);
        context.drawTextWithShadow(mc.textRenderer, info, bx + 8, by + 4, 0xFFF0F0F8);

        String hpText = String.format("§f%.1f §8| §7A%d", hp, armor);
        context.drawTextWithShadow(mc.textRenderer, hpText, bx + 8, by + 14, CYAN);

        int barW = boxW - 16;
        int barY = by + 26;
        context.fill(bx + 8, barY, bx + 8 + barW, barY + 5, 0xFF1A1A24);
        int fill = Math.max(1, (int) (barW * pct));
        int col = pct > 0.5f ? 0xFF3DDCFF : (pct > 0.25f ? 0xFFCCAA33 : 0xFFCC4444);
        context.fill(bx + 8, barY, bx + 8 + fill, barY + 5, col);
    }

    private static String strip(String s) {
        return s.replace("§b", "").replace("§f", "").replace("§8", "").replace("§7", "").replace("§e", "");
    }
}
