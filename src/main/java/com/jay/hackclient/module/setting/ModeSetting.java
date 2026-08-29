package com.jay.hackclient.module.setting;

import java.util.Arrays;
import java.util.List;

public class ModeSetting extends Setting {
    private final List<String> modes;
    private int index;

    public ModeSetting(String name, String description, String def, String... modes) {
        super(name, description);
        this.modes = Arrays.asList(modes);
        this.index = Math.max(0, this.modes.indexOf(def));
        if (this.index < 0) this.index = 0;
    }

    public String get() { return modes.get(index); }
    public int getIndex() { return index; }

    public void cycle() {
        index = (index + 1) % modes.size();
    }

    public void set(String mode) {
        int i = modes.indexOf(mode);
        if (i >= 0) index = i;
    }

    public List<String> getModes() { return modes; }

    @Override
    public String getDisplayValue() { return get(); }
}
