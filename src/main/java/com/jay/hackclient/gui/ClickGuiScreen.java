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
 * Simple dark ClickGUI — open with Right Shift, close with Escape / Right Shift / Close button.
 */
public class ClickGuiScreen extends Screen {

    private static final int PANEL_W = 130;
    private static final int HEADER_H = 18;
    private static final int ROW_H = 14;
    private static final int PAD = 8;

    private final List<Panel> panels = new ArrayList<>();
    private Panel dragging;
    private int dragOffX, dragOffY;

    public ClickGuiScreen() {
        super(Text.literal("Jay ClickGUI"));
        buildPanels();
    }

    private void buildPanels() {
        panels.clear();
        int startX = 40;
        int startY = 40;
        int i = 0;
        for (Module.Category cat : Module.Category.values()) {
            List<Module> mods = JayHackClient.moduleManager.getByCategory(cat);
            if (mods.isEmpty()) continue;
            panels.add(new Panel(cat, mods, startX + i * (PANEL_W + PAD), startY));
            i++;
            if (i >= 4) {
                i = 0;
                startY += 200;
            }
        }
    }

    @Override
    public void render(DrawContext context, int mouseX, int mouseY, float delta) {
        // Dim background
        context.fill(0, 0, this.width, this.height, 0x99000000);

        // Title bar
        String title = "Jay's Hack Client  v" + JayHackClient.VERSION;
        int tw = textRenderer.getWidth(title);
        context.fill(this.width / 2 - tw / 2 - 12, 8, this.width / 2 + tw / 2 + 12, 24, 0xEE101018);
        context.drawTextWithShadow(textRenderer, title, this.width / 2 - tw / 2, 12, 0x55CCFF);

        String hint = "RShift / ESC close  ·  click module to toggle";
        context.drawTextWithShadow(textRenderer, hint, this.width / 2 - textRenderer.getWidth(hint) / 2, 28, 0x888888);

        for (Panel p : panels) {
            p.render(context, mouseX, mouseY, textRenderer);
        }

        // Close button top-right
        int cx = this.width - 60;
        int cy = 10;
        boolean hoverClose = mouseX >= cx && mouseX <= cx + 50 && mouseY >= cy && mouseY <= cy + 16;
        context.fill(cx, cy, cx + 50, cy + 16, hoverClose ? 0xFFAA3333 : 0xFF662222);
        context.drawCenteredTextWithShadow(textRenderer, "Close", cx + 25, cy + 4, 0xFFFFFF);

        super.render(context, mouseX, mouseY, delta);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Close button
        int cx = this.width - 60;
        int cy = 10;
        if (mouseX >= cx && mouseX <= cx + 50 && mouseY >= cy && mouseY <= cy + 16) {
            close();
            return true;
        }

        if (button == 0) {
            for (int i = panels.size() - 1; i >= 0; i--) {
                Panel p = panels.get(i);
                if (p.mouseClicked(mouseX, mouseY)) {
                    // move to front
                    panels.remove(i);
                    panels.add(p);
                    if (p.draggingHeader) {
                        dragging = p;
                        dragOffX = (int) mouseX - p.x;
                        dragOffY = (int) mouseY - p.y;
                    }
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        dragging = null;
        for (Panel p : panels) p.draggingHeader = false;
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dx, double dy) {
        if (dragging != null) {
            dragging.x = (int) mouseX - dragOffX;
            dragging.y = (int) mouseY - dragOffY;
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dx, dy);
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
        return false; // keep world running behind GUI
    }

    // ── Panel ──────────────────────────────────────────────

    private static class Panel {
        final Module.Category category;
        final List<Module> modules;
        int x, y;
        boolean open = true;
        boolean draggingHeader = false;

        Panel(Module.Category category, List<Module> modules, int x, int y) {
            this.category = category;
            this.modules = modules;
            this.x = x;
            this.y = y;
        }

        int height() {
            return HEADER_H + (open ? modules.size() * ROW_H : 0) + 2;
        }

        void render(DrawContext ctx, int mx, int my, net.minecraft.client.font.TextRenderer tr) {
            int h = height();
            // shadow
            ctx.fill(x + 2, y + 2, x + PANEL_W + 2, y + h + 2, 0x44000000);
            // body
            ctx.fill(x, y, x + PANEL_W, y + h, 0xEE0C0C14);
            // header
            int headerColor = categoryColor(category);
            ctx.fill(x, y, x + PANEL_W, y + HEADER_H, headerColor);
            ctx.drawTextWithShadow(tr, category.displayName, x + 6, y + 5, 0xFFFFFF);
            ctx.drawTextWithShadow(tr, open ? "-" : "+", x + PANEL_W - 12, y + 5, 0xCCCCCC);

            if (!open) return;

            int rowY = y + HEADER_H;
            for (Module m : modules) {
                boolean hover = mx >= x && mx <= x + PANEL_W && my >= rowY && my < rowY + ROW_H;
                int bg = m.isEnabled() ? 0x4422AA66 : (hover ? 0x33FFFFFF : 0x00000000);
                if (bg != 0) ctx.fill(x + 1, rowY, x + PANEL_W - 1, rowY + ROW_H, bg);

                String mark = m.isEnabled() ? "§a● " : "§8○ ";
                ctx.drawTextWithShadow(tr, mark + "§f" + m.getName(), x + 6, rowY + 3, 0xFFFFFF);
                rowY += ROW_H;
            }
            // bottom accent
            ctx.fill(x, y + h - 1, x + PANEL_W, y + h, headerColor);
        }

        boolean mouseClicked(double mx, double my) {
            if (mx < x || mx > x + PANEL_W || my < y || my > y + height()) return false;

            // header → drag or collapse
            if (my < y + HEADER_H) {
                if (mx > x + PANEL_W - 18) {
                    open = !open;
                } else {
                    draggingHeader = true;
                }
                return true;
            }

            if (!open) return true;

            int rowY = y + HEADER_H;
            for (Module m : modules) {
                if (my >= rowY && my < rowY + ROW_H) {
                    m.toggle();
                    return true;
                }
                rowY += ROW_H;
            }
            return true;
        }

        private static int categoryColor(Module.Category c) {
            return switch (c) {
                case COMBAT -> 0xDD882222;
                case MOVEMENT -> 0xDD228822;
                case RENDER -> 0xDD226688;
                case PLAYER -> 0xDD886622;
                case WORLD -> 0xDD4444AA;
                case MISC -> 0xDD662266;
            };
        }
    }
}
