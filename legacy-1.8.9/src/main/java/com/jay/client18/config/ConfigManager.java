package com.jay.client18.config;

import com.jay.client18.JayClient18;
import com.jay.client18.module.Module;
import net.minecraft.client.Minecraft;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;

public class ConfigManager {

    private File file() {
        File dir = new File(Minecraft.getMinecraft().mcDataDir, "jayclient18");
        if (!dir.exists()) dir.mkdirs();
        return new File(dir, "config.txt");
    }

    public void save() {
        try {
            BufferedWriter w = new BufferedWriter(new FileWriter(file()));
            for (Module m : JayClient18.moduleManager.getModules()) {
                w.write("mod:" + m.getName() + "=" + m.isEnabled() + ":" + m.getKeyBind());
                w.newLine();
            }
            for (String f : JayClient18.moduleManager.getFriends()) {
                w.write("friend:" + f);
                w.newLine();
            }
            w.close();
        } catch (Exception e) {
            System.err.println("[Jay18] config save: " + e.getMessage());
        }
    }

    public void load() {
        File f = file();
        if (!f.exists()) return;
        try {
            BufferedReader r = new BufferedReader(new FileReader(f));
            String line;
            while ((line = r.readLine()) != null) {
                line = line.trim();
                if (line.startsWith("mod:")) {
                    String rest = line.substring(4);
                    int eq = rest.indexOf('=');
                    if (eq < 0) continue;
                    String name = rest.substring(0, eq);
                    String[] parts = rest.substring(eq + 1).split(":");
                    Module m = JayClient18.moduleManager.get(name);
                    if (m == null) continue;
                    if (parts.length >= 2) {
                        try { m.setKeyBind(Integer.parseInt(parts[1])); } catch (Exception ignored) {}
                    }
                    if (parts.length >= 1 && Boolean.parseBoolean(parts[0])) {
                        m.setEnabled(true);
                    }
                } else if (line.startsWith("friend:")) {
                    JayClient18.moduleManager.addFriend(line.substring(7));
                }
            }
            r.close();
        } catch (Exception e) {
            System.err.println("[Jay18] config load: " + e.getMessage());
        }
    }
}
