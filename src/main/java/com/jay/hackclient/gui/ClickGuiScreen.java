package com.jay.hackclient.gui;

import com.jay.hackclient.JayHackClient;
import com.jay.hackclient.module.Module;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;

/**
 * Vape-inspired ClickGUI:
 * - Centered dark window
 * - Left category sidebar
 * - Module rows with pill toggles
 * - Accent purple line
 * - Closable (ESC / RShift / X)
 */
public class ClickGuiScreen extends Screen {

    // Layout
    private static final int WIN_W = 420;
    private static final int WIN_H = 280;
    private static final int SIDEBAR_W = 100;
    private static final int HEADER_H = 28;
    private static final int ROW_H = 22;

    // Colors (Vape-like dark + purple accent)
    private static final int BG_OVERLAY = 0xAA000000;
    private static final int BG_WINDOW = 0xF0121218;
    private static final int BG_SIDEBAR = 0xF00C0C10;
    private static final int BG_HEADER = 0xF0161620;
    private static final int ACCENT = 0xFFB24BF3;      // purple
    private static final int ACCENT_DIM = 0xFF6B2A9A;
    private static final int TEXT = 0xFFE8E8F0;
    private static final int TEXT_DIM = 0xFF8888A0;
    private static final int ROW_HOVER = 0x18FFFFFF;
    private static final int ROW_ON = 0x22B24BF3;
    private static final int TOGGLE_ON = 0xFFB24BF3;
    private static final int TOGGLE_OFF = 0xFF333344;

    private Module.Category selected = Module.Category.COMBAT;
    private int scroll;

    public ClickGuiScreen() {
        super(Text.literal("Jay"));
        // Pick first category that has modules
        for (Module.Category c : Module.Category.values()) {
            if (!JayHackClient.moduleManager.getByCategory(c).isEmpty()) {
                selected = c;
                break;
            }
        }
    }

    private int winX() { return (this.width - WIN_W) / 2; }
    private int winY() { return (this.height - WIN_H) / 2; }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        // Dim world
        ctx.fill(0, 0, this.width, this.height, BG_OVERLAY);

        int x = winX();
        int y = winY();

        // Window shadow
        ctx.fill(x + 4, y + 4, x + WIN_W + 4, y + WIN_H + 4, 0x55000000);

        // Main window
        ctx.fill(x, y, x + WIN_W, y + WIN_H, BG_WINDOW);

        // Header
        ctx.fill(x, y, x + WIN_W, y + HEADER_H, BG_HEADER);
        ctx.fill(x, y + HEADER_H - 1, x + WIN_W, y + HEADER_H, ACCENT_DIM);

        // Brand
        ctx.drawTextWithShadow(textRenderer, "§dJ§fay", x + 10, y + 10, TEXT);
        ctx.drawTextWithShadow(textRenderer, "§8v" + JayHackClient.VERSION, x + 36, y + 10, TEXT_DIM);

        // Status
        String status = JayHackClient.moduleManager.isFrozen() ? "§cFROZEN" : "§aREADY";
        ctx.drawTextWithShadow(textRenderer, status, x + WIN_W - 70, y + 10, TEXT);

        // Close X
        int closeX = x + WIN_W - 22;
        int closeY = y + 6;
        boolean hoverX = mouseX >= closeX && mouseX <= closeX + 16 && mouseY >= closeY && mouseY <= closeY + 16;
        ctx.fill(closeX, closeY, closeX + 16, closeY + 16, hoverX ? 0xFFAA3333 : 0x00000000);
        ctx.drawCenteredTextWithShadow(textRenderer, "x", closeX + 8, closeY + 4, hoverX ? 0xFFFFFF : TEXT_DIM);

        // Sidebar
        ctx.fill(x, y + HEADER_H, x + SIDEBAR_W, y + WIN_H, BG_SIDEBAR);
        ctx.fill(x + SIDEBAR_W - 1, y + HEADER_H, x + SIDEBAR_W, y + WIN_H, 0x22FFFFFF);

        int catY = y + HEADER_H + 8;
        for (Module.Category cat : Module.Category.values()) {
            List<Module> mods = JayHackClient.moduleManager.getByCategory(cat);
            if (mods.isEmpty()) continue;

            boolean sel = cat == selected;
            boolean hover = mouseX >= x && mouseX < x + SIDEBAR_W
                    && mouseY >= catY && mouseY < catY + 18;

            if (sel) {
                ctx.fill(x, catY, x + SIDEBAR_W, catY + 18, 0x33B24BF3);
                ctx.fill(x, catY, x + 2, catY + 18, ACCENT);
            } else if (hover) {
                ctx.fill(x, catY, x + SIDEBAR_W, catY + 18, 0x15FFFFFF);
            }

            int col = sel ? ACCENT : (hover ? TEXT : TEXT_DIM);
            ctx.drawTextWithShadow(textRenderer, cat.displayName, x + 10, catY + 5, col);
            catY += 20;
        }

