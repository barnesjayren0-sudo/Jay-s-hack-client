package com.jay.hackclient.gui;

/** Palette — mutable so ThemeEngine can recolor at runtime. */
public final class GuiTheme {

    private GuiTheme() {}

    public static int ACCENT = 0xFF3DDCFF;
    public static int ACCENT2 = 0xFF9B7BFF;

    public static int BG = 0xF00A0C11;
    public static int PANEL = 0xF0121520;
    public static int PANEL2 = 0xF0171D2A;
    public static int ROW = 0xFF111721;
    public static int ROW_HOVER = 0xFF192333;
    public static int ROW_ON = 0xFF122B36;
    public static int TEXT = 0xFFF4F7FB;
    public static int TEXT_DIM = 0xFF98A3B8;
    public static int MUTED = 0xFF657086;
    public static int SUCCESS = 0xFF53E6A4;
    public static int DANGER = 0xFFFF6B7A;
    public static int BORDER = 0x30FFFFFF;
    public static int TOGGLE_OFF = 0xFF2A3445;
    public static int SHADOW = 0x70000000;
    public static int OVERLAY = 0xA0000000;
    public static int GLASS = 0x551E2736;

    public static int withAlpha(int rgb, int alpha) {
        return (alpha << 24) | (rgb & 0x00FFFFFF);
    }
}
