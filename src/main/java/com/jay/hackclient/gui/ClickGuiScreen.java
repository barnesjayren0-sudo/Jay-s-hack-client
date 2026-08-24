package com.jay.hackclient.gui;

import com.jay.hackclient.JayHackClient;
import com.jay.hackclient.module.Module;
import com.jay.hackclient.render.JayLogo;
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
 * Mobile-first ClickGUI (mockup layout):
 * header (logo + Jay + READY) → search → category chips → module rows + toggles.
 */
public class ClickGuiScreen extends Screen {

    // Colors from mockup
    private static final int BG = 0xFF0B0D12;
    private static final int CARD = 0xFF12151C;
    private static final int ROW = 0xFF151922;
    private static final int SEARCH_BG = 0xFF1A1E28;
    private static final int CHIP_ON = 0xFF1A2330;
    private static final int CHIP_OFF = 0x00000000;
    private static final int CYAN = 0xFF3DDCFF;
    private static final int TEXT = 0xFFE8ECF4;
    private static final int TEXT_DIM = 0xFF7A8499;
    private static final int TOGGLE_ON = 0xFF3DDCFF;
    private static final int TOGGLE_OFF = 0xFF2A3140;
    private static final int KNOB = 0xFFF0F4FA;

    private Module.Category selected = Module.Category.COMBAT;
    private boolean friendsTab = false;
    private int scroll;
    private String search = "";
    private boolean searchFocused = false;
    private String friendInput = "";

    // layout
    private int pad;
    private int headerH;
    private int searchH;
    private int chipH;
    private int rowH;
    private int listTop;

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

    private void layout() {
        boolean phone = width < 520 || height < 360;
        pad = phone ? 12 : 16;
        headerH = phone ? 36 : 40;
        searchH = phone ? 34 : 36;
        chipH = phone ? 28 : 30;
        rowH = phone ? 52 : 48;
        listTop = pad + headerH + 8 + searchH + 10 + chipH + 10;
    }

    private List<Module> filteredModules() {
        List<Module> out = new ArrayList<>();
        if (search != null && !search.isBlank()) {
            String q = search.toLowerCase(Locale.ROOT);
            for (Module m : JayHackClient.moduleManager.getModules()) {
                if (m.getName().toLowerCase(Locale.ROOT).contains(q)
                        || m.getDescription().toLowerCase(Locale.ROOT).contains(q)
                        || m.getCategory().displayName.toLowerCase(Locale.ROOT).contains(q)) {
                    out.add(m);
                }
            }
        } else {
            out.addAll(JayHackClient.moduleManager.getByCategory(selected));
        }
        return out;
    }

    private List<String> friendList() {
        List<String> list = new ArrayList<>();
        if (JayHackClient.friendManager != null) {
            list.addAll(JayHackClient.friendManager.getFriends());
            list.sort(String::compareToIgnoreCase);
        }
        return list;
    }