        // Module list area
        int listX = x + SIDEBAR_W;
        int listY = y + HEADER_H;
        int listW = WIN_W - SIDEBAR_W;
        int listH = WIN_H - HEADER_H;

        // Section title
        ctx.drawTextWithShadow(textRenderer, selected.displayName.toUpperCase(),
                listX + 12, listY + 8, TEXT_DIM);

        List<Module> modules = new ArrayList<>(JayHackClient.moduleManager.getByCategory(selected));
        int rowTop = listY + 24;
        int maxRows = (listH - 28) / ROW_H;

        for (int i = 0; i < modules.size(); i++) {
            if (i < scroll) continue;
            int drawIndex = i - scroll;
            if (drawIndex >= maxRows) break;

            Module m = modules.get(i);
            int ry = rowTop + drawIndex * ROW_H;

            boolean hover = mouseX >= listX && mouseX < x + WIN_W
                    && mouseY >= ry && mouseY < ry + ROW_H;

            if (m.isEnabled()) {
                ctx.fill(listX + 4, ry, x + WIN_W - 4, ry + ROW_H - 1, ROW_ON);
            } else if (hover) {
                ctx.fill(listX + 4, ry, x + WIN_W - 4, ry + ROW_H - 1, ROW_HOVER);
            }

            // Name + description
            ctx.drawTextWithShadow(textRenderer, m.getName(), listX + 12, ry + 7, m.isEnabled() ? TEXT : TEXT_DIM);

            // Pill toggle (right side)
            int tx = x + WIN_W - 36;
            int ty = ry + 5;
            int tw = 22;
            int th = 12;
            ctx.fill(tx, ty, tx + tw, ty + th, m.isEnabled() ? TOGGLE_ON : TOGGLE_OFF);
            // knob
            int knob = m.isEnabled() ? tx + tw - 11 : tx + 1;
            ctx.fill(knob, ty + 1, knob + 10, ty + th - 1, 0xFFF0F0F8);
        }

        // Footer hint
        ctx.drawTextWithShadow(textRenderer, "ESC / RShift close",
                x + 8, y + WIN_H - 12, 0xFF555566);

        super.render(ctx, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return super.mouseClicked(mouseX, mouseY, button);

        int x = winX();
        int y = winY();

        // Close X
        int closeX = x + WIN_W - 22;
        int closeY = y + 6;
        if (mouseX >= closeX && mouseX <= closeX + 16 && mouseY >= closeY && mouseY <= closeY + 16) {
            close();
            return true;
        }

        // Sidebar categories
        int catY = y + HEADER_H + 8;
        for (Module.Category cat : Module.Category.values()) {
            List<Module> mods = JayHackClient.moduleManager.getByCategory(cat);
            if (mods.isEmpty()) continue;
            if (mouseX >= x && mouseX < x + SIDEBAR_W && mouseY >= catY && mouseY < catY + 18) {
                selected = cat;
                scroll = 0;
                return true;
            }
            catY += 20;
        }

        // Module rows
        List<Module> modules = JayHackClient.moduleManager.getByCategory(selected);
        int listX = x + SIDEBAR_W;
        int rowTop = y + HEADER_H + 24;
        int maxRows = (WIN_H - HEADER_H - 28) / ROW_H;

        for (int i = 0; i < modules.size(); i++) {
            if (i < scroll) continue;
            int drawIndex = i - scroll;
            if (drawIndex >= maxRows) break;

            int ry = rowTop + drawIndex * ROW_H;
            if (mouseX >= listX && mouseX < x + WIN_W && mouseY >= ry && mouseY < ry + ROW_H) {
                if (JayHackClient.moduleManager.isFrozen()) {
                    if (client != null && client.player != null) {
                        client.player.sendMessage(Text.literal("§8[§bJay§8] §cUnpanic first"), false);
                    }
                } else {
                    modules.get(i).toggle();
                }
                return true;
            }
        }

        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        List<Module> modules = JayHackClient.moduleManager.getByCategory(selected);
        int maxRows = (WIN_H - HEADER_H - 28) / ROW_H;
        int maxScroll = Math.max(0, modules.size() - maxRows);
        if (verticalAmount > 0) scroll = Math.max(0, scroll - 1);
        else if (verticalAmount < 0) scroll = Math.min(maxScroll, scroll + 1);
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE || keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT) {
            close();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
