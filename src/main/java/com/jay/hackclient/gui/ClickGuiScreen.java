package com.jay.hackclient.gui;

import com.jay.hackclient.JayHackClient;
import com.jay.hackclient.module.Module;
import com.jay.hackclient.profile.LegitProfile;
import com.jay.hackclient.render.JayLogo;
import com.jay.hackclient.settings.ClientSettings;
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

/** PvP + server utility ClickGUI — dual profiles. */
public class ClickGuiScreen extends Screen {

    private static final int BG_OVERLAY = 0x99000000;
    private static final int BG_WINDOW = 0xFF0E0E14;
    private static final int BG_ROW = 0xFF14141C;
    private static final int BG_SEARCH = 0xFF1A1A24;
    private static final int ACCENT = 0xFF3DDCFF;
    private static final int TEXT = 0xFFF0F0F8;
    private static final int TEXT_DIM = 0xFF7A7A90;
    private static final int TOGGLE_OFF = 0xFF2A2A38;
    private static final int DIVIDER = 0x14FFFFFF;
    private static final int PILL_BG = 0xFF1A1A24;
    private static final int PILL_ON = 0xFF1A2A32;

    /** PvP + utility quick profiles */
    private static final String[] PROFILES = { "sword", "scout", "nethpot" };

    private Module.Category selected = Module.Category.COMBAT;
    private boolean friendsTab = false;
    private int scroll;
    private String search = "";
    private boolean searchFocused = false;
    private String friendInput = "";

    private int winX, winY, winW, winH;
    private int headerH = 36;
    private int searchH = 28;
    private int tabH = 28;
    private int profileH = 26;
    private int rowH = 44;
    private boolean mobile;

    public ClickGuiScreen() {
        super(Text.literal("Jay"));
        // Prefer last profile side: utility → World, else Combat
        String lp = ClientSettings.lastProfile;
        if (lp != null && (lp.equals("scout") || lp.equals("builder") || lp.equals("explore") || lp.equals("utility"))) {
            selected = Module.Category.WORLD;
        } else {
            selected = Module.Category.COMBAT;
        }
        if (JayHackClient.moduleManager != null
                && JayHackClient.moduleManager.getByCategory(selected).isEmpty()) {
            for (Module.Category c : Module.Category.values()) {
                if (!JayHackClient.moduleManager.getByCategory(c).isEmpty()) {
                    selected = c;
                    break;
                }
            }
        }
    }

