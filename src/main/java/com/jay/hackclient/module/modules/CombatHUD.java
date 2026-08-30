package com.jay.hackclient.module.modules;

import com.jay.hackclient.JayHackClient;
import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.BoolSetting;
import com.jay.hackclient.util.TargetUtil;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.player.PlayerEntity;

/** Merged target + last-hit reach panel. */
public class CombatHUD extends Module {

    public final BoolSetting showReach = new BoolSetting("ShowReach", "Last hit distance", true);
    public final BoolSetting showHp = new BoolSetting("ShowHP", "Target health", true);

    public CombatHUD() {
        super("CombatHUD", "Target + reach panel", Category.RENDER);
        addSetting(showReach);
        addSetting(showHp);
    }

    public static void draw(DrawContext ctx) {
        if (JayHackClient.moduleManager == null) return;
        Module m = JayHackClient.moduleManager.getModuleByName("CombatHUD");
        if (!(m instanceof CombatHUD hud) || !m.isEnabled()) return;
        if (hud.mc.player == null) return;

        PlayerEntity t = TargetUtil.findCombatTarget(6.0, 180f);
        int sw = hud.mc.getWindow().getScaledWidth();
        int x = sw / 2 - 60;
        int y = hud.mc.getWindow().getScaledHeight() / 2 + 28;

        if (t != null) {
            String name = t.getName().getString();
            float hp = t.getHealth() + t.getAbsorptionAmount();
            String line = name;
            if (hud.showHp.get()) line += String.format(" §c%.0f❤", hp);
            int tw = hud.mc.textRenderer.getWidth(line.replaceAll("§.", ""));
            ctx.fill(x, y, x + Math.max(120, tw + 12), y + 22, 0xAA0B0D12);
            ctx.drawTextWithShadow(hud.mc.textRenderer, line, x + 6, y + 3, 0xFF3DDCFF);
            if (hud.showReach.get()) {
                String r = String.format("§7reach §f%.2f", ReachHUD.lastHitDist());
                ctx.drawTextWithShadow(hud.mc.textRenderer, r, x + 6, y + 12, 0xFFAAAAAA);
            }
        } else if (hud.showReach.get() && ReachHUD.lastHitDist() > 0) {
            String r = String.format("§7last §f%.2f", ReachHUD.lastHitDist());
            ctx.fill(x, y, x + 80, y + 14, 0x880B0D12);
            ctx.drawTextWithShadow(hud.mc.textRenderer, r, x + 6, y + 3, 0xFFAAAAAA);
        }
    }
}
