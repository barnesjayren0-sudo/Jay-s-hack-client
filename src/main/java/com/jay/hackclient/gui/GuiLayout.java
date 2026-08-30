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
