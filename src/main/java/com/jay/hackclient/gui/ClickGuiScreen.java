package com.jay.hackclient.gui;

import com.jay.hackclient.JayHackClient;
import com.jay.hackclient.module.Module;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Vape-style GUI — modules + Friends tab.
 * LMB toggle, RMB settings, Friends: LMB remove, type name + Enter to add.
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
    private boolean friendsTab = false;
    private int scroll;
    private String search = "";
    private boolean searchFocused = false;
    private String friendInput = "";

    private int winX, winY, winW, winH;
    private int sidebarW;
    private int headerH = 28;
    private int searchH = 22;
    private int rowH = 24;

    public ClickGuiScreen() {
        super(Text.literal("Jay"));
        if (JayHackClient.moduleManager != null) {
            for (Module.Category c : Module.Category.values()) {
                if (!JayHackClient.moduleManager.getByCategory(c).isEmpty()) {
                    selected = c;
                    break;
                }
            }
        }
    }

    private void computeLayout() {
        boolean small = this.width < 500 || this.height < 320;
        if (small) {
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

    private List<Module> filteredModules() {
        List<Module> base = new ArrayList<>();
        if (search != null && !search.isBlank()) {
            String q = search.toLowerCase(Locale.ROOT);
            for (Module m : JayHackClient.moduleManager.getModules()) {
                if (m.getName().toLowerCase(Locale.ROOT).contains(q)
                        || m.getDescription().toLowerCase(Locale.ROOT).contains(q)
                        || m.getCategory().displayName.toLowerCase(Locale.ROOT).contains(q)) {
                    base.add(m);
                }
            }
        } else {
            base.addAll(JayHackClient.moduleManager.getByCategory(selected));
        }
        return base;
    }

    private List<String> friendList() {
        List<String> list = new ArrayList<>();
        if (JayHackClient.friendManager != null) {
            list.addAll(JayHackClient.friendManager.getFriends());
            list.sort(String::compareToIgnoreCase);
        }
        return list;
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        computeLayout();
        int x = winX;
        int y = winY;

        ctx.fill(0, 0, this.width, this.height, BG_OVERLAY);
        ctx.fill(x + 3, y + 3, x + winW + 3, y + winH + 3, 0x55000000);
        ctx.fill(x, y, x + winW, y + winH, BG_WINDOW);

        ctx.fill(x, y, x + winW, y + headerH, BG_HEADER);
        ctx.fill(x, y + headerH - 1, x + winW, y + headerH, ACCENT_DIM);
        ctx.drawTextWithShadow(textRenderer, "§dJ§fay", x + 8, y + 10, TEXT);
        ctx.drawTextWithShadow(textRenderer, "§8v" + JayHackClient.VERSION, x + 34, y + 10, TEXT_DIM);

        String status = JayHackClient.moduleManager.isFrozen() ? "§cFROZEN" : "§aREADY";
        ctx.drawTextWithShadow(textRenderer, status, x + winW - 78, y + 10, TEXT);

        int closeX = x + winW - 24;
        int closeY = y + 6;
        boolean hoverX = mouseX >= closeX && mouseX <= closeX + 18 && mouseY >= closeY && mouseY <= closeY + 18;
        ctx.fill(closeX, closeY, closeX + 18, closeY + 18, hoverX ? 0xFFAA3333 : 0x22FFFFFF);
        ctx.drawCenteredTextWithShadow(textRenderer, "x", closeX + 9, closeY + 5, 0xFFFFFF);

        ctx.fill(x, y + headerH, x + sidebarW, y + winH, BG_SIDEBAR);
        ctx.fill(x + sidebarW - 1, y + headerH, x + sidebarW, y + winH, 0x22FFFFFF);

        int catY = y + headerH + 6;
        for (Module.Category cat : Module.Category.values()) {
            if (JayHackClient.moduleManager.getByCategory(cat).isEmpty()) continue;
            boolean sel = !friendsTab && cat == selected && (search == null || search.isBlank());
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

        // Friends tab
        {
            boolean sel = friendsTab;
            boolean hover = mouseX >= x && mouseX < x + sidebarW && mouseY >= catY && mouseY < catY + 22;
            if (sel) {
                ctx.fill(x, catY, x + sidebarW, catY + 22, 0x33B24BF3);
                ctx.fill(x, catY, x + 3, catY + 22, ACCENT);
            } else if (hover) {
                ctx.fill(x, catY, x + sidebarW, catY + 22, 0x15FFFFFF);
            }
            ctx.drawTextWithShadow(textRenderer, "Friends", x + 8, catY + 7,
                    sel ? ACCENT : (hover ? TEXT : TEXT_DIM));
        }

        int sx = x + sidebarW + 8;
        int sy = y + headerH + 4;
        int sw = winW - sidebarW - 16;
        ctx.fill(sx, sy, sx + sw, sy + searchH - 4, searchFocused ? 0xFF1A1A28 : 0xFF14141C);
        ctx.fill(sx, sy + searchH - 5, sx + sw, sy + searchH - 4, searchFocused ? ACCENT : 0x33FFFFFF);

        if (friendsTab) {
            String shown = friendInput.isEmpty() && !searchFocused
                    ? "§8Type name + Enter to add..."
                    : "§f" + friendInput + (searchFocused ? "§7_" : "");
            ctx.drawTextWithShadow(textRenderer, shown, sx + 6, sy + 5, TEXT);

            List<String> friends = friendList();
            int listX = x + sidebarW;
            int rowTop = y + headerH + searchH + 8;
            int listBottom = y + winH - 16;
            int maxRows = Math.max(1, (listBottom - rowTop) / rowH);

            ctx.drawTextWithShadow(textRenderer, "FRIENDS (" + friends.size() + ")",
                    listX + 10, y + headerH + searchH + 2, TEXT_DIM);

            for (int i = 0; i < friends.size(); i++) {
                if (i < scroll) continue;
                int di = i - scroll;
                if (di >= maxRows) break;
                String name = friends.get(i);
                int ry = rowTop + di * rowH;
                boolean hover = mouseX >= listX && mouseX < x + winW && mouseY >= ry && mouseY < ry + rowH;
                if (hover) ctx.fill(listX + 4, ry, x + winW - 4, ry + rowH - 2, ROW_HOVER);
                ctx.drawTextWithShadow(textRenderer, name, listX + 12, ry + (rowH / 2) - 4, TEXT);
                ctx.drawTextWithShadow(textRenderer, "§c×", x + winW - 20, ry + (rowH / 2) - 4, 0xFFFF6666);
            }

            ctx.drawTextWithShadow(textRenderer, "Enter add · click remove",
                    x + 8, y + winH - 12, 0xFF555566);
        } else {
            String shown = search.isEmpty() && !searchFocused ? "§8Search modules..." : "§f" + search + (searchFocused ? "§7_" : "");
            ctx.drawTextWithShadow(textRenderer, shown, sx + 6, sy + 5, TEXT);

            List<Module> modules = filteredModules();
            int listX = x + sidebarW;
            int rowTop = y + headerH + searchH + 8;
            int listBottom = y + winH - 16;
            int maxRows = Math.max(1, (listBottom - rowTop) / rowH);

            String title = (search == null || search.isBlank())
                    ? selected.displayName.toUpperCase(Locale.ROOT)
                    : "Results";
            ctx.drawTextWithShadow(textRenderer, title, listX + 10, y + headerH + searchH + 2, TEXT_DIM);

            for (int i = 0; i < modules.size(); i++) {
                if (i < scroll) continue;
                int di = i - scroll;
                if (di >= maxRows) break;

                Module m = modules.get(i);
                int ry = rowTop + di * rowH;
                boolean hover = mouseX >= listX && mouseX < x + winW && mouseY >= ry && mouseY < ry + rowH;

                if (m.isEnabled()) ctx.fill(listX + 4, ry, x + winW - 4, ry + rowH - 2, ROW_ON);
                else if (hover) ctx.fill(listX + 4, ry, x + winW - 4, ry + rowH - 2, ROW_HOVER);

                ctx.drawTextWithShadow(textRenderer, m.getName(), listX + 12, ry + (rowH / 2) - 4,
                        m.isEnabled() ? TEXT : TEXT_DIM);

                int tw = 26, th = 14;
                int tx = x + winW - 14 - tw;
                int ty = ry + (rowH - th) / 2;
                ctx.fill(tx, ty, tx + tw, ty + th, m.isEnabled() ? TOGGLE_ON : TOGGLE_OFF);
                int knob = m.isEnabled() ? tx + tw - 13 : tx + 2;
                ctx.fill(knob, ty + 2, knob + 11, ty + th - 2, 0xFFF0F0F8);
            }

            ctx.drawTextWithShadow(textRenderer, "LMB toggle · RMB settings · ESC",
                    x + 8, y + winH - 12, 0xFF555566);
        }

        super.render(ctx, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        double mouseX = click.x();
        double mouseY = click.y();
        int button = click.button();

        computeLayout();
        int x = winX;
        int y = winY;

        int closeX = x + winW - 24;
        int closeY = y + 6;
        if (button == 0 && mouseX >= closeX && mouseX <= closeX + 18 && mouseY >= closeY && mouseY <= closeY + 18) {
            close();
            return true;
        }

        if (button == 0) {
            int sx = x + sidebarW + 8;
            int sy = y + headerH + 4;
            int sw = winW - sidebarW - 16;
            if (mouseX >= sx && mouseX <= sx + sw && mouseY >= sy && mouseY <= sy + searchH - 4) {
                searchFocused = true;
                return true;
            } else {
                searchFocused = false;
            }

            int catY = y + headerH + 6;
            for (Module.Category cat : Module.Category.values()) {
                if (JayHackClient.moduleManager.getByCategory(cat).isEmpty()) continue;
                if (mouseX >= x && mouseX < x + sidebarW && mouseY >= catY && mouseY < catY + 22) {
                    selected = cat;
                    friendsTab = false;
                    search = "";
                    friendInput = "";
                    scroll = 0;
                    return true;
                }
                catY += 24;
            }
            // Friends sidebar button
            if (mouseX >= x && mouseX < x + sidebarW && mouseY >= catY && mouseY < catY + 22) {
                friendsTab = true;
                searchFocused = true;
                scroll = 0;
                return true;
            }
        }

        int listX = x + sidebarW;
        int rowTop = y + headerH + searchH + 8;
        int listBottom = y + winH - 16;
        int maxRows = Math.max(1, (listBottom - rowTop) / rowH);

        if (friendsTab && button == 0) {
            List<String> friends = friendList();
            for (int i = 0; i < friends.size(); i++) {
                if (i < scroll) continue;
                int di = i - scroll;
                if (di >= maxRows) break;
                int ry = rowTop + di * rowH;
                if (mouseX >= listX && mouseX < x + winW && mouseY >= ry && mouseY < ry + rowH) {
                    String name = friends.get(i);
                    JayHackClient.friendManager.remove(name);
                    if (JayHackClient.configManager != null) JayHackClient.configManager.save();
                    if (client != null && client.player != null) {
                        client.player.sendMessage(Text.literal("§8[§bJay§8] §c- " + name), false);
                    }
                    return true;
                }
            }
            return true;
        }

        if (!friendsTab && (button == 0 || button == 1)) {
            List<Module> modules = filteredModules();
            for (int i = 0; i < modules.size(); i++) {
                if (i < scroll) continue;
                int di = i - scroll;
                if (di >= maxRows) break;
                int ry = rowTop + di * rowH;
                if (mouseX >= listX && mouseX < x + winW && mouseY >= ry && mouseY < ry + rowH) {
                    Module mod = modules.get(i);
                    if (button == 1) {
                        if (client != null) client.setScreen(new SettingsScreen(this, mod));
                        return true;
                    }
                    if (JayHackClient.moduleManager.isFrozen()) {
                        if (client != null && client.player != null) {
                            client.player.sendMessage(Text.literal("§8[§bJay§8] §cUnpanic first"), false);
                        }
                    } else {
                        mod.toggle();
                    }
                    return true;
                }
            }
        }

        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        computeLayout();
        int rowTop = winY + headerH + searchH + 8;
        int listBottom = winY + winH - 16;
        int maxRows = Math.max(1, (listBottom - rowTop) / rowH);
        int size = friendsTab ? friendList().size() : filteredModules().size();
        int maxScroll = Math.max(0, size - maxRows);
        if (verticalAmount > 0) scroll = Math.max(0, scroll - 1);
        else if (verticalAmount < 0) scroll = Math.min(maxScroll, scroll + 1);
        return true;
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        int keyCode = input.key();

        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            if (searchFocused && (!(friendsTab ? friendInput : search).isEmpty())) {
                if (friendsTab) friendInput = "";
                else search = "";
                return true;
            }
            close();
            return true;
        }

        if (searchFocused) {
            if (keyCode == GLFW.GLFW_KEY_BACKSPACE) {
                if (friendsTab && !friendInput.isEmpty()) {
                    friendInput = friendInput.substring(0, friendInput.length() - 1);
                    return true;
                }
                if (!friendsTab && !search.isEmpty()) {
                    search = search.substring(0, search.length() - 1);
                    scroll = 0;
                    return true;
                }
            }
            if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
                if (friendsTab && !friendInput.isBlank()) {
                    String name = friendInput.trim();
                    JayHackClient.friendManager.add(name);
                    if (JayHackClient.configManager != null) JayHackClient.configManager.save();
                    if (client != null && client.player != null) {
                        client.player.sendMessage(Text.literal("§8[§bJay§8] §a+ " + name), false);
                    }
                    friendInput = "";
                    return true;
                }
                searchFocused = false;
                return true;
            }
        }

        if (keyCode == GLFW.GLFW_KEY_RIGHT_SHIFT && !searchFocused) {
            close();
            return true;
        }

        return super.keyPressed(input);
    }

    @Override
    public boolean charTyped(CharInput input) {
        if (searchFocused) {
            int cp = input.codepoint();
            if (cp >= 32 && cp < 127) {
                if (friendsTab) {
                    if (friendInput.length() < 24) friendInput += (char) cp;
                } else if (search.length() < 32) {
                    search += (char) cp;
                    scroll = 0;
                }
                return true;
            }
            return true;
        }
        return super.charTyped(input);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
