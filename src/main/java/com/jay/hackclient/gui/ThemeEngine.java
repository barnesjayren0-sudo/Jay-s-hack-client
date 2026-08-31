package com.jay.hackclient.gui;

/**
 * Theme engine — Cyan / Purple / Red / Green / Custom.
 * Applies palette into GuiTheme statics.
 */
public final class ThemeEngine {

    public enum Theme {
        CYAN, PURPLE, RED, GREEN, CUSTOM
    }

    public static Theme current = Theme.CYAN;
    public static int customAccent = 0xFF3DDCFF;
    public static float bgOpacity = 0.94f;
    public static int cornerRadius = 6;
    public static boolean blur = false;
    public static float animSpeed = 1.0f;

    private ThemeEngine() {}

    public static void apply(Theme t) {
        current = t == null ? Theme.CYAN : t;
        int accent;
        int accent2;
        switch (current) {
            case PURPLE -> { accent = 0xFFB07CFF; accent2 = 0xFF7B5CFF; }
            case RED -> { accent = 0xFFFF5C6A; accent2 = 0xFFFF8A6B; }
            case GREEN -> { accent = 0xFF53E6A4; accent2 = 0xFF3DDCFF; }
            case CUSTOM -> { accent = customAccent | 0xFF000000; accent2 = 0xFF9B7BFF; }
            default -> { accent = 0xFF3DDCFF; accent2 = 0xFF9B7BFF; }
        }
        GuiTheme.ACCENT = accent;
        GuiTheme.ACCENT2 = accent2;

        int a = Math.max(0x80, Math.min(0xFF, (int) (bgOpacity * 255)));
        GuiTheme.BG = GuiTheme.withAlpha(0x0A0C11, a);
        GuiTheme.PANEL = GuiTheme.withAlpha(0x121520, a);
        GuiTheme.PANEL2 = GuiTheme.withAlpha(0x171D2A, a);
    }

    public static void cycle() {
        Theme[] all = Theme.values();
        int i = (current.ordinal() + 1) % all.length;
        apply(all[i]);
    }

    public static String name() {
        return current.name().charAt(0) + current.name().substring(1).toLowerCase();
    }

    public static void loadFromConfig(String theme, float opacity, int radius, float anim, int custom) {
        try {
            customAccent = custom;
            bgOpacity = Math.max(0.4f, Math.min(1f, opacity));
            cornerRadius = Math.max(0, Math.min(16, radius));
            animSpeed = Math.max(0.4f, Math.min(2f, anim));
            apply(Theme.valueOf(theme.toUpperCase()));
        } catch (Exception e) {
            apply(Theme.CYAN);
        }
    }
}
