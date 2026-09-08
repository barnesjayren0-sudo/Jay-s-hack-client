package com.jay.client18.render;

import com.jay.client18.JayClient18;
import com.jay.client18.module.Module;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

/** ArrayList HUD — hidden when F3 debug or hideGUI (screenshare-safer). */
public class HudRenderer {

    @SubscribeEvent
    public void onOverlay(RenderGameOverlayEvent.Text e) {
        if (JayClient18.moduleManager == null) return;
        Minecraft mc = Minecraft.getMinecraft();

        // Screenshare / F3 / F1 safety
        if (mc.gameSettings.showDebugInfo) return;
        if (mc.gameSettings.hideGUI) return;

        Module arr = JayClient18.moduleManager.get("ArrayList");
        if (arr == null || !arr.isEnabled()) return;

        FontRenderer fr = mc.fontRendererObj;
        int y = 4;
        // Subtle label — not a giant watermark
        fr.drawStringWithShadow("§8Jay§7 1.8", 4, y, 0xAAAAAA);
        y += 10;
        for (Module m : JayClient18.moduleManager.getModules()) {
            if (!m.isEnabled()) continue;
            if (m.getName().equals("ArrayList")) continue;
            String label = m.getName();
            if (!m.getKeyLabel().isEmpty()) {
                label += " §8[§7" + m.getKeyLabel() + "§8]";
            }
            fr.drawStringWithShadow("§7" + label, 4, y, 0x88FFFF);
            y += 10;
        }
    }
}
