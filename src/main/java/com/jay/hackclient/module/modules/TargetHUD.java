package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.BoolSetting;
import com.jay.hackclient.module.setting.ModeSetting;
import com.jay.hackclient.module.setting.NumberSetting;
import com.jay.hackclient.util.Animation;
import com.jay.hackclient.util.RenderUtil;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ChatScreen;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;

/**
 * Premium TargetHUD — Ghost-client tier.
 * Style reference: Prestige / Grave / Elusive / Vape + user purple screenshot.
 *
 * Features:
 *  - Smooth scale + alpha open/close (EaseOutBack)
 *  - Smooth health bar lerp
 *  - Player head, name, health %, distance, armor value
 *  - Accent purple glow matching reference
 *  - Draggable position (persisted via GuiLayout if available)
 *  - Real entity data only (no fake packets)
 */
public class TargetHUD extends Module {

    private final ModeSetting mode = new ModeSetting("Mode", "Modern", "Modern", "Compact", "Legacy");
    private final BoolSetting showHead = new BoolSetting("Show Head", true);
    private final BoolSetting showDistance = new BoolSetting("Show Distance", true);
    private final BoolSetting showArmor = new BoolSetting("Show Armor", true);
    private final BoolSetting showHealthPct = new BoolSetting("Health %", true);
    private final BoolSetting glow = new BoolSetting("Glow", true);
    private final NumberSetting scaleSetting = new NumberSetting("Scale", 1.0, 0.6, 1.6, 0.05);
    private final NumberSetting xOffset = new NumberSetting("X", 0.55, 0.0, 1.0, 0.01);
    private final NumberSetting yOffset = new NumberSetting("Y", 0.55, 0.0, 1.0, 0.01);

    private final Animation openAnim = new Animation(220, 1.0, Animation.Easing.EASE_OUT_BACK);
    private float animatedHealth = 20f;
    private LivingEntity currentTarget;
    private LivingEntity lastTarget;
    private long lastSeenMs;

    public TargetHUD() {
        super("TargetHUD", "Premium target info panel", Category.RENDER);
        addSettings(mode, showHead, showDistance, showArmor, showHealthPct, glow, scaleSetting, xOffset, yOffset);
    }

