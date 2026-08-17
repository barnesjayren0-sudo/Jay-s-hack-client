package com.jay.hackclient.module;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ModuleManager {

    private final List<Module> modules = new ArrayList<>();
    private boolean frozen = false; // panic / master off

    public void register(Module module) {
        modules.add(module);
    }

    public List<Module> getModules() {
        return Collections.unmodifiableList(modules);
    }

    public List<Module> getByCategory(Module.Category category) {
        List<Module> list = new ArrayList<>();
        for (Module m : modules) {
            if (m.getCategory() == category) list.add(m);
        }
        return list;
    }

    public Module getModuleByName(String name) {
        for (Module m : modules) {
            if (m.getName().equalsIgnoreCase(name)) return m;
        }
        return null;
    }

    public boolean isFrozen() {
        return frozen;
    }

    /** Instantly disables every module and blocks ticks until unfrozen. */
    public void panic() {
        frozen = true;
        for (Module m : modules) {
            if (m.isEnabled()) {
                m.setEnabled(false);
            }
        }
    }

    public void unfreeze() {
        frozen = false;
    }

    public void disableAll() {
        for (Module m : modules) {
            if (m.isEnabled()) m.setEnabled(false);
        }
    }

    public void disableCategory(Module.Category category) {
        for (Module m : modules) {
            if (m.getCategory() == category && m.isEnabled()) {
                m.setEnabled(false);
            }
        }
    }

    public void onTick() {
        if (frozen) return;
        for (Module m : modules) {
            if (m.isEnabled()) {
                try {
                    m.onTick();
                } catch (Exception e) {
                    System.err.println("[JayHack] " + m.getName() + ": " + e.getMessage());
                }
            }
        }
    }
}
