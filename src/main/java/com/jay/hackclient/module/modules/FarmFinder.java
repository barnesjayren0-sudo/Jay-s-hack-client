package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import net.minecraft.text.Text;

/**
 * Focused farm ESP — uses BaseFinder scan data filtered to farm labels.
 */
public class FarmFinder extends Module {

    private long lastKick;

    public FarmFinder() {
        super("FarmFinder", "Kelp/cane/crop farm ESP", Category.WORLD);
    }

    @Override
    public void onEnable() {
        BaseFinder.findFarms = true;
        kickScan(true);
    }

    @Override
    public void onTick() {
        if (mc.player == null) return;
        long now = System.currentTimeMillis();
        if (now - lastKick < 8000) return;
        lastKick = now;
        kickScan(false);
    }

    private void kickScan(boolean chat) {
        if (com.jay.hackclient.JayHackClient.moduleManager == null) return;
        Module m = com.jay.hackclient.JayHackClient.moduleManager.getModuleByName("BaseFinder");
        if (m instanceof BaseFinder bf) {
            boolean old = BaseFinder.findFarms;
            BaseFinder.findFarms = true;
            bf.scan(chat);
            BaseFinder.findFarms = old || isEnabled();
            if (chat && mc.player != null) {
                int farms = 0;
                for (BaseFinder.Hit h : BaseFinder.lastHits) {
                    String l = h.label.toLowerCase();
                    if (l.contains("farm") || l.contains("kelp") || l.contains("crop")
                            || l.contains("cane") || l.contains("wart")) farms++;
                }
                mc.player.sendMessage(Text.literal(
                        "§8[§aFarm§8] §f" + farms + " farm markers (ESP)"), false);
            }
        }
    }
}
