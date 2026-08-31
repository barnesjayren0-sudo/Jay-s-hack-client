package com.jay.hackclient.gui;

import com.jay.hackclient.JayHackClient;
import com.jay.hackclient.module.Module;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.entity.Entity;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

/** Developer debug overlay — active modules, FPS, errors, versions. */
public class DebugScreen extends Screen {

    private final Screen parent;

    public DebugScreen(Screen parent) {
        super(Text.literal("Debug"));
        this.parent = parent;
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, width, height, GuiTheme.OVERLAY);
        int x = 12, y = 12;
        ctx.drawTextWithShadow(textRenderer, "§bJay Debug §7· Esc close", x, y, GuiTheme.TEXT);
        y += 14;

        var mc = client;
        int fps = mc.getCurrentFps();
        Runtime rt = Runtime.getRuntime();
        long used = (rt.totalMemory() - rt.freeMemory()) / (1024 * 1024);
        long max = rt.maxMemory() / (1024 * 1024);

        y = line(ctx, x, y, "FPS", String.valueOf(fps));
        y = line(ctx, x, y, "Memory", used + " / " + max + " MB");
        y = line(ctx, x, y, "Client", JayHackClient.VERSION);
        y = line(ctx, x, y, "MC", mc.getGameVersion());
        y = line(ctx, x, y, "Loader", FabricLoader.getInstance().getModContainer("fabricloader")
                .map(c -> c.getMetadata().getVersion().getFriendlyString()).orElse("?"));
        y = line(ctx, x, y, "Mods", String.valueOf(FabricLoader.getInstance().getAllMods().size()));

        if (mc.world != null) {
            int count = 0;
            for (Entity ignored : mc.world.getEntities()) count++;
            y = line(ctx, x, y, "Entities", String.valueOf(count));
        }
        if (mc.getNetworkHandler() != null && mc.player != null) {
            var pe = mc.getNetworkHandler().getPlayerListEntry(mc.player.getUuid());
            int ping = pe != null ? pe.getLatency() : -1;
            y = line(ctx, x, y, "Ping", ping + " ms");
            String addr = mc.getCurrentServerEntry() != null
                    ? mc.getCurrentServerEntry().address : "singleplayer";
            y = line(ctx, x, y, "Server", addr);
        }

        y += 6;
        ctx.drawTextWithShadow(textRenderer, "§fActive modules", x, y, GuiTheme.ACCENT);
        y += 12;
        if (JayHackClient.moduleManager != null) {
            for (Module m : JayHackClient.moduleManager.getActive()) {
                String err = m.getRuntimeErrorStreak() > 0 ? " §cerr=" + m.getRuntimeErrorStreak() : "";
                ctx.drawTextWithShadow(textRenderer, "§7- §f" + m.getName() + err, x, y, GuiTheme.TEXT);
                y += 10;
                if (y > height - 20) break;
            }
        }
    }

    private int line(DrawContext ctx, int x, int y, String k, String v) {
        ctx.drawTextWithShadow(textRenderer, "§7" + k + ": §f" + v, x, y, GuiTheme.TEXT);
        return y + 11;
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if (input.key() == GLFW.GLFW_KEY_ESCAPE) {
            client.setScreen(parent);
            return true;
        }
        return super.keyPressed(input);
    }

    @Override
    public boolean shouldPause() { return false; }
}
