package com.jay.hackclient.gui;

/**
 * Vape V4–inspired palette for Jay Utility Client.
 * Near-black panels, subtle separation, single soft accent.
 * Accent is sourced from GuiColors so the accent picker in the toolbar drives it.
 */
public final class VapeTheme {

    private VapeTheme() {}

    // Core surfaces (slight alpha so the world shows through faintly)
    public static final int BG_BACKDROP = 0x8E000000;
    public static final int BG_PANEL = 0xF20D0D11;
    public static final int BG_HEADER = 0xF2121218;
    public static final int BG_MODULE = 0xE8161620;
    public static final int BG_ENABLED = 0xE81E1730;
    public static final int BG_SUNKEN = 0xE8101016;

    // Text
    public static final int TEXT_FIXED = 0xFFF2F2F6;
    public static final int TEXT_DIM_FIXED = 0xFF8E8EA0;

    /** Accent from the accent picker (GuiColors), falls back to soft purple. */
    public static int ACCENT() {
        try { return GuiColors.accent; } catch (Throwable t) { return 0xFF9B6BFF; }
    }

    public static int ACCENT_TEXT() {
        try { return GuiColors.accentHover; } catch (Throwable t) { return 0xFFB794FF; }
    }

    public static int TEXT() { return TEXT_FIXED; }

    public static int TEXT_DIM() { return TEXT_DIM_FIXED; }
}