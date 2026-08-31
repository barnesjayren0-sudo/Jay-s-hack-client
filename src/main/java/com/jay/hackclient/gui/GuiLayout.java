package com.jay.hackclient.gui;

import com.jay.hackclient.module.Module;

import java.util.EnumMap;
import java.util.Map;

/** Persisted floating panel positions — compact defaults for mobile. */
public final class GuiLayout {

    public static final Map<Module.Category, float[]> POS = new EnumMap<>(Module.Category.class);
    public static boolean loaded;

    /** Match ClickGuiScreen.PANEL_W */
    public static final int PANEL_W = 98;

    private GuiLayout() {}

    public static void ensureDefaults() {
        if (!POS.isEmpty()) return;
        Module.Category[] cats = Module.Category.values();
        int gap = 6;
        int startX = 8;
        int startY = 22;
        for (int i = 0; i < cats.length; i++) {
            float x = startX + (i % 4) * (PANEL_W + gap);
            float y = startY + (i / 4) * 130;
            POS.put(cats[i], new float[]{x, y});
        }
    }

    public static void set(Module.Category cat, float x, float y) {
        POS.put(cat, new float[]{x, y});
    }

    public static float[] get(Module.Category cat) {
        ensureDefaults();
        return POS.computeIfAbsent(cat, c -> new float[]{12, 24});
    }

    public static void writeConfig(StringBuilder sb) {
        ensureDefaults();
        for (var e : POS.entrySet()) {
            float[] xy = e.getValue();
            if (xy == null || xy.length < 2) continue;
            sb.append("panel.").append(e.getKey().name()).append('=')
                    .append(xy[0]).append(',').append(xy[1]).append('\n');
        }
    }

    public static void loadLine(String k, String v) {
        try {
            if (!k.startsWith("panel.")) return;
            String catName = k.substring(6);
            Module.Category cat = Module.Category.valueOf(catName);
            String[] parts = v.split(",");
            if (parts.length < 2) return;
            set(cat, Float.parseFloat(parts[0].trim()), Float.parseFloat(parts[1].trim()));
            loaded = true;
        } catch (Exception ignored) {}
    }
}
