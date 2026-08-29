package com.jay.hackclient.gui;

/** Single place to change GUI accent / palette. */
public final class GuiTheme {

    private GuiTheme() {}

    /** Primary accent — cyan */
    public static int ACCENT = 0xFF3DDCFF;
    /** Secondary accent — soft purple */
    public static int ACCENT2 = 0xFF9B7BFF;

    public static final int BG = 0xF00B0D12;
    public static final int PANEL = 0xF012151C;
    public static final int PANEL2 = 0xF0171B24;
    public static final int ROW = 0xFF141822;
    public static final int ROW_HOVER = 0xFF1A2030;
    public static final int ROW_ON = 0xFF152532;
    public static final int TEXT = 0xFFF1F3F8;
    public static final int TEXT_DIM = 0xFF8E96A8;
    public static final int BORDER = 0x22FFFFFF;
    public static final int TOGGLE_OFF = 0xFF2A3040;
    public static final int SHADOW = 0x66000000;
    public static final int OVERLAY = 0x99000000;

    public static int withAlpha(int rgb, int alpha) {
        return (alpha << 24) | (rgb & 0x00FFFFFF);
    }
}