    private List<Module.Category> visibleCategories() {
        List<Module.Category> list = new ArrayList<>();
        for (Module.Category c : Module.Category.values()) {
            if (!JayHackClient.moduleManager.getByCategory(c).isEmpty()) list.add(c);
        }
        return list;
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        layout();

        // Full dark panel (mobile sheet)
        ctx.fill(0, 0, width, height, BG);

        // —— Header ——
        int logo = 22;
        JayLogo.draw(ctx, pad, pad + (headerH - logo) / 2, logo);
        int titleX = pad + logo + 8;
        ctx.drawTextWithShadow(textRenderer, "§fJay", titleX, pad + (headerH / 2) - 4, TEXT);

        // READY / FROZEN pill
        boolean frozen = JayHackClient.moduleManager != null && JayHackClient.moduleManager.isFrozen();
        String status = frozen ? "FROZEN" : "READY";
        int statusCol = frozen ? 0xFFFF5555 : CYAN;
        int sw = textRenderer.getWidth(status) + 18;
        int sx = width - pad - 28 - sw;
        int sy = pad + (headerH / 2) - 6;
        // cyan/red dot
        ctx.fill(sx, sy + 4, sx + 6, sy + 10, statusCol);
        ctx.drawTextWithShadow(textRenderer, status, sx + 10, sy + 1, statusCol);

        // Close X
        int cx = width - pad - 16;
        int cy = pad + (headerH / 2) - 6;
        boolean hoverClose = mouseX >= cx - 4 && mouseX <= cx + 14 && mouseY >= cy - 2 && mouseY <= cy + 14;
        ctx.drawTextWithShadow(textRenderer, "×", cx, cy, hoverClose ? 0xFFFF8888 : TEXT_DIM);

        // —— Search ——
        int searchY = pad + headerH + 8;
        int searchW = width - pad * 2;
        fillRoundish(ctx, pad, searchY, pad + searchW, searchY + searchH, SEARCH_BG);
        if (searchFocused) {
            ctx.fill(pad, searchY + searchH - 1, pad + searchW, searchY + searchH, CYAN);
        }

        String searchShown;
        if (friendsTab) {
            searchShown = friendInput.isEmpty() && !searchFocused
                    ? "§8Add friend name..."
                    : "§f" + friendInput + (searchFocused ? "§7|" : "");
        } else {
            searchShown = search.isEmpty() && !searchFocused
                    ? "§8Search modules..."
                    : "§f" + search + (searchFocused ? "§7|" : "");
        }
        ctx.drawTextWithShadow(textRenderer, searchShown, pad + 12, searchY + (searchH / 2) - 4, TEXT);

        // —— Category chips ——
        int chipY = searchY + searchH + 10;
        int chipX = pad;
        List<Module.Category> cats = visibleCategories();
        for (Module.Category cat : cats) {
            String label = cat.displayName;
            int cw = textRenderer.getWidth(label) + 20;
            boolean on = !friendsTab && cat == selected && (search == null || search.isBlank());
            boolean hover = mouseX >= chipX && mouseX < chipX + cw && mouseY >= chipY && mouseY < chipY + chipH;
            if (on) {
                fillRoundish(ctx, chipX, chipY, chipX + cw, chipY + chipH, CHIP_ON);
                // subtle cyan border
                ctx.fill(chipX, chipY, chipX + cw, chipY + 1, CYAN & 0x55FFFFFF | 0x55000000);
            } else if (hover) {
                fillRoundish(ctx, chipX, chipY, chipX + cw, chipY + chipH, 0x18FFFFFF);
            }
            ctx.drawTextWithShadow(textRenderer, label, chipX + 10, chipY + (chipH / 2) - 4,
                    on ? CYAN : (hover ? TEXT : TEXT_DIM));
            chipX += cw + 6;
            if (chipX > width - pad - 40) break; // overflow stop
        }
        // Friends chip
        {
            String label = "Friends";
            int cw = textRenderer.getWidth(label) + 20;
            boolean on = friendsTab;
            boolean hover = mouseX >= chipX && mouseX < chipX + cw && mouseY >= chipY && mouseY < chipY + chipH;
            if (on) fillRoundish(ctx, chipX, chipY, chipX + cw, chipY + chipH, CHIP_ON);
            else if (hover) fillRoundish(ctx, chipX, chipY, chipX + cw, chipY + chipH, 0x18FFFFFF);
            ctx.drawTextWithShadow(textRenderer, label, chipX + 10, chipY + (chipH / 2) - 4,
                    on ? CYAN : (hover ? TEXT : TEXT_DIM));
        }

        // —— Module / Friends list ——
        int y = listTop;
        int maxY = height - pad - 8;

        if (friendsTab) {
            List<String> friends = friendList();
            int idx = 0;
            for (int i = 0; i < friends.size(); i++) {
                if (i < scroll) continue;
                if (y + rowH > maxY) break;
                String name = friends.get(i);
                boolean hover = mouseX >= pad && mouseX < width - pad && mouseY >= y && mouseY < y + rowH - 4;
                fillRoundish(ctx, pad, y, width - pad, y + rowH - 6, hover ? 0xFF1A2030 : ROW);
                ctx.drawTextWithShadow(textRenderer, name, pad + 14, y + 12, TEXT);
                ctx.drawTextWithShadow(textRenderer, "§cRemove", width - pad - 52, y + 12, 0xFFFF6666);
                y += rowH;
                idx++;
            }
            if (friends.isEmpty()) {
                ctx.drawTextWithShadow(textRenderer, "§8No friends yet", pad + 8, y + 8, TEXT_DIM);
            }
        } else {
            List<Module> modules = filteredModules();
            for (int i = 0; i < modules.size(); i++) {
                if (i < scroll) continue;
                if (y + rowH > maxY) break;
                Module m = modules.get(i);
                boolean hover = mouseX >= pad && mouseX < width - pad && mouseY >= y && mouseY < y + rowH - 4;

                fillRoundish(ctx, pad, y, width - pad, y + rowH - 6, hover ? 0xFF1A2030 : ROW);

                // Name + description
                ctx.drawTextWithShadow(textRenderer, m.getName(), pad + 14, y + 10,
                        m.isEnabled() ? TEXT : 0xFFC0C6D4);
                String desc = m.getDescription();
                if (desc.length() > 36) desc = desc.substring(0, 34) + "…";
                ctx.drawTextWithShadow(textRenderer, desc, pad + 14, y + 24, TEXT_DIM);

                // iOS-style toggle
                drawToggle(ctx, width - pad - 44, y + (rowH - 6) / 2 - 8, m.isEnabled());

                y += rowH;
            }
            if (modules.isEmpty()) {
                ctx.drawTextWithShadow(textRenderer, "§8No modules", pad + 8, y + 8, TEXT_DIM);
            }
        }

        // Footer hint
        ctx.drawTextWithShadow(textRenderer, "§8Tap toggle · Hold/RMB settings · ESC",
                pad, height - 14, 0xFF4A5260);

        super.render(ctx, mouseX, mouseY, delta);
    }

