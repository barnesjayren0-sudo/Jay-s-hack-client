package com.jay.client18.gui;

import com.jay.client18.JayClient18;
import com.jay.client18.module.Module;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ClickGuiScreen extends GuiScreen {

    private final List<Panel> panels = new ArrayList<Panel>();

    public ClickGuiScreen() {
        int x = 20;
        for (Module.Category c : Module.Category.values()) {
            panels.add(new Panel(c, x, 20));
            x += 110;
        }
    }

    @Override
    public void drawScreen(int mouseX, int mouseY, float partialTicks) {
        drawDefaultBackground();
        for (Panel p : panels) p.draw(mouseX, mouseY);
        super.drawScreen(mouseX, mouseY, partialTicks);
    }

    @Override
    protected void mouseClicked(int mouseX, int mouseY, int mouseButton) throws IOException {
        for (Panel p : panels) p.mouseClicked(mouseX, mouseY, mouseButton);
        super.mouseClicked(mouseX, mouseY, mouseButton);
    }

    @Override
    public void onGuiClosed() {
        if (JayClient18.configManager != null) JayClient18.configManager.save();
    }

    @Override
    public boolean doesGuiPauseGame() {
        return false;
    }

    private static class Panel {
        final Module.Category category;
        int x, y;
        boolean open = true;

        Panel(Module.Category category, int x, int y) {
            this.category = category;
            this.x = x;
            this.y = y;
        }

        void draw(int mx, int my) {
            int w = 100;
            int h = 14;
            drawRect(x, y, x + w, y + h, 0xDD0B0D12);
            MinecraftFont(x + 4, y + 3, "§b" + category.display);

            if (!open) return;
            int cy = y + h;
            List<Module> mods = JayClient18.moduleManager.getByCategory(category);
            for (Module m : mods) {
                int col = m.isEnabled() ? 0xDD123D3D : 0xDD12151C;
                drawRect(x, cy, x + w, cy + 12, col);
                String label = (m.isEnabled() ? "§a" : "§7") + m.getName();
                MinecraftFont(x + 4, cy + 2, label);
                cy += 12;
            }
        }

        void mouseClicked(int mx, int my, int btn) {
            int w = 100;
            if (mx >= x && mx <= x + w && my >= y && my <= y + 14) {
                if (btn == 1) open = !open;
                return;
            }
            if (!open) return;
            int cy = y + 14;
            for (Module m : JayClient18.moduleManager.getByCategory(category)) {
                if (mx >= x && mx <= x + w && my >= cy && my <= cy + 12) {
                    if (btn == 0) m.toggle();
                    return;
                }
                cy += 12;
            }
        }

        private void MinecraftFont(int x, int y, String s) {
            net.minecraft.client.Minecraft.getMinecraft().fontRendererObj.drawStringWithShadow(s, x, y, 0xFFFFFF);
        }

        private void drawRect(int l, int t, int r, int b, int color) {
            GuiScreen.drawRect(l, t, r, b, color);
        }
    }
}
