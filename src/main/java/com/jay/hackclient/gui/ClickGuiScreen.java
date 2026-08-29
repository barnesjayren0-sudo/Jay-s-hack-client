package com.jay.hackclient.gui;

import com.jay.hackclient.JayHackClient;
import com.jay.hackclient.module.Module;
import com.jay.hackclient.profile.LegitProfile;
import com.jay.hackclient.render.JayLogo;
import com.jay.hackclient.settings.ClientSettings;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.CharInput;
import net.minecraft.client.input.KeyInput;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Premium centered ClickGUI — JAY CLIENT
 * Sidebar categories + module cards · mobile-safe · soft glass panels.
 */
public class ClickGuiScreen extends Screen {

    private static final String[] PROFILES = { "sword", "scout", "nethpot" };

    private Module.Category selected = Module.Category.COMBAT;
    private boolean friendsTab;
    private String search = "";
    private boolean searchFocused;
    private String friendInput = "";
    private int scroll;

    private int winX, winY, winW, winH;
    private int sideW, headerH, footerH, searchH, cardH;
    private boolean mobile;

    private long openMs = System.currentTimeMillis();

    public ClickGuiScreen() {
        super(Text.literal("JAY CLIENT"));
        String lp = ClientSettings.lastProfile;
        if (lp != null && (lp.equals("scout") || lp.equals("builder") || lp.equals("explore"))) {
            selected = Module.Category.WORLD;
        }
    }

    private void layout() {
        mobile = width < 520 || height < 360;
        headerH = mobile ? 36 : 40;
        footerH = mobile ? 22 : 24;
        searchH = mobile ? 26 : 28;
        sideW = mobile ? 72 : 96;
        cardH = mobile ? 36 : 40;

        winW = Math.min(mobile ? width - 12 : 520, width - 16);
        winH = Math.min(mobile ? height - 16 : 360, height - 20);
        winX = (width - winW) / 2;
        winY = (height - winH) / 2;
    }

    private float openAnim() {
        float t = (System.currentTimeMillis() - openMs) / 180f;
        if (t >= 1f) return 1f;
        // ease out
        return 1f - (1f - t) * (1f - t);
    }

