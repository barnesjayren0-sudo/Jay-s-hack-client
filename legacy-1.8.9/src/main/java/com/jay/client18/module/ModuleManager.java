package com.jay.client18.module;

import org.lwjgl.input.Keyboard;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

public class ModuleManager {

    private final List<Module> modules = new ArrayList<Module>();
    private final Set<String> friends = new HashSet<String>();
    private final Set<Integer> held = new HashSet<Integer>();
    private boolean frozen;

    public void register(Module m) {
        if (m == null) return;
        for (Module x : modules) {
            if (x.getName().equalsIgnoreCase(m.getName())) return;
        }
        modules.add(m);
    }

    public List<Module> getModules() {
        return Collections.unmodifiableList(modules);
    }

    public List<Module> getByCategory(Module.Category c) {
        List<Module> list = new ArrayList<Module>();
        for (Module m : modules) {
            if (m.getCategory() == c) list.add(m);
        }
        return list;
    }

    public Module get(String name) {
        for (Module m : modules) {
            if (m.getName().equalsIgnoreCase(name)) return m;
        }
        return null;
    }

    public void panic() {
        frozen = true;
        for (Module m : modules) {
            if (m.isEnabled()) m.setEnabled(false);
        }
        frozen = false;
    }

    public void onTick() {
        pollKeys();
        for (Module m : modules) {
            if (!m.isEnabled()) continue;
            try {
                m.onTick();
            } catch (Throwable t) {
                System.err.println("[Jay18] " + m.getName() + ": " + t.getMessage());
                m.setEnabled(false);
            }
        }
    }

    private void pollKeys() {
        for (Module m : modules) {
            int key = m.getKeyBind();
            if (key == Keyboard.KEY_NONE) continue;
            boolean down = Keyboard.isKeyDown(key);
            if (down && !held.contains(Integer.valueOf(key))) {
                held.add(Integer.valueOf(key));
                m.toggle();
            } else if (!down) {
                held.remove(Integer.valueOf(key));
            }
        }
    }

    public void addFriend(String name) {
        if (name != null) friends.add(name.toLowerCase(Locale.ROOT));
    }

    public void removeFriend(String name) {
        if (name != null) friends.remove(name.toLowerCase(Locale.ROOT));
    }

    public boolean isFriend(String name) {
        return name != null && friends.contains(name.toLowerCase(Locale.ROOT));
    }

    public List<String> getFriends() {
        return new ArrayList<String>(friends);
    }

    public Set<String> getFriendSet() {
        return friends;
    }
}
