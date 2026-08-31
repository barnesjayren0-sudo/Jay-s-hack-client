package com.jay.hackclient.gui;

import com.jay.hackclient.JayHackClient;
import com.jay.hackclient.render.HudLayout;
import net.minecraft.client.gui.Click;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.input.KeyInput;
import net.minecraft.text.Text;
import org.lwjgl.glfw.GLFW;

/** Drag HUD elements (FPS, ping, coords, etc.). */
public class HudEditorScreen extends Screen {

    private final Screen parent;
    private HudLayout.Element drag;
    private float ox, oy;

    public HudEditorScreen(Screen parent) {
        super(Text.literal("HUD Editor"));
        this.parent = parent;
    }

    @Override
    public void render(DrawContext ctx, int mouseX, int mouseY, float delta) {
        ctx.fill(0, 0, width, height, GuiTheme.OVERLAY);
        ctx.drawTextWithShadow(textRenderer, "§bHUD Editor §7· drag boxes · R-click toggle · Esc save",
                8, 6, GuiTheme.TEXT);

        for (HudLayout.Element e : HudLayout.ELEMENTS.values()) {
            int x = resolveX(e);
            int y = (int) e.y;
            int w = Math.max(48, textRenderer.getWidth(e.label) + 12);
            int h = 14;
            int bg = e.visible ? GuiTheme.ROW_ON : GuiTheme.PANEL2;
            ctx.fill(x, y, x + w, y + h, bg);
            ctx.fill(x, y, x + 2, y + h, e.visible ? GuiTheme.ACCENT : GuiTheme.MUTED);
            ctx.drawTextWithShadow(textRenderer, e.label, x + 5, y + 3,
                    e.visible ? GuiTheme.TEXT : GuiTheme.TEXT_DIM);
        }
    }

    private int resolveX(HudLayout.Element e) {
        if (e.x < 0) return width + (int) e.x;
        return (int) e.x;
    }

    @Override
    public boolean mouseClicked(Click click, boolean doubled) {
        double mx = click.x(), my = click.y();
        int button = click.button();
        for (HudLayout.Element e : HudLayout.ELEMENTS.values()) {
            int x = resolveX(e);
            int y = (int) e.y;
            int w = Math.max(48, textRenderer.getWidth(e.label) + 12);
            if (mx >= x && mx <= x + w && my >= y && my <= y + 14) {
                if (button == 1) {
                    e.visible = !e.visible;
                    return true;
                }
                if (button == 0) {
                    drag = e;
                    ox = (float) (mx - x);
                    oy = (float) (my - y);
                    return true;
                }
            }
        }
        return super.mouseClicked(click, doubled);
    }

    @Override
    public boolean mouseDragged(Click click, double dx, double dy) {
        if (drag != null) {
            float nx = (float) (click.x() - ox);
            float ny = (float) (click.y() - oy);
            // store right-anchored if near right edge
            if (nx > width * 0.65f) {
                drag.x = nx - width;
            } else {
                drag.x = Math.max(0, nx);
            }
            drag.y = Math.max(0, Math.min(height - 16, ny));
            return true;
        }
        return super.mouseDragged(click, dx, dy);
    }

    @Override
    public boolean mouseReleased(Click click) {
        if (drag != null) {
            drag = null;
            if (JayHackClient.configManager != null) JayHackClient.configManager.save();
            return true;
        }
        return super.mouseReleased(click);
    }

    @Override
    public boolean keyPressed(KeyInput input) {
        if (input.key() == GLFW.GLFW_KEY_ESCAPE) {
            if (JayHackClient.configManager != null) JayHackClient.configManager.save();
            client.setScreen(parent);
            return true;
        }
        return super.keyPressed(input);
    }

    @Override
    public boolean shouldPause() { return false; }
}