    private void drawToggle(DrawContext ctx, int x, int y, boolean on) {
        int w = 36;
        int h = 18;
        ctx.fill(x, y, x + w, y + h, on ? TOGGLE_ON : TOGGLE_OFF);
        // soft corners approximation
        ctx.fill(x, y, x + 2, y + h, on ? TOGGLE_ON : TOGGLE_OFF);
        int knob = on ? x + w - 16 : x + 2;
        ctx.fill(knob, y + 2, knob + 14, y + h - 2, KNOB);
    }

    /** Soft rectangle (no real rounded API needed). */
    private void fillRoundish(DrawContext ctx, int x1, int y1, int x2, int y2, int color) {
        ctx.fill(x1, y1, x2, y2, color);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        double mx = click.x();
        double my = click.y();
        int button = click.button();
        layout();

        // Close
        int cx = width - pad - 16;
        int cy = pad + (headerH / 2) - 6;
        if (button == 0 && mx >= cx - 4 && mx <= cx + 14 && my >= cy - 2 && my <= cy + 14) {
            close();
            return true;
        }

        // Search focus
        int searchY = pad + headerH + 8;
        int searchW = width - pad * 2;
        if (button == 0 && mx >= pad && mx <= pad + searchW && my >= searchY && my <= searchY + searchH) {
            searchFocused = true;
            return true;
        }

        // Chips
        int chipY = searchY + searchH + 10;
        int chipX = pad;
        if (button == 0 && my >= chipY && my < chipY + chipH) {
            for (Module.Category cat : visibleCategories()) {
                String label = cat.displayName;
                int cw = textRenderer.getWidth(label) + 20;
                if (mx >= chipX && mx < chipX + cw) {
                    selected = cat;
                    friendsTab = false;
                    search = "";
                    friendInput = "";
                    scroll = 0;
                    searchFocused = false;
                    return true;
                }
                chipX += cw + 6;
                if (chipX > width - pad - 40) break;
            }
            String label = "Friends";
            int cw = textRenderer.getWidth(label) + 20;
            if (mx >= chipX && mx < chipX + cw) {
                friendsTab = true;
                searchFocused = true;
                scroll = 0;
                return true;
            }
        }

        // List interactions
        int y = listTop;
        int maxY = height - pad - 8;

        if (friendsTab && button == 0) {
            List<String> friends = friendList();
            for (int i = 0; i < friends.size(); i++) {
                if (i < scroll) continue;
                if (y + rowH > maxY) break;
                if (mx >= pad && mx < width - pad && my >= y && my < y + rowH - 4) {
                    String name = friends.get(i);
                    JayHackClient.friendManager.remove(name);
                    if (JayHackClient.configManager != null) JayHackClient.configManager.save();
                    if (client != null && client.player != null) {
                        client.player.sendMessage(Text.literal("§8[§bJay§8] §c- " + name), false);
                    }
                    return true;
                }
                y += rowH;
            }
            return true;
        }

        if (!friendsTab && (button == 0 || button == 1)) {
            List<Module> modules = filteredModules();
            for (int i = 0; i < modules.size(); i++) {
                if (i < scroll) continue;
                if (y + rowH > maxY) break;
                if (mx >= pad && mx < width - pad && my >= y && my < y + rowH - 4) {
                    Module mod = modules.get(i);
                    if (button == 1) {
                        if (client != null) client.setScreen(new SettingsScreen(this, mod));
                        return true;
                    }
                    // toggle hit-area prefers right side but whole row works
                    if (JayHackClient.moduleManager.isFrozen()) {
                        if (client != null && client.player != null) {
                            client.player.sendMessage(Text.literal("§8[§bJay§8] §cUnpanic first"), false);
                        }
                    } else {
                        mod.toggle();
                    }
                    return true;
                }
                y += rowH;
            }
        }

        if (button == 0) searchFocused = false;
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double horizontalAmount, double verticalAmount) {
        layout();
        int maxY = height - pad - 8;
        int visible = Math.max(1, (maxY - listTop) / rowH);
        int size = friendsTab ? friendList().size() : filteredModules().size();
        int maxScroll = Math.max(0, size - visible);
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