    private void computeLayout() {
        mobile = this.width < 480 || this.height < 360;
        if (mobile) {
            winW = Math.max(this.width - 8, 210);
            winH = Math.max(this.height - 8, 190);
            rowH = 42;
            headerH = 32;
            searchH = 28;
            tabH = 26;
            profileH = 24;
        } else {
            winW = Math.min(340, this.width - 40);
            winH = Math.min(440, this.height - 36);
            rowH = 44;
            headerH = 36;
            searchH = 28;
            tabH = 28;
            profileH = 26;
        }
        winX = (this.width - winW) / 2;
        winY = (this.height - winH) / 2;
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

    private List<Module.Category> visibleCategories() {
        List<Module.Category> out = new ArrayList<>();
        if (JayHackClient.moduleManager == null) return out;
        // Combat + World first (dual client)
        for (Module.Category prefer : new Module.Category[]{
                Module.Category.COMBAT, Module.Category.WORLD,
                Module.Category.MOVEMENT, Module.Category.RENDER,
                Module.Category.PLAYER, Module.Category.MISC}) {
            if (!JayHackClient.moduleManager.getByCategory(prefer).isEmpty()) out.add(prefer);
        }
        return out;
    }

    private void fillRound(DrawContext ctx, int x1, int y1, int x2, int y2, int color) {
        ctx.fill(x1 + 2, y1, x2 - 2, y2, color);
        ctx.fill(x1, y1 + 2, x2, y2 - 2, color);
        ctx.fill(x1 + 1, y1 + 1, x2 - 1, y2 - 1, color);
    }

    private void drawToggle(DrawContext ctx, int x, int y, boolean on) {
        int tw = mobile ? 30 : 34;
        int th = mobile ? 16 : 18;
        int bg = on ? ACCENT : TOGGLE_OFF;
        ctx.fill(x + 1, y, x + tw - 1, y + th, bg);
        ctx.fill(x, y + 1, x + tw, y + th - 1, bg);
        int kn = th - 4;
        int kx = on ? x + tw - kn - 2 : x + 2;
        ctx.fill(kx, y + 2, kx + kn, y + 2 + kn, 0xFFFFFFFF);
    }

    private void applyProfile(String name) {
        switch (name.toLowerCase(Locale.ROOT)) {
            case "sword" -> LegitProfile.applySword();
            case "scout" -> LegitProfile.applyScout();
            case "nethpot" -> LegitProfile.applyNethpot();
            case "builder" -> LegitProfile.applyBuilder();
            case "explore" -> LegitProfile.applyExplore();
            default -> { return; }
        }
        if (name.equalsIgnoreCase("scout") || name.equalsIgnoreCase("builder") || name.equalsIgnoreCase("explore")) {
            selected = Module.Category.WORLD;
        } else {
            selected = Module.Category.COMBAT;
        }
        if (JayHackClient.configManager != null) JayHackClient.configManager.save();
        if (client != null && client.player != null) {
            client.player.sendMessage(Text.literal("§8[§bJay§8] §aProfile §f" + name), false);
        }
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        computeLayout();
        int x = winX;
        int y = winY;

        ctx.fill(0, 0, this.width, this.height, BG_OVERLAY);
        ctx.fill(x + 3, y + 4, x + winW + 3, y + winH + 4, 0x44000000);
        fillRound(ctx, x, y, x + winW, y + winH, BG_WINDOW);

        int logoSize = mobile ? 18 : 20;
        JayLogo.draw(ctx, x + 10, y + (headerH - logoSize) / 2, logoSize);
        ctx.drawTextWithShadow(textRenderer, "§fJay", x + 12 + logoSize, y + (headerH / 2) - 4, TEXT);

        boolean frozen = JayHackClient.moduleManager != null && JayHackClient.moduleManager.isFrozen();
        String status = frozen ? "FROZEN" : "READY";
        int statusColor = frozen ? 0xFFFF5555 : ACCENT;
        int sx = x + winW - 28 - textRenderer.getWidth(status) - 12;
        int sy = y + (headerH / 2) - 6;
        ctx.fill(sx, sy + 4, sx + 5, sy + 9, statusColor);
        ctx.drawTextWithShadow(textRenderer, status, sx + 10, sy + 1, statusColor);

        int closeX = x + winW - 20;
        int closeY = y + (headerH / 2) - 6;
        boolean hoverClose = mouseX >= closeX - 4 && mouseX <= closeX + 12 && mouseY >= closeY - 2 && mouseY <= closeY + 12;
        ctx.drawTextWithShadow(textRenderer, "×", closeX, closeY, hoverClose ? 0xFFFF8888 : TEXT_DIM);

        int searchY = y + headerH + 2;
        int searchX = x + 10;
        int searchW = winW - 20;
        fillRound(ctx, searchX, searchY, searchX + searchW, searchY + searchH - 4, BG_SEARCH);
        if (searchFocused) {
            ctx.fill(searchX + 2, searchY + searchH - 6, searchX + searchW - 2, searchY + searchH - 5, ACCENT);
        }

        String searchShown;
        if (friendsTab) {
            searchShown = friendInput.isEmpty() && !searchFocused ? "§8Add friend..."
                    : "§f" + friendInput + (searchFocused ? "§7|" : "");
        } else {
            searchShown = search.isEmpty() && !searchFocused ? "§8Search modules..."
                    : "§f" + search + (searchFocused ? "§7|" : "");
        }
        ctx.drawTextWithShadow(textRenderer, searchShown, searchX + 8, searchY + 5, TEXT);

        int tabY = searchY + searchH;
        int tabX = x + 10;
        boolean searching = search != null && !search.isBlank();

        for (Module.Category cat : visibleCategories()) {
            String label = mobile && cat.displayName.length() > 6
                    ? cat.displayName.substring(0, 4)
                    : cat.displayName;
            int pw = textRenderer.getWidth(label) + 12;
            if (tabX + pw > x + winW - 10) break;

            boolean on = !friendsTab && !searching && cat == selected;
            boolean hover = mouseX >= tabX && mouseX < tabX + pw && mouseY >= tabY && mouseY < tabY + tabH - 6;
            if (on) {
                fillRound(ctx, tabX, tabY, tabX + pw, tabY + tabH - 8, PILL_ON);
                ctx.fill(tabX + 3, tabY + tabH - 10, tabX + pw - 3, tabY + tabH - 9, ACCENT);
            } else if (hover) {
                fillRound(ctx, tabX, tabY, tabX + pw, tabY + tabH - 8, PILL_BG);
            }
            ctx.drawTextWithShadow(textRenderer, label, tabX + 6, tabY + 4, on ? ACCENT : (hover ? TEXT : TEXT_DIM));
            tabX += pw + 3;
        }

        {
            String label = "Friends";
            int pw = textRenderer.getWidth(label) + 12;
            if (tabX + pw <= x + winW - 10) {
                boolean on = friendsTab;
                boolean hover = mouseX >= tabX && mouseX < tabX + pw && mouseY >= tabY && mouseY < tabY + tabH - 6;
                if (on) {
                    fillRound(ctx, tabX, tabY, tabX + pw, tabY + tabH - 8, PILL_ON);
                    ctx.fill(tabX + 3, tabY + tabH - 10, tabX + pw - 3, tabY + tabH - 9, ACCENT);
                } else if (hover) {
                    fillRound(ctx, tabX, tabY, tabX + pw, tabY + tabH - 8, PILL_BG);
                }
                ctx.drawTextWithShadow(textRenderer, label, tabX + 6, tabY + 4, on ? ACCENT : (hover ? TEXT : TEXT_DIM));
            }
        }

        int profileY = y + winH - profileH - 6;
        int listBottom = profileY - 4;
        int listTop = tabY + tabH;
        int maxRows = Math.max(1, (listBottom - listTop) / rowH);

        int px = x + 10;
        int gap = 4;
        int btnW = (winW - 20 - gap * (PROFILES.length - 1)) / PROFILES.length;
        for (String prof : PROFILES) {
            boolean on = ClientSettings.lastProfile != null
                    && ClientSettings.lastProfile.equalsIgnoreCase(prof);
            boolean hover = mouseX >= px && mouseX < px + btnW && mouseY >= profileY && mouseY < profileY + profileH;
            int bg = on ? 0xFF1A2A32 : (hover ? 0xFF1A1A24 : 0xFF121218);
            fillRound(ctx, px, profileY, px + btnW, profileY + profileH, bg);
            if (on) ctx.fill(px + 4, profileY + profileH - 2, px + btnW - 4, profileY + profileH - 1, ACCENT);
            int tw = textRenderer.getWidth(prof);
            ctx.drawTextWithShadow(textRenderer, prof, px + (btnW - tw) / 2, profileY + profileH / 2 - 4,
                    on ? ACCENT : TEXT_DIM);
            px += btnW + gap;
        }

        if (friendsTab) {
            List<String> friends = friendList();
            for (int i = 0; i < friends.size(); i++) {
                if (i < scroll) continue;
                int di = i - scroll;
                if (di >= maxRows) break;
                String name = friends.get(i);
                int ry = listTop + di * rowH;
                boolean hover = mouseX >= x + 8 && mouseX < x + winW - 8 && mouseY >= ry && mouseY < ry + rowH;
                if (hover) fillRound(ctx, x + 8, ry + 1, x + winW - 8, ry + rowH - 2, BG_ROW);
                ctx.drawTextWithShadow(textRenderer, name, x + 14, ry + rowH / 2 - 4, TEXT);
                ctx.drawTextWithShadow(textRenderer, "§c×", x + winW - 22, ry + rowH / 2 - 4, 0xFFFF6666);
            }
        } else {
            List<Module> modules = filteredModules();
            for (int i = 0; i < modules.size(); i++) {
                if (i < scroll) continue;
                int di = i - scroll;
                if (di >= maxRows) break;

                Module m = modules.get(i);
                int ry = listTop + di * rowH;
                boolean hover = mouseX >= x + 8 && mouseX < x + winW - 8 && mouseY >= ry && mouseY < ry + rowH;

                if (m.isEnabled() || hover) {
                    fillRound(ctx, x + 8, ry + 1, x + winW - 8, ry + rowH - 2,
                            m.isEnabled() ? 0xFF121820 : BG_ROW);
                }

                String title = m.getName();
                ctx.drawTextWithShadow(textRenderer, title, x + 14, mobile ? ry + rowH / 2 - 4 : ry + 8,
                        m.isEnabled() ? TEXT : 0xFFC8C8D4);

                String keyLab = m.getKeyLabel();
                if (!keyLab.isEmpty()) {
                    int nx = x + 14 + textRenderer.getWidth(title) + 4;
                    ctx.drawTextWithShadow(textRenderer, "§8[§b" + keyLab + "§8]", nx,
                            mobile ? ry + rowH / 2 - 4 : ry + 8, TEXT_DIM);
                }

                if (!mobile) {
                    String desc = m.getDescription();
                    if (desc.length() > 28) desc = desc.substring(0, 25) + "...";
                    ctx.drawTextWithShadow(textRenderer, desc, x + 14, ry + 22, TEXT_DIM);
                }

                int tx = x + winW - (mobile ? 46 : 50);
                int ty = ry + (rowH - (mobile ? 16 : 18)) / 2;
                drawToggle(ctx, tx, ty, m.isEnabled());
            }
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

        int closeX = x + winW - 20;
        int closeY = y + (headerH / 2) - 6;
        if (button == 0 && mouseX >= closeX - 4 && mouseX <= closeX + 12 && mouseY >= closeY - 2 && mouseY <= closeY + 12) {
            close();
            return true;
        }

        int profileY = y + winH - profileH - 6;
        if (button == 0 && mouseY >= profileY && mouseY < profileY + profileH) {
            int px = x + 10;
            int gap = 4;
            int btnW = (winW - 20 - gap * (PROFILES.length - 1)) / PROFILES.length;
            for (String prof : PROFILES) {
                if (mouseX >= px && mouseX < px + btnW) {
                    applyProfile(prof);
                    return true;
                }
                px += btnW + gap;
            }
        }

        if (button == 0) {
            int searchY = y + headerH + 2;
            int searchX = x + 10;
            int searchW = winW - 20;
            if (mouseX >= searchX && mouseX <= searchX + searchW && mouseY >= searchY && mouseY <= searchY + searchH - 4) {
                searchFocused = true;
                return true;
            } else {
                searchFocused = false;
            }

            int tabY = searchY + searchH;
            int tabX = x + 10;
            for (Module.Category cat : visibleCategories()) {
                String label = mobile && cat.displayName.length() > 6
                        ? cat.displayName.substring(0, 4)
                        : cat.displayName;
                int pw = textRenderer.getWidth(label) + 12;
                if (tabX + pw > x + winW - 10) break;
                if (mouseX >= tabX && mouseX < tabX + pw && mouseY >= tabY && mouseY < tabY + tabH - 6) {
                    selected = cat;
                    friendsTab = false;
                    search = "";
                    friendInput = "";
                    scroll = 0;
                    return true;
                }
                tabX += pw + 3;
            }
            {
                String label = "Friends";
                int pw = textRenderer.getWidth(label) + 12;
                if (tabX + pw <= x + winW - 10
                        && mouseX >= tabX && mouseX < tabX + pw && mouseY >= tabY && mouseY < tabY + tabH - 6) {
                    friendsTab = true;
                    searchFocused = true;
                    scroll = 0;
                    return true;
                }
            }
        }

        int searchY = y + headerH + 2;
        int listTop = searchY + searchH + tabH;
        int listBottom = profileY - 4;
        int maxRows = Math.max(1, (listBottom - listTop) / rowH);

        if (friendsTab && button == 0) {
            List<String> friends = friendList();
            for (int i = 0; i < friends.size(); i++) {
                if (i < scroll) continue;
                int di = i - scroll;
                if (di >= maxRows) break;
                int ry = listTop + di * rowH;
                if (mouseX >= x + 8 && mouseX < x + winW - 8 && mouseY >= ry && mouseY < ry + rowH) {
                    JayHackClient.friendManager.remove(friends.get(i));
                    if (JayHackClient.configManager != null) JayHackClient.configManager.save();
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
                int ry = listTop + di * rowH;
                if (mouseX >= x + 8 && mouseX < x + winW - 8 && mouseY >= ry && mouseY < ry + rowH) {
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
        int searchY = winY + headerH + 2;
        int listTop = searchY + searchH + tabH;
        int profileY = winY + winH - profileH - 6;
        int listBottom = profileY - 4;
        int maxRows = Math.max(1, (listBottom - listTop) / rowH);
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
                    JayHackClient.friendManager.add(friendInput.trim());
                    if (JayHackClient.configManager != null) JayHackClient.configManager.save();
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
