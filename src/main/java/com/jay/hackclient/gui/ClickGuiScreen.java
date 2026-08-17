package com.jay.hackclient.gui;

import com.jay.hackclient.JayHackClient;
import com.jay.hackclient.module.Module;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.gui.widget.TextFieldWidget;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Responsive Vape-style GUI for PC + PoJav / Mojo (phones like Redmi A5).
 * Uses nearly full screen on small displays, search bar to filter modules.
 */
public class ClickGuiScreen extends Screen {

    private static final int ACCENT = 0xFFB24BF3;
    private static final int ACCENT_DIM = 0xFF6B2A9A;
    private static final int BG_OVERLAY = 0xCC000000;
    private static final int BG_WINDOW = 0xF0121218;
    private static final int BG_SIDEBAR = 0xF00C0C10;
    private static final int BG_HEADER = 0xF0161620;
    private static final int TEXT = 0xFFE8E8F0;
    private static final int TEXT_DIM = 0xFF8888A0;
    private static final int ROW_HOVER = 0x18FFFFFF;
    private static final int ROW_ON = 0x22B24BF3;
    private static final int TOGGLE_ON = 0xFFB24BF3;
    private static final int TOGGLE_OFF = 0xFF333344;

    private Module.Category selected = Module.Category.COMBAT;
    private int scroll;
    private TextFieldWidget searchField;
    private String search = "";

    // Layout computed each frame from screen size
    private int winX, winY, winW, winH;
    private int sidebarW;
    private int headerH = 28;
    private int searchH = 22;
    private int rowH = 24; // larger for touch

    public ClickGuiScreen() {
        super(Text.literal("Jay"));
        for (Module.Category c : Module.Category.values()) {
            if (JayHackClient.moduleManager != null
                    && !JayHackClient.moduleManager.getByCategory(c).isEmpty()) {
                selected = c;
                break;
            }
        }
    }

    private void computeLayout() {
        // Near full-screen on phones; slight margin on large PC screens
        boolean small = this.width < 500 || this.height < 320;
        if (small) {
            // PoJav / Redmi-class: fill almost entire screen
            winW = Math.max(this.width - 8, 200);
            winH = Math.max(this.height - 8, 180);
            winX = (this.width - winW) / 2;
            winY = (this.height - winH) / 2;
            sidebarW = Math.max(72, winW / 4);
            rowH = 28;
            headerH = 30;
            searchH = 24;
        } else {
            winW = Math.min(460, this.width - 40);
            winH = Math.min(300, this.height - 40);
            winX = (this.width - winW) / 2;
            winY = (this.height - winH) / 2;
            sidebarW = 100;
            rowH = 22;
            headerH = 28;
            searchH = 22;
        }
    }

    @Override
    protected void init() {
        computeLayout();
        int sx = winX + sidebarW + 8;
        int sy = winY + headerH + 4;
        int sw = winW - sidebarW - 16;

        searchField = new TextFieldWidget(textRenderer, sx, sy, sw, searchH - 4, Text.literal("Search"));
        searchField.setMaxLength(32);
        searchField.setPlaceholder(Text.literal("Search modules..."));
        searchField.setText(search);
        searchField.setChangedListener(s -> {
            search = s == null ? "" : s;
            scroll = 0;
        });
        addSelectableChild(searchField);
        setInitialFocus(searchField);
    }

