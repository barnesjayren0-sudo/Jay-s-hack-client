package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.BoolSetting;
import com.jay.hackclient.render.HudLayout;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;

/** Android-friendly performance overlay. */
public class PerfDashboard extends Module {

    private final BoolSetting showFps = new BoolSetting("FPS", "Show FPS", true);
    private final BoolSetting showMem = new BoolSetting("Memory", "Show memory", true);
    private final BoolSetting showEnt = new BoolSetting("Entities", "Show entity count", true);
    private final BoolSetting showPing = new BoolSetting("Ping", "Show ping", true);

    public PerfDashboard() {
        super("PerfDashboard", "FPS, memory, entities, ping overlay", Category.RENDER);
        addSetting(showFps);
        addSetting(showMem);
        addSetting(showEnt);
        addSetting(showPing);
    }

    public void renderOverlay(DrawContext ctx, int screenW) {
        if (!isEnabled() || mc.player == null) return;
        HudLayout.Element el = HudLayout.get("perf");
        int x = el != null ? (el.x < 0 ? screenW + (int) el.x : (int) el.x) : 4;
        int y = el != null ? (int) el.y : 200;
        if (el != null && !el.visible) return;

        int line = y;
        var tr = mc.textRenderer;

        if (showFps.get()) {
            ctx.drawTextWithShadow(tr, "FPS §f" + mc.getCurrentFps(), x, line, 0xFF3DDCFF);
            line += 10;
        }
        if (showMem.get()) {
            Runtime rt = Runtime.getRuntime();
            long used = (rt.totalMemory() - rt.freeMemory()) / (1024 * 1024);
            long max = rt.maxMemory() / (1024 * 1024);
            ctx.drawTextWithShadow(tr, "Mem §f" + used + "/" + max + "M", x, line, 0xFFAAAAAA);
            line += 10;
        }
        if (showEnt.get() && mc.world != null) {
            int count = 0;
            for (Entity ignored : mc.world.getEntities()) count++;
            ctx.drawTextWithShadow(tr, "Ent §f" + count, x, line, 0xFFAAAAAA);
            line += 10;
        }
        if (showPing.get() && mc.getNetworkHandler() != null) {
            var pe = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
            int ping = pe != null ? pe.getLatency() : 0;
            ctx.drawTextWithShadow(tr, "Ping §f" + ping, x, line, 0xFFAAAAAA);
        }
    }

    /** Static entry used by HudRenderer. */
    public static void draw(DrawContext ctx) {
        if (ctx == null) return;
        var client = net.minecraft.client.MinecraftClient.getInstance();
        if (client == null) return;
        try {
            if (com.jay.hackclient.JayHackClient.moduleManager == null) return;
            Module m = com.jay.hackclient.JayHackClient.moduleManager.getModuleByName("PerfDashboard");
            if (m instanceof PerfDashboard pd && pd.isEnabled()) {
                pd.renderOverlay(ctx, client.getWindow().getScaledWidth());
            }
        } catch (Throwable ignored) {}
    }
}
