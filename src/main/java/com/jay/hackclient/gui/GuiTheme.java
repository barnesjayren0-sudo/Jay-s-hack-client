package com.jay.hackclient.gui;

/** Single place to change GUI accent / palette. */
public final class GuiTheme {

    private GuiTheme() {}

    /** Primary accent — cyan */
    public static int ACCENT = 0xFF3DDCFF;
    /** Secondary accent — soft purple */
    public static int ACCENT2 = 0xFF9B7BFF;

    public static final int BG = 0xF00A0C11;
    public static final int PANEL = 0xF0121520;
    public static final int PANEL2 = 0xF0171D2A;
    public static final int ROW = 0xFF111721;
    public static final int ROW_HOVER = 0xFF192333;
    public static final int ROW_ON = 0xFF122B36;
    public static final int TEXT = 0xFFF4F7FB;
    public static final int TEXT_DIM = 0xFF98A3B8;
    public static final int MUTED = 0xFF657086;
    public static final int SUCCESS = 0xFF53E6A4;
    public static final int DANGER = 0xFFFF6B7A;
    public static final int BORDER = 0x30FFFFFF;
    public static final int TOGGLE_OFF = 0xFF2A3445;
    public static final int SHADOW = 0x70000000;
    public static final int OVERLAY = 0xA0000000;
    public static final int GLASS = 0x551E2736;

    public static int withAlpha(int rgb, int alpha) {
        return (alpha << 24) | (rgb & 0x00FFFFFF);
    }
}