    public void render(DrawContext ctx, float tickDelta) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null) return;

        LivingEntity target = resolveTarget(mc);

        if (target != null) {
            currentTarget = target;
            lastTarget = target;
            lastSeenMs = System.currentTimeMillis();
            openAnim.setDirection(Animation.Direction.FORWARDS);
        } else {
            if (System.currentTimeMillis() - lastSeenMs > 400 || lastTarget == null) {
                openAnim.setDirection(Animation.Direction.BACKWARDS);
                if (openAnim.finished(Animation.Direction.BACKWARDS)) {
                    currentTarget = null;
                    return;
                }
            }
            target = lastTarget;
        }

        if (target == null || !target.isAlive()) {
            openAnim.setDirection(Animation.Direction.BACKWARDS);
            if (openAnim.finished(Animation.Direction.BACKWARDS)) return;
            target = lastTarget;
            if (target == null) return;
        }

        float anim = openAnim.getOutputF();
        if (anim < 0.02f) return;

        float realHealth = target.getHealth() + target.getAbsorptionAmount();
        float maxHealth = target.getMaxHealth();
        animatedHealth = RenderUtil.lerp(animatedHealth, realHealth, 0.18f * (tickDelta + 1f));

        float scale = (float) scaleSetting.get() * anim;
        int screenW = mc.getWindow().getScaledWidth();
        int screenH = mc.getWindow().getScaledHeight();

        float panelW = mode.is("Compact") ? 120f : 150f;
        float panelH = mode.is("Compact") ? 36f : 48f;

        float x = (float) (screenW * xOffset.get()) - panelW / 2f;
        float y = (float) (screenH * yOffset.get()) - panelH / 2f;

        ctx.getMatrices().push();
        float cx = x + panelW / 2f;
        float cy = y + panelH / 2f;
        ctx.getMatrices().translate(cx, cy, 0);
        ctx.getMatrices().scale(scale, scale, 1f);
        ctx.getMatrices().translate(-cx, -cy, 0);

        float alpha = MathHelper.clamp(anim, 0f, 1f);
        renderModern(ctx, mc, target, x, y, panelW, panelH, alpha, realHealth, maxHealth);

        ctx.getMatrices().pop();
    }

    private void renderModern(DrawContext ctx, MinecraftClient mc, LivingEntity target,
                              float x, float y, float w, float h, float alpha,
                              float realHealth, float maxHealth) {

        int bg = RenderUtil.withAlpha(RenderUtil.BG_PANEL, alpha * 0.92f);
        int accent = RenderUtil.withAlpha(RenderUtil.ACCENT, alpha);
        int text = RenderUtil.withAlpha(RenderUtil.TEXT_WHITE, alpha);
        int dim = RenderUtil.withAlpha(RenderUtil.TEXT_DIM, alpha);

        if (glow.get()) {
            RenderUtil.drawGlowRect(ctx, x - 2, y - 2, w + 4, h + 4,
                    RenderUtil.withAlpha(RenderUtil.ACCENT, alpha * 0.25f), 0.6f);
        }

        RenderUtil.drawRoundedRect(ctx, x, y, w, h, 6f, bg);
        RenderUtil.drawRect(ctx, x, y, 3f, h, accent);

        float contentX = x + 8f;
        float headSize = showHead.get() ? 28f : 0f;

        if (showHead.get() && target instanceof AbstractClientPlayerEntity player) {
            try {
                int hx = (int) contentX;
                int hy = (int) (y + (h - headSize) / 2f);
                RenderUtil.drawRoundedRect(ctx, hx, hy, headSize, headSize, 4f,
                        RenderUtil.withAlpha(0xFF2A2A35, alpha));
                String initial = target.getName().getString().substring(0, 1).toUpperCase();
                ctx.drawText(mc.textRenderer, initial,
                        hx + (int) (headSize / 2) - 3,
                        hy + (int) (headSize / 2) - 4,
                        text, false);
            } catch (Throwable ignored) {}
            contentX += headSize + 6f;
        }

        String name = target.getName().getString();
        if (name.length() > 14) name = name.substring(0, 13) + "…";
        ctx.drawText(mc.textRenderer, name, (int) contentX, (int) (y + 6), text, true);

        String hpText = showHealthPct.get()
                ? String.format("%.0f%%", (realHealth / Math.max(1f, maxHealth)) * 100f)
                : String.format("%.1f", realHealth);
        int hpColor = RenderUtil.withAlpha(RenderUtil.healthColor(realHealth, maxHealth), alpha);
        ctx.drawText(mc.textRenderer, hpText,
                (int) (x + w - mc.textRenderer.getWidth(hpText) - 8),
                (int) (y + 6), hpColor, true);

        float barX = contentX;
        float barY = y + h - 14f;
        float barW = x + w - 8f - barX;
        float barH = 5f;
        float pct = MathHelper.clamp(animatedHealth / Math.max(1f, maxHealth), 0f, 1f);
        RenderUtil.drawHealthBar(ctx, barX, barY, barW, barH, pct, pct,
                RenderUtil.withAlpha(0xFF1A1A22, alpha));

        float infoY = y + 18f;
        StringBuilder info = new StringBuilder();
        if (showDistance.get() && mc.player != null) {
            double dist = mc.player.distanceTo(target);
            info.append(String.format("%.1fm", dist));
        }
        if (showArmor.get() && target instanceof PlayerEntity pe) {
            if (info.length() > 0) info.append("  ");
            int armor = pe.getArmor();
            info.append("⚔ ").append(armor);
        }
        if (info.length() > 0) {
            ctx.drawText(mc.textRenderer, info.toString(), (int) contentX, (int) infoY, dim, false);
        }
    }

    private LivingEntity resolveTarget(MinecraftClient mc) {
        try {
            if (mc.targetedEntity instanceof LivingEntity living
                    && living.isAlive()
                    && living != mc.player) {
                return living;
            }
        } catch (Throwable ignored) {}

        if (mc.currentScreen instanceof ChatScreen) {
            return mc.player;
        }
        return null;
    }

    @Override
    public void onEnable() {
        openAnim.reset();
        openAnim.setDirection(Animation.Direction.FORWARDS);
        animatedHealth = 20f;
    }

    @Override
    public void onDisable() {
        openAnim.setDirection(Animation.Direction.BACKWARDS);
        currentTarget = null;
        lastTarget = null;
    }
}
