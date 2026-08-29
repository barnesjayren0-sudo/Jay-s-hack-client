package com.jay.hackclient.module.setting;

public class BoolSetting extends Setting {
    private boolean value;

    public BoolSetting(String name, String description, boolean def) {
        super(name, description);
        this.value = def;
    }

    public boolean get() { return value; }
    public void set(boolean v) { this.value = v; }
    public void toggle() { value = !value; }

    @Override
    public String getDisplayValue() { return value ? "ON" : "OFF"; }
}