    private List<Module> modulesList() {
        List<Module> out = new ArrayList<>();
        if (JayHackClient.moduleManager == null) return out;
        String q = search == null ? "" : search.toLowerCase(Locale.ROOT).trim();
        if (!q.isEmpty()) {
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

    private List<String> friends() {
        List<String> list = new ArrayList<>();
        if (JayHackClient.friendManager != null) {
            list.addAll(JayHackClient.friendManager.getFriends());
            list.sort(String::compareToIgnoreCase);
        }
        return list;
    }

    private void panel(DrawContext ctx, int x1, int y1, int x2, int y2, int color) {
        ctx.fill(x1 + 2, y1, x2 - 2, y2, color);
        ctx.fill(x1, y1 + 2, x2, y2 - 2, color);
        ctx.fill(x1 + 1, y1 + 1, x2 - 1, y2 - 1, color);
    }

    private void border(DrawContext ctx, int x1, int y1, int x2, int y2, int c) {
        ctx.fill(x1, y1, x2, y1 + 1, c);
        ctx.fill(x1, y2 - 1, x2, y2, c);
        ctx.fill(x1, y1, x1 + 1, y2, c);
        ctx.fill(x2 - 1, y1, x2, y2, c);
    }

    private void toggle(DrawContext ctx, int x, int y, boolean on) {
        int tw = mobile ? 28 : 32;
        int th = mobile ? 14 : 16;
        int bg = on ? GuiTheme.ACCENT : GuiTheme.TOGGLE_OFF;
        ctx.fill(x + 1, y, x + tw - 1, y + th, bg);
        ctx.fill(x, y + 1, x + tw, y + th - 1, bg);
        int kn = th - 4;
        int kx = on ? x + tw - kn - 2 : x + 2;
        ctx.fill(kx, y + 2, kx + kn, y + 2 + kn, 0xFFFFFFFF);
    }

    private int ping() {
        try {
            MinecraftClient mc = client;
            if (mc == null || mc.player == null) return -1;
            ClientPlayNetworkHandler nh = mc.getNetworkHandler();
            if (nh == null) return -1;
            PlayerListEntry e = nh.getPlayerListEntry(mc.player.getUuid());
            return e == null ? -1 : e.getLatency();
        } catch (Throwable t) {
            return -1;
        }
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        layout();
        float anim = openAnim();

        // Dim world
        ctx.fill(0, 0, width, height, GuiTheme.withAlpha(0x000000, (int) (0x99 * anim)));

        int x = winX;
        int y = winY + (int) ((1f - anim) * 12);
        int w = winW;
        int h = winH;

        // Shadow
        ctx.fill(x + 4, y + 6, x + w + 4, y + h + 6, GuiTheme.SHADOW);

        // Main glass panel
        panel(ctx, x, y, x + w, y + h, GuiTheme.BG);
        border(ctx, x, y, x + w, y + h, GuiTheme.BORDER);
        // Top accent line
        ctx.fill(x, y, x + w, y + 1, GuiTheme.ACCENT);

        // —— Header ——
        ctx.fill(x, y, x + w, y + headerH, GuiTheme.PANEL);
        JayLogo.draw(ctx, x + 10, y + (headerH - 16) / 2, 16);
        ctx.drawTextWithShadow(textRenderer, "JAY CLIENT", x + 32, y + headerH / 2 - 4, GuiTheme.TEXT);

        String ver = "v" + JayHackClient.VERSION;
        ctx.drawTextWithShadow(textRenderer, ver,
                x + w - 12 - textRenderer.getWidth(ver) - 28, y + headerH / 2 - 4, GuiTheme.TEXT_DIM);

        // READY / FROZEN
        boolean frozen = JayHackClient.moduleManager != null && JayHackClient.moduleManager.isFrozen();
        String st = frozen ? "FROZEN" : "READY";
        int stc = frozen ? 0xFFFF5555 : GuiTheme.ACCENT;
        int stx = x + w - 14 - textRenderer.getWidth(st);
        ctx.fill(stx - 10, y + headerH / 2 - 2, stx - 5, y + headerH / 2 + 3, stc);
        ctx.drawTextWithShadow(textRenderer, st, stx, y + headerH / 2 - 4, stc);

        // —— Search ——
        int sy = y + headerH + 6;
        int sPad = 10;
        int searchX = x + sPad;
        int searchW = w - sPad * 2;
        panel(ctx, searchX, sy, searchX + searchW, sy + searchH, GuiTheme.PANEL2);
        if (searchFocused) {
            ctx.fill(searchX + 2, sy + searchH - 2, searchX + searchW - 2, sy + searchH - 1, GuiTheme.ACCENT);
        }
        String ph = friendsTab
                ? (friendInput.isEmpty() && !searchFocused ? "§8Add friend..." : "§f" + friendInput + (searchFocused ? "§7|" : ""))
                : (search.isEmpty() && !searchFocused ? "§8Search modules..." : "§f" + search + (searchFocused ? "§7|" : ""));
        ctx.drawTextWithShadow(textRenderer, ph, searchX + 8, sy + searchH / 2 - 4, GuiTheme.TEXT);

        // —— Body: sidebar + content ——
        int bodyTop = sy + searchH + 6;
        int bodyBot = y + h - footerH - 4;
        int bodyH = bodyBot - bodyTop;

        // Sidebar
        ctx.fill(x + 1, bodyTop, x + sideW, bodyBot, GuiTheme.PANEL);

        int catY = bodyTop + 4;
        for (Module.Category cat : Module.Category.values()) {
            if (JayHackClient.moduleManager != null
                    && JayHackClient.moduleManager.getByCategory(cat).isEmpty()
                    && (search == null || search.isBlank())) continue;

            boolean on = !friendsTab && (search == null || search.isBlank()) && cat == selected;
            boolean hover = mouseX >= x + 4 && mouseX < x + sideW - 4
                    && mouseY >= catY && mouseY < catY + 18;
            if (on || hover) {
                ctx.fill(x + 4, catY, x + sideW - 4, catY + 18, on ? GuiTheme.ROW_ON : GuiTheme.ROW_HOVER);
            }
            if (on) ctx.fill(x + 4, catY + 3, x + 6, catY + 15, GuiTheme.ACCENT);
            String label = mobile && cat.displayName.length() > 6
                    ? cat.displayName.substring(0, 4)
                    : cat.displayName;
            ctx.drawTextWithShadow(textRenderer, label, x + 10, catY + 5,
                    on ? GuiTheme.ACCENT : (hover ? GuiTheme.TEXT : GuiTheme.TEXT_DIM));
            catY += 20;
        }

        // Friends tab
        {
            boolean on = friendsTab;
            boolean hover = mouseX >= x + 4 && mouseX < x + sideW - 4
                    && mouseY >= catY && mouseY < catY + 18;
            if (on || hover) ctx.fill(x + 4, catY, x + sideW - 4, catY + 18, on ? GuiTheme.ROW_ON : GuiTheme.ROW_HOVER);
            if (on) ctx.fill(x + 4, catY + 3, x + 6, catY + 15, GuiTheme.ACCENT2);
            ctx.drawTextWithShadow(textRenderer, "Friends", x + 10, catY + 5,
                    on ? GuiTheme.ACCENT2 : (hover ? GuiTheme.TEXT : GuiTheme.TEXT_DIM));
        }

        // Content area
        int cx = x + sideW + 6;
        int cw = x + w - 8 - cx;
        int listTop = bodyTop + 2;
        int listBot = bodyBot - 2;
        int maxCards = Math.max(1, (listBot - listTop) / (cardH + 3));

        if (friendsTab) {
            List<String> fl = friends();
            if (fl.isEmpty()) {
                ctx.drawTextWithShadow(textRenderer, "No friends yet", cx + 8, listTop + 20, GuiTheme.TEXT_DIM);
                ctx.drawTextWithShadow(textRenderer, "Type a name above + Enter", cx + 8, listTop + 34, GuiTheme.TEXT_DIM);
            }
            int row = 0;
            for (int i = 0; i < fl.size(); i++) {
                if (i < scroll) continue;
                if (row >= maxCards) break;
                int ry = listTop + row * (cardH + 3);
                boolean hover = mouseX >= cx && mouseX < cx + cw && mouseY >= ry && mouseY < ry + cardH;
                panel(ctx, cx, ry, cx + cw, ry + cardH, hover ? GuiTheme.ROW_HOVER : GuiTheme.PANEL2);
                ctx.drawTextWithShadow(textRenderer, fl.get(i), cx + 10, ry + cardH / 2 - 4, GuiTheme.TEXT);
                ctx.drawTextWithShadow(textRenderer, "§c×", cx + cw - 16, ry + cardH / 2 - 4, 0xFFFF6666);
                row++;
            }
        } else {
            List<Module> mods = modulesList();
            if (mods.isEmpty()) {
                ctx.drawTextWithShadow(textRenderer, "No results found",
                        cx + 8, listTop + 24, GuiTheme.TEXT_DIM);
            }
            int row = 0;
            for (int i = 0; i < mods.size(); i++) {
                if (i < scroll) continue;
                if (row >= maxCards) break;
                Module m = mods.get(i);
                int ry = listTop + row * (cardH + 3);
                boolean hover = mouseX >= cx && mouseX < cx + cw && mouseY >= ry && mouseY < ry + cardH;

                int bg = m.isEnabled() ? GuiTheme.ROW_ON : (hover ? GuiTheme.ROW_HOVER : GuiTheme.PANEL2);
                panel(ctx, cx, ry, cx + cw, ry + cardH, bg);
                if (m.isEnabled()) {
                    ctx.fill(cx, ry, cx + 2, ry + cardH, GuiTheme.ACCENT);
                }

                ctx.drawTextWithShadow(textRenderer, m.getName(), cx + 10, ry + (mobile ? cardH / 2 - 4 : 8),
                        m.isEnabled() ? GuiTheme.TEXT : 0xFFC8CDD8);

                if (!mobile) {
                    String desc = m.getDescription();
                    if (desc.length() > 36) desc = desc.substring(0, 33) + "...";
                    ctx.drawTextWithShadow(textRenderer, desc, cx + 10, ry + 22, GuiTheme.TEXT_DIM);
                }

                String key = m.getKeyLabel();
                if (!key.isEmpty()) {
                    int kx = cx + 12 + textRenderer.getWidth(m.getName()) + 6;
                    ctx.drawTextWithShadow(textRenderer, "§8[§b" + key + "§8]", kx,
                            ry + (mobile ? cardH / 2 - 4 : 8), GuiTheme.TEXT_DIM);
                }

                toggle(ctx, cx + cw - (mobile ? 40 : 46), ry + (cardH - (mobile ? 14 : 16)) / 2, m.isEnabled());
                row++;
            }
        }

        // —— Footer: profiles + status ——
        int fy = y + h - footerH;
        ctx.fill(x, fy, x + w, y + h, GuiTheme.PANEL);
        ctx.fill(x, fy, x + w, fy + 1, GuiTheme.BORDER);

        int px = x + 8;
        for (String p : PROFILES) {
            boolean on = p.equalsIgnoreCase(ClientSettings.lastProfile);
            int pw = textRenderer.getWidth(p) + 10;
            boolean hover = mouseX >= px && mouseX < px + pw && mouseY >= fy + 3 && mouseY < y + h - 3;
            ctx.fill(px, fy + 4, px + pw, y + h - 4, on ? GuiTheme.ROW_ON : (hover ? GuiTheme.ROW_HOVER : GuiTheme.PANEL2));
            if (on) ctx.fill(px, y + h - 5, px + pw, y + h - 4, GuiTheme.ACCENT);
            ctx.drawTextWithShadow(textRenderer, p, px + 5, fy + 8, on ? GuiTheme.ACCENT : GuiTheme.TEXT_DIM);
            px += pw + 4;
        }

        // FPS / ping
        int fps = MinecraftClient.getInstance().getCurrentFps();
        int pg = ping();
        String info = "FPS " + fps + (pg >= 0 ? "  ·  " + pg + "ms" : "");
        ctx.drawTextWithShadow(textRenderer, info,
                x + w - 10 - textRenderer.getWidth(info), fy + 8, GuiTheme.TEXT_DIM);

        super.render(ctx, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        double mx = click.x();
        double my = click.y();
        int button = click.button();
        layout();

        int x = winX, y = winY, w = winW, h = winH;
        int sy = y + headerH + 6;
        int searchX = x + 10;
        int searchW = w - 20;

        // Search focus
        if (button == 0 && mx >= searchX && mx <= searchX + searchW && my >= sy && my <= sy + searchH) {
            searchFocused = true;
            return true;
        }

        int bodyTop = sy + searchH + 6;
        int bodyBot = y + h - footerH - 4;

        // Sidebar categories
        if (button == 0 && mx >= x + 4 && mx < x + sideW - 4) {
            int catY = bodyTop + 4;
            for (Module.Category cat : Module.Category.values()) {
                if (JayHackClient.moduleManager != null
                        && JayHackClient.moduleManager.getByCategory(cat).isEmpty()
                        && (search == null || search.isBlank())) continue;
                if (my >= catY && my < catY + 18) {
                    selected = cat;
                    friendsTab = false;
                    search = "";
                    scroll = 0;
                    searchFocused = false;
                    return true;
                }
                catY += 20;
            }
            if (my >= catY && my < catY + 18) {
                friendsTab = true;
                searchFocused = true;
                scroll = 0;
                return true;
            }
        }

        // Profiles
        int fy = y + h - footerH;
        if (button == 0 && my >= fy) {
            int px = x + 8;
            for (String p : PROFILES) {
                int pw = textRenderer.getWidth(p) + 10;
                if (mx >= px && mx < px + pw) {
                    applyProfile(p);
                    return true;
                }
                px += pw + 4;
            }
        }

        int cx = x + sideW + 6;
        int cw = x + w - 8 - cx;
        int listTop = bodyTop + 2;
        int listBot = bodyBot - 2;
        int maxCards = Math.max(1, (listBot - listTop) / (cardH + 3));

        if (friendsTab && button == 0) {
            List<String> fl = friends();
            int row = 0;
            for (int i = 0; i < fl.size(); i++) {
                if (i < scroll) continue;
                if (row >= maxCards) break;
                int ry = listTop + row * (cardH + 3);
                if (mx >= cx && mx < cx + cw && my >= ry && my < ry + cardH) {
                    JayHackClient.friendManager.remove(fl.get(i));
                    if (JayHackClient.configManager != null) JayHackClient.configManager.save();
                    return true;
                }
                row++;
            }
            searchFocused = false;
            return true;
        }

        if (!friendsTab && (button == 0 || button == 1)) {
            List<Module> mods = modulesList();
            int row = 0;
            for (int i = 0; i < mods.size(); i++) {
                if (i < scroll) continue;
                if (row >= maxCards) break;
                int ry = listTop + row * (cardH + 3);
                if (mx >= cx && mx < cx + cw && my >= ry && my < ry + cardH) {
                    Module m = mods.get(i);
                    if (button == 1) {
                        if (client != null) client.setScreen(new SettingsScreen(this, m));
                        return true;
                    }
                    if (JayHackClient.moduleManager != null && JayHackClient.moduleManager.isFrozen()) {
                        if (client != null && client.player != null) {
                            client.player.sendMessage(Text.literal("§8[§bJay§8] §cUnpanic first"), false);
                        }
                    } else {
                        m.toggle();
                    }
                    return true;
                }
                row++;
            }
        }

        searchFocused = false;
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double hAmount, double vAmount) {
        layout();
        int bodyTop = winY + headerH + 6 + searchH + 6;
        int bodyBot = winY + winH - footerH - 4;
        int maxCards = Math.max(1, (bodyBot - bodyTop - 2) / (cardH + 3));
        int size = friendsTab ? friends().size() : modulesList().size();
        int maxScroll = Math.max(0, size - maxCards);
        if (vAmount > 0) scroll = Math.max(0, scroll - 1);
        else if (vAmount < 0) scroll = Math.min(maxScroll, scroll + 1);
        return true;
    }

    private void applyProfile(String name) {
        switch (name.toLowerCase(Locale.ROOT)) {
            case "sword" -> LegitProfile.applySword();
            case "scout" -> LegitProfile.applyScout();
            case "nethpot" -> LegitProfile.applyNethpot();
            default -> { return; }
        }
        if (JayHackClient.configManager != null) JayHackClient.configManager.save();
        if (client != null && client.player != null) {
            client.player.sendMessage(Text.literal("§8[§bJay§8] §aProfile §f" + name), false);
        }
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        int key = input.key();
        if (key == GLFW.GLFW_KEY_ESCAPE || (key == GLFW.GLFW_KEY_RIGHT_SHIFT && !searchFocused)) {
            close();
            return true;
        }
        if (searchFocused && key == GLFW.GLFW_KEY_BACKSPACE) {
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
        if (searchFocused && (key == GLFW.GLFW_KEY_ENTER || key == GLFW.GLFW_KEY_KP_ENTER)) {
            if (friendsTab && !friendInput.isBlank()) {
                JayHackClient.friendManager.add(friendInput.trim());
                if (JayHackClient.configManager != null) JayHackClient.configManager.save();
                friendInput = "";
                return true;
            }
            searchFocused = false;
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
        }
        return super.charTyped(input);
    }

    @Override
    public boolean shouldPause() {
        return false;
    }
}
