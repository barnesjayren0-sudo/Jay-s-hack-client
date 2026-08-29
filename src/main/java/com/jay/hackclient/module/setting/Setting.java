package com.jay.hackclient.module.setting;

/** Base setting attached to a Module (Cyemer-style settings system). */
public abstract class Setting {
    private final String name;
    private final String description;

    protected Setting(String name, String description) {
        this.name = name;
        this.description = description == null ? "" : description;
    }

    public String getName() { return name; }
    public String getDescription() { return description; }
    public abstract String getDisplayValue();
}
