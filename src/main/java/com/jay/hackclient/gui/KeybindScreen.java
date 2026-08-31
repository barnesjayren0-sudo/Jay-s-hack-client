package com.jay.hackclient.gui;

import com.jay.hackclient.JayHackClient;
import com.jay.hackclient.module.Module;
import com.jay.hackclient.util.Notifications;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/** Dedicated keybind manager with conflict detection. */
public class KeybindScreen extends Screen {

    private final Screen parent;
    private Module listening;
    private int scroll;
    private String filter = "";

    public KeybindScreen(Screen parent) {
        super(Text.literal("Keybinds"));
        this.parent = parent;
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, width, height, GuiTheme.OVERLAY);
        int w = Math.min(320, width - 20);
        int h = Math.min(height - 40, 280);
        int x = (width - w) / 2;
        int y = (height - h) / 2;
        ctx.fill(x, y, x + w, y + h, GuiTheme.BG);
        ctx.drawTextWithShadow(textRenderer, "§bKeybind Manager", x + 8, y + 6, GuiTheme.TEXT);
        ctx.drawTextWithShadow(textRenderer, "§7Click module → press key · Del clears · Esc back",
                x + 8, y + 18, GuiTheme.TEXT_DIM);

        Map<Integer, List<String>> conflicts = conflicts();
        List<Module> mods = filtered();
        int rowH = 14;
        int maxRows = (h - 40) / rowH;
        for (int i = 0; i < maxRows && scroll + i < mods.size(); i++) {
            Module m = mods.get(scroll + i);
            int ry = y + 34 + i * rowH;
            boolean hover = mouseX >= x && mouseX < x + w && mouseY >= ry && mouseY < ry + rowH;
            if (hover || m == listening) ctx.fill(x + 2, ry, x + w - 2, ry + rowH, GuiTheme.ROW_HOVER);
            String key = m.getKeyBind() < 0 ? "None" : m.getKeyLabel();
            boolean conflict = m.getKeyBind() >= 0 && conflicts.getOrDefault(m.getKeyBind(), List.of()).size() > 1;
            int kc = conflict ? GuiTheme.DANGER : (m == listening ? GuiTheme.ACCENT : GuiTheme.TEXT_DIM);
            ctx.drawTextWithShadow(textRenderer, m.getName(), x + 8, ry + 3, GuiTheme.TEXT);
            ctx.drawTextWithShadow(textRenderer, m == listening ? "§e..." : key,
                    x + w - 8 - textRenderer.getWidth(m == listening ? "..." : key), ry + 3, kc);
        }
    }

    private List<Module> filtered() {
        List<Module> out = new ArrayList<>();
        if (JayHackClient.moduleManager == null) return out;
        String q = filter.toLowerCase();
        for (Module m : JayHackClient.moduleManager.getModules()) {
            if (q.isEmpty() || m.getName().toLowerCase().contains(q)) out.add(m);
        }
        out.sort((a, b) -> a.getName().compareToIgnoreCase(b.getName()));
        return out;
    }

    private Map<Integer, List<String>> conflicts() {
        Map<Integer, List<String>> map = new HashMap<>();
        if (JayHackClient.moduleManager == null) return map;
        for (Module m : JayHackClient.moduleManager.getModules()) {
            if (m.getKeyBind() < 0) continue;
            map.computeIfAbsent(m.getKeyBind(), k -> new ArrayList<>()).add(m.getName());
        }
        return map;
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        if (click.button() != 0) return super.mouseClicked(click, doubled);
        int w = Math.min(320, width - 20);
        int h = Math.min(height - 40, 280);
        int x = (width - w) / 2;
        int y = (height - h) / 2;
        List<Module> mods = filtered();
        int rowH = 14;
        int maxRows = (h - 40) / rowH;
        for (int i = 0; i < maxRows && scroll + i < mods.size(); i++) {
            int ry = y + 34 + i * rowH;
            if (click.x() >= x && click.x() < x + w && click.y() >= ry && click.y() < ry + rowH) {
                listening = mods.get(scroll + i);
                return true;
            }
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseScrolled(double mx, double my, double h, double v) {
        scroll = Math.max(0, scroll - (int) Math.signum(v) * 2);
        return true;
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        int key = input.key();
        if (listening != null) {
            if (key == GLFW.GLFW_KEY_ESCAPE) { listening = null; return true; }
            if (key == GLFW.GLFW_KEY_DELETE || key == GLFW.GLFW_KEY_BACKSPACE) {
                listening.setKeyBind(-1);
                Notifications.push("Keybind", listening.getName() + " unbound");
                listening = null;
                if (JayHackClient.configManager != null) JayHackClient.configManager.save();
                return true;
            }
            listening.setKeyBind(key);
            Notifications.push("Keybind", listening.getName() + " → " + listening.getKeyLabel());
            listening = null;
            if (JayHackClient.configManager != null) JayHackClient.configManager.save();
            return true;
        }
        if (key == GLFW.GLFW_KEY_ESCAPE) {
            client.setScreen(parent);
            return true;
        }
        return super.keyPressed(input);
    }

    @Override
    public boolean shouldPause() { return false; }
}
