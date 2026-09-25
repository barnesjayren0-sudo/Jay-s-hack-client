package com.jay.hackclient.gui;

import com.jay.hackclient.util.Animation;
import com.jay.hackclient.util.RenderUtil;
import net.minecraft.client.gui.DrawContext;

/**
 * Premium visual theme for JAY CLIENT ClickGUI + HUD.
 * Reference: paid Ghost clients (Prestige, Grave, Elusive) + user purple screenshot.
 *
 * Accent = soft purple (#9B6BFF)
 * Background = near-black with blue undertone
 * Animations = EaseOutBack / EaseOutCubic
 */
public final class PremiumTheme {

    public static final int ACCENT       = 0xFF9B6BFF;
    public static final int ACCENT_HOVER = 0xFFB794FF;
    public static final int ACCENT_DARK  = 0xFF5C3A9E;
    public static final int BG           = 0xF00C0C12;
    public static final int BG_PANEL     = 0xF012121A;
    public static final int BG_MODULE    = 0xE8161620;
    public static final int BG_ENABLED   = 0xE82A1F3D;
    public static final int TEXT         = 0xFFFFFFFF;
    public static final int TEXT_DIM     = 0xFF9A9AAA;
    public static final int BORDER       = 0x28FFFFFF;
    public static final int SUCCESS      = 0xFF55FF88;
    public static final int DANGER       = 0xFFFF5555;

    // Global GUI open animation (shared)
    public static final Animation GUI_OPEN = new Animation(280, 1.0, Animation.Easing.EASE_OUT_BACK);

    private PremiumTheme() {}

    public static void onGuiOpen() {
        GUI_OPEN.reset();
        GUI_OPEN.setDirection(Animation.Direction.FORWARDS);
    }

    public static void onGuiClose() {
        GUI_OPEN.setDirection(Animation.Direction.BACKWARDS);
    }

    public static float guiAnim() {
        return GUI_OPEN.getOutputF();
    }

    /** Draw a premium category panel header */
    public static void drawPanelHeader(DrawContext ctx, float x, float y, float w, float h, String title, boolean accentBar) {
        RenderUtil.drawRoundedRect(ctx, x, y, w, h, 5f, BG_PANEL);
        if (accentBar) {
            RenderUtil.drawRect(ctx, x, y, 3f, h, ACCENT);
        }
        // Title drawn by caller with font
    }

    /** Module row background */
    public static int moduleBg(boolean enabled, boolean hovered) {
        if (enabled) return hovered ? RenderUtil.withAlpha(BG_ENABLED, 1f) : BG_ENABLED;
        return hovered ? RenderUtil.withAlpha(BG_MODULE, 0.95f) : BG_MODULE;
    }

    /** Toggle indicator color */
    public static int toggleColor(boolean enabled) {
        return enabled ? ACCENT : 0xFF3A3A48;
    }

    public static String name() {
        return "Premium Purple";
    }
}
