package com.jay.hackclient.module.setting;

public class NumberSetting extends Setting {
    private double value;
    private final double def;
    private final double min, max;
    private final double step;

    public NumberSetting(String name, String description, double def, double min, double max, double step) {
        super(name, description);
        this.min = min;
        this.max = max;
        this.step = step <= 0 ? 0.1 : step;
        this.def = clamp(def);
        this.value = this.def;
    }

    public double get() { return value; }
    public int getInt() { return (int) Math.round(value); }
    public float getFloat() { return (float) value; }

    public void set(double v) { this.value = clamp(v); }

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
        return String.format("%.2f", value);
    }
}
