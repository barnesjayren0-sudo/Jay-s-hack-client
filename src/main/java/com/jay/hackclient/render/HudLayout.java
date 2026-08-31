package com.jay.hackclient.render;

import java.util.LinkedHashMap;
import java.util.Map;

/** Positions for HUD editor elements (persisted in config). */
public final class HudLayout {

    public static final class Element {
        public final String id;
        public final String label;
        public float x, y;
        public boolean visible = true;

        public Element(String id, String label, float x, float y) {
            this.id = id;
            this.label = label;
            this.x = x;
            this.y = y;
        }
    }

    public static final Map<String, Element> ELEMENTS = new LinkedHashMap<>();

    static {
        put("fps", "FPS", 4, 4);
        put("ping", "Ping", 4, 16);
        put("coords", "Coords", 4, 28);
        put("speed", "Speed", 4, 40);
        put("cps", "CPS", 4, 52);
        put("keystrokes", "Keys", 4, 70);
        put("armor", "Armor", 4, 120);
        put("potions", "Potions", 4, 160);
        put("target", "Target", 120, 4);
        put("arraylist", "ArrayList", -120, 4); // negative = from right
        put("perf", "Perf", 4, 200);
    }

    private static void put(String id, String label, float x, float y) {
        ELEMENTS.put(id, new Element(id, label, x, y));
    }

    public static Element get(String id) {
        return ELEMENTS.get(id);
    }

    public static void writeConfig(StringBuilder sb) {
        for (Element e : ELEMENTS.values()) {
            sb.append("hud.").append(e.id).append('=')
                    .append(e.x).append(',').append(e.y).append(',').append(e.visible).append('\n');
        }
    }

    public static void loadLine(String k, String v) {
        if (!k.startsWith("hud.")) return;
        Element e = ELEMENTS.get(k.substring(4));
        if (e == null) return;
        try {
            String[] p = v.split(",");
            if (p.length >= 2) {
                e.x = Float.parseFloat(p[0].trim());
                e.y = Float.parseFloat(p[1].trim());
            }
            if (p.length >= 3) e.visible = Boolean.parseBoolean(p[2].trim());
        } catch (Exception ignored) {}
    }
}