    private List<Module> filteredModules() {
        List<Module> base;
        if (search != null && !search.isBlank()) {
            base = new ArrayList<>();
            String q = search.toLowerCase(Locale.ROOT);
            for (Module m : JayHackClient.moduleManager.getModules()) {
                if (m.getName().toLowerCase(Locale.ROOT).contains(q)
                        || m.getDescription().toLowerCase(Locale.ROOT).contains(q)
                        || m.getCategory().displayName.toLowerCase(Locale.ROOT).contains(q)) {
                    base.add(m);
                }
            }
        } else {
            base = new ArrayList<>(JayHackClient.moduleManager.getByCategory(selected));
        }
        return base;
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        computeLayout();

        // Keep search field positioned if window size changes (rotation etc.)
        if (searchField != null) {
            searchField.setX(winX + sidebarW + 8);
            searchField.setY(winY + headerH + 4);
            searchField.setWidth(winW - sidebarW - 16);
        }

        ctx.fill(0, 0, this.width, this.height, BG_OVERLAY);

        int x = winX;
        int y = winY;

        ctx.fill(x + 3, y + 3, x + winW + 3, y + winH + 3, 0x55000000);
        ctx.fill(x, y, x + winW, y + winH, BG_WINDOW);

        // Header
        ctx.fill(x, y, x + winW, y + headerH, BG_HEADER);
        ctx.fill(x, y + headerH - 1, x + winW, y + headerH, ACCENT_DIM);
        ctx.drawTextWithShadow(textRenderer, "§dJ§fay", x + 8, y + 10, TEXT);
        ctx.drawTextWithShadow(textRenderer, "§8v" + JayHackClient.VERSION, x + 34, y + 10, TEXT_DIM);

        String status = JayHackClient.moduleManager.isFrozen() ? "§cFROZEN" : "§aREADY";
        ctx.drawTextWithShadow(textRenderer, status, x + winW - 78, y + 10, TEXT);

        // Close
        int closeX = x + winW - 24;
        int closeY = y + 6;
        boolean hoverX = mouseX >= closeX && mouseX <= closeX + 18 && mouseY >= closeY && mouseY <= closeY + 18;
        ctx.fill(closeX, closeY, closeX + 18, closeY + 18, hoverX ? 0xFFAA3333 : 0x22FFFFFF);
        ctx.drawCenteredTextWithShadow(textRenderer, "x", closeX + 9, closeY + 5, 0xFFFFFF);

        // Sidebar
        ctx.fill(x, y + headerH, x + sidebarW, y + winH, BG_SIDEBAR);
        ctx.fill(x + sidebarW - 1, y + headerH, x + sidebarW, y + winH, 0x22FFFFFF);

        int catY = y + headerH + 6;
        for (Module.Category cat : Module.Category.values()) {
            if (JayHackClient.moduleManager.getByCategory(cat).isEmpty()) continue;
            boolean sel = cat == selected && (search == null || search.isBlank());
            boolean hover = mouseX >= x && mouseX < x + sidebarW && mouseY >= catY && mouseY < catY + 22;

            if (sel) {
                ctx.fill(x, catY, x + sidebarW, catY + 22, 0x33B24BF3);
                ctx.fill(x, catY, x + 3, catY + 22, ACCENT);
            } else if (hover) {
                ctx.fill(x, catY, x + sidebarW, catY + 22, 0x15FFFFFF);
            }

            ctx.drawTextWithShadow(textRenderer, cat.displayName, x + 8, catY + 7,
                    sel ? ACCENT : (hover ? TEXT : TEXT_DIM));
            catY += 24;
        }

        // Search field
        if (searchField != null) {
            searchField.render(ctx, mouseX, mouseY, delta);
        }

        // Modules
        List<Module> modules = filteredModules();
        int listX = x + sidebarW;
        int rowTop = y + headerH + searchH + 8;
        int listBottom = y + winH - 16;
        int maxRows = Math.max(1, (listBottom - rowTop) / rowH);

        if (!(search == null || search.isBlank())) {
            ctx.drawTextWithShadow(textRenderer, "Results",
                    listX + 10, y + headerH + searchH + 2, TEXT_DIM);
        } else {
            ctx.drawTextWithShadow(textRenderer, selected.displayName.toUpperCase(Locale.ROOT),
                    listX + 10, y + headerH + searchH + 2, TEXT_DIM);
        }

        for (int i = 0; i < modules.size(); i++) {
            if (i < scroll) continue;
            int di = i - scroll;
            if (di >= maxRows) break;

            Module m = modules.get(i);
            int ry = rowTop + di * rowH;
            boolean hover = mouseX >= listX && mouseX < x + winW && mouseY >= ry && mouseY < ry + rowH;

            if (m.isEnabled()) {
                ctx.fill(listX + 4, ry, x + winW - 4, ry + rowH - 2, ROW_ON);
            } else if (hover) {
                ctx.fill(listX + 4, ry, x + winW - 4, ry + rowH - 2, ROW_HOVER);
            }

            ctx.drawTextWithShadow(textRenderer, m.getName(), listX + 12, ry + (rowH / 2) - 4,
                    m.isEnabled() ? TEXT : TEXT_DIM);

            // Larger pill for touch
            int tw = 26;
            int th = 14;
            int tx = x + winW - 14 - tw;
            int ty = ry + (rowH - th) / 2;
            ctx.fill(tx, ty, tx + tw, ty + th, m.isEnabled() ? TOGGLE_ON : TOGGLE_OFF);
            int knob = m.isEnabled() ? tx + tw - 13 : tx + 2;
            ctx.fill(knob, ty + 2, knob + 11, ty + th - 2, 0xFFF0F0F8);
        }

        ctx.drawTextWithShadow(textRenderer, "ESC / RShift close · type to search",
                x + 8, y + winH - 12, 0xFF555566);

        super.render(ctx, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return super.mouseClicked(mouseX, mouseY, button);

        computeLayout();
        int x = winX;
        int y = winY;

        // Close
        int closeX = x + winW - 24;
        int closeY = y + 6;
        if (mouseX >= closeX && mouseX <= closeX + 18 && mouseY >= closeY && mouseY <= closeY + 18) {
            close();
            return true;
        }

        // Categories
        int catY = y + headerH + 6;
        for (Module.Category cat : Module.Category.values()) {
            if (JayHackClient.moduleManager.getByCategory(cat).isEmpty()) continue;
            if (mouseX >= x && mouseX < x + sidebarW && mouseY >= catY && mouseY < catY + 22) {
                selected = cat;
                search = "";
                if (searchField != null) searchField.setText("");
                scroll = 0;
                return true;
            }
            catY += 24;
        }

        // Search field click
        if (searchField != null && searchField.mouseClicked(mouseX, mouseY, button)) {
            setFocused(searchField);
            return true;
        }

        // Modules
        List<Module> modules = filteredModules();
        int listX = x + sidebarW;
        int rowTop = y + headerH + searchH + 8;
        int listBottom = y + winH - 16;
        int maxRows = Math.max(1, (listBottom - rowTop) / rowH);

        for (int i = 0; i < modules.size(); i++) {
            if (i < scroll) continue;
            int di = i - scroll;
            if (di >= maxRows) break;
            int ry = rowTop + di * rowH;
            if (mouseX >= listX && mouseX < x + winW && mouseY >= ry && mouseY < ry + rowH) {
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
        List<Module> modules = filteredModules();
        computeLayout();
        int rowTop = winY + headerH + searchH + 8;
        int listBottom = winY + winH - 16;
        int maxRows = Math.max(1, (listBottom - rowTop) / rowH);
        int maxScroll = Math.max(0, modules.size() - maxRows);
        if (verticalAmount > 0) scroll = Math.max(0, scroll - 1);
        else if (verticalAmount < 0) scroll = Math.min(maxScroll, scroll + 1);
        return true;
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            close();
            return true;
        }
        // Only close on RShift if search is not focused
        if (keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT && (searchField == null || !searchField.isFocused())) {
            close();
            return true;
        }
        if (searchField != null && searchField.keyPressed(keyCode, scanCode, modifiers)) {
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public boolean charTyped(char chr, int modifiers) {
        if (searchField != null && searchField.charTyped(chr, modifiers)) {
            return true;
        }
        return super.charTyped(chr, modifiers);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
