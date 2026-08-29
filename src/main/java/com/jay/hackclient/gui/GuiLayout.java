package com.jay.hackclient.gui;

import com.jay.hackclient.module.Module;

import java.util.EnumMap;
import java.util.Map;

/** Persisted floating panel positions for ClickGUI. */
public final class GuiLayout {

    public static final Map<Module.Category, float[]> POS = new EnumMap<>(Module.Category.class);
    public static boolean loaded;

    private GuiLayout() {}

    public static void ensureDefaults() {
        if (!POS.isEmpty()) return;
        Module.Category[] cats = Module.Category.values();
        int gap = 8;
        int startX = 12;
        int startY = 28;
        int panelW = 118;
        for (int i = 0; i < cats.length; i++) {
            float x = startX + (i % 4) * (panelW + gap);
            float y = startY + (i / 4) * 160;
            POS.put(cats[i], new float[]{x, y});
        }
    }

    public static void set(Module.Category cat, float x, float y) {
        POS.put(cat, new float[]{x, y});
    }

    public static float[] get(Module.Category cat) {
        ensureDefaults();
        return POS.computeIfAbsent(cat, c -> new float[]{20, 40});
    }
}
