package com.jay.hackclient.module;

import net.minecraft.client.MinecraftClient;
import org.lwjgl.glfw.GLFW;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class ModuleManager {

    private final List<Module> modules = new ArrayList<>();
    private final Set<Integer> heldKeys = new HashSet<>();
    private boolean frozen = false;

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

    public void panic() {
        frozen = true;
        disableCombat();
        for (Module m : modules) {
            if (m.isEnabled() && m.getCategory() != Module.Category.RENDER) {
                // leave pure render; disable rest on panic
            }
            if (m.isEnabled()) m.setEnabled(false);
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

    /** Combat + inventory combat helpers off (death / world change). */
    public void disableCombat() {
        for (Module m : modules) {
            if (!m.isEnabled()) continue;
            Module.Category c = m.getCategory();
            if (c == Module.Category.COMBAT || c == Module.Category.PLAYER) {
                m.setEnabled(false);
            }
        }
    }

    public void pollKeybinds() {
        if (frozen) return;
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.getWindow() == null) return;
        if (mc.currentScreen != null) {
            heldKeys.clear();
            return;
        }

        long handle = mc.getWindow().getHandle();
        for (Module m : modules) {
            int key = m.getKeyBind();
            if (key < 0) continue;

            boolean down = GLFW.glfwGetKey(handle, key) == GLFW.GLFW_PRESS;
            if (down && !heldKeys.contains(key)) {
                heldKeys.add(key);
                m.toggle();
            } else if (!down) {
                heldKeys.remove(key);
            }
        }
    }

    public void onTick() {
        if (frozen) return;
        pollKeybinds();
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
