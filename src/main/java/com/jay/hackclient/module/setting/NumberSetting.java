package com.jay.hackclient.module.setting;

public class NumberSetting extends Setting {
    private final double min, max, step, def;
    private double value;

    public NumberSetting(String name, String description, double def, double min, double max, double step) {
        super(name, description);
        this.min = min;
        this.max = max;
        this.step = step <= 0 ? 0.1 : step;
        this.def = clamp(def);
        this.value = this.def;
    }

    public double get() { return value; }
    public float getFloat() { return (float) value; }
    public int getInt() { return (int) Math.round(value); }

    public void set(double v) {
        double clamped = clamp(v);
        // Snap to step grid for stable slider / config values
        double snapped = min + Math.round((clamped - min) / step) * step;
        this.value = clamp(snapped);
    }

    public void increment() { set(value + step); }
    public void decrement() { set(value - step); }

    public double getMin() { return min; }
    public double getMax() { return max; }

    @Override
    public void reset() { this.value = def; }

    private double clamp(double v) {
        return Math.max(min, Math.min(max, v));
    }

    @Override
    public String getDisplayValue() {
        if (step >= 1.0) return String.valueOf(getInt());
        int decimals = step >= 0.1 ? 1 : 2;
        return String.format(java.util.Locale.ROOT, "%"." + decimals + "f", value);
    }
}
