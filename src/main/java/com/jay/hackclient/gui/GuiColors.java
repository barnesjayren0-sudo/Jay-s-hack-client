package com.jay.hackclient.gui;

import com.jay.hackclient.util.RenderUtil;

/** Customizable ClickGUI colors (Orchard-inspired dark + accent). */
public final class GuiColors {

    public static int accent      = 0xFF9B6BFF;
    public static int accentHover = 0xFFB794FF;
    public static int accentDark  = 0xFF5C3A9E;
    public static int background  = 0xF00C0C12;
    public static int panel       = 0xF012121A;
    public static int moduleBg    = 0xE8161620;
    public static int moduleOn    = 0xE82A1F3D;
    public static int text        = 0xFFFFFFFF;
    public static int textDim     = 0xFF9A9AAA;

    private GuiColors() {}

    public static void setAccent(int rgb) {
        accent = 0xFF000000 | (rgb & 0xFFFFFF);
        int r = (accent >> 16) & 0xFF;
        int g = (accent >> 8) & 0xFF;
        int b = accent & 0xFF;
        accentHover = 0xFF000000
                | (Math.min(255, r + 30) << 16)
                | (Math.min(255, g + 30) << 8)
                | Math.min(255, b + 30);
        accentDark = 0xFF000000
                | (Math.max(0, r - 50) << 16)
                | (Math.max(0, g - 50) << 8)
                | Math.max(0, b - 50);
        try {
            PremiumTheme.ACCENT = accent;
            PremiumTheme.ACCENT_HOVER = accentHover;
            PremiumTheme.ACCENT_DARK = accentDark;
        } catch (Throwable ignored) {}
        try {
            RenderUtil.ACCENT = accent;
            RenderUtil.ACCENT_DARK = accentDark;
        } catch (Throwable ignored) {}
    }

    public static void setAccentRGB(int r, int g, int b) {
        setAccent((r << 16) | (g << 8) | b);
    }

    public static void setAccentHex(String hex) {
        String h = hex.trim().replace("#", "");
        if (h.length() == 6) {
            try { setAccent(Integer.parseInt(h, 16)); } catch (NumberFormatException ignored) {}
        }
    }

    public static void applyPreset(String name) {
        switch (name.toLowerCase()) {
            case "purple", "orchard", "default" -> setAccent(0x9B6BFF);
            case "blue", "cyan" -> setAccent(0x4DA6FF);
            case "red", "combat" -> setAccent(0xFF5555);
            case "green", "mint" -> setAccent(0x55FF99);
            case "orange", "amber" -> setAccent(0xFFAA44);
            case "pink", "rose" -> setAccent(0xFF6BCB);
            case "white", "light" -> setAccent(0xE0E0E0);
            case "gold" -> setAccent(0xFFD700);
            default -> setAccent(0x9B6BFF);
        }
    }

    public static String accentHex() {
        return String.format("#%06X", accent & 0xFFFFFF);
    }
}
