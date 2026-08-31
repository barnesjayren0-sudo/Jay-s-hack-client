package com.jay.hackclient.render;

import com.jay.hackclient.JayHackClient;
import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.modules.AntiBot;
import com.jay.hackclient.module.modules.ESP;
import com.jay.hackclient.module.modules.Nametags;
import com.jay.hackclient.module.modules.StorageESP;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

/**
 * HUD-space ESP: 2D boxes + nametags using Camera-based projection.
 */
public final class WorldEspRenderer {

    private static boolean registered;

    private WorldEspRenderer() {}

    public static void register() {
        if (registered) return;
        registered = true;
    }

    public static void drawHudOverlay(DrawContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null || JayHackClient.moduleManager == null) return;
        if (mc.options.hudHidden) return;

        Module espMod = JayHackClient.moduleManager.getModuleByName("ESP");
        Module ntMod = JayHackClient.moduleManager.getModuleByName("Nametags");
        Module storageMod = JayHackClient.moduleManager.getModuleByName("StorageESP");

        ESP esp = espMod instanceof ESP e && e.isEnabled() ? e : null;
        Nametags tags = ntMod instanceof Nametags n && n.isEnabled() ? n : null;

        int sw = mc.getWindow().getScaledWidth();
        int sh = mc.getWindow().getScaledHeight();

        // Player / entity boxes + nametags
        if (esp != null || tags != null) {
            double maxEsp = esp != null ? esp.range.get() : 64;
            double maxNt = tags != null ? tags.range.get() : 64;
            double max = Math.max(maxEsp, maxNt);

            for (Entity ent : mc.world.getEntities()) {
                if (ent == mc.player || !ent.isAlive()) continue;
                double dist = mc.player.distanceTo(ent);
                if (dist > max) continue;

                boolean isPlayer = ent instanceof PlayerEntity;
                if (isPlayer) {
                    PlayerEntity p = (PlayerEntity) ent;
                    try {
                        if (AntiBot.isBot(p)) continue;
                    } catch (Throwable ignored) {}
                    boolean friend = JayHackClient.friendManager != null
                            && JayHackClient.friendManager.isFriend(p.getName().getString());

                    if (esp != null && esp.players.get() && dist <= maxEsp) {
                        if (!(friend && !esp.friends.get())) {
                            drawEntityBox(ctx, ent, friend ? 0xFF55FF55 : esp.colorArgb(), sw, sh);
                        }
                    }
                    if (tags != null && dist <= maxNt) {
                        drawNametag(ctx, p, mc.player, tags, sw, sh);
                    }
                } else if (esp != null && dist <= maxEsp) {
                    if (ent instanceof HostileEntity && esp.hostiles.get()) {
                        drawEntityBox(ctx, ent, 0xFFFF5555, sw, sh);
                    } else if (ent instanceof PassiveEntity && esp.passives.get()) {
                        drawEntityBox(ctx, ent, 0xFF55FF55, sw, sh);
                    }
                }
            }
        }

        // StorageESP markers (simple)
        if (storageMod instanceof StorageESP se && se.isEnabled()) {
            try {
                // leave existing storage drawing if any via PlayerBoxes / StorageESP internals
            } catch (Throwable ignored) {}
        }

        try { com.jay.hackclient.module.modules.PearlTrajectory.draw(ctx); } catch (Throwable ignored) {}
        try { com.jay.hackclient.module.modules.CombatHUD.draw(ctx); } catch (Throwable ignored) {}
    }

    private static void drawEntityBox(DrawContext ctx, Entity ent, int color, int sw, int sh) {
        Box box = ent.getBoundingBox();
        // 8 corners → screen min/max
        double[] xs = {box.minX, box.maxX};
        double[] ys = {box.minY, box.maxY};
        double[] zs = {box.minZ, box.maxZ};

        int minX = Integer.MAX_VALUE, minY = Integer.MAX_VALUE;
        int maxX = Integer.MIN_VALUE, maxY = Integer.MIN_VALUE;
        int hits = 0;

        for (double x : xs) {
            for (double y : ys) {
                for (double z : zs) {
                    int[] s = RenderUtil.worldToScreen(x, y, z);
                    if (s == null) continue;
                    hits++;
                    minX = Math.min(minX, s[0]);
                    minY = Math.min(minY, s[1]);
                    maxX = Math.max(maxX, s[0]);
                    maxY = Math.max(maxY, s[1]);
                }
            }
        }
        if (hits < 2) return;

        // Clamp box size
        minX = Math.max(0, minX);
        minY = Math.max(0, minY);
        maxX = Math.min(sw - 1, maxX);
        maxY = Math.min(sh - 1, maxY);
        if (maxX - minX < 2 || maxY - minY < 2) return;
        if (maxX - minX > sw * 0.9 || maxY - minY > sh * 0.9) return;

        int a = (color >> 24) & 0xFF;
        if (a == 0) a = 0xFF;
        int line = (a << 24) | (color & 0x00FFFFFF);
        int fill = ((a / 4) << 24) | (color & 0x00FFFFFF);

        // Filled translucent
        ctx.fill(minX, minY, maxX, maxY, fill);
        // Border 1px
        ctx.fill(minX, minY, maxX, minY + 1, line);
        ctx.fill(minX, maxY - 1, maxX, maxY, line);
        ctx.fill(minX, minY, minX + 1, maxY, line);
        ctx.fill(maxX - 1, minY, maxX, maxY, line);
    }

    private static void drawNametag(DrawContext ctx, PlayerEntity p, PlayerEntity self,
                                    Nametags tags, int sw, int sh) {
        // Anchor above head
        Vec3d top = new Vec3d(p.getX(), p.getY() + p.getHeight() + 0.35, p.getZ());
        int[] s = RenderUtil.worldToScreen(top);
        if (s == null) return;

        int sx = s[0];
        int sy = s[1];
        if (sx < 2 || sx > sw - 2 || sy < 2 || sy > sh - 2) return;

        String label = Nametags.formatTag(p, self);
        // Width without § codes for centering
        String plain = label.replaceAll("§.", "");
        int tw = self.getEntityWorld() != null
                ? MinecraftClient.getInstance().textRenderer.getWidth(plain)
                : plain.length() * 6;
        var tr = MinecraftClient.getInstance().textRenderer;
        tw = tr.getWidth(plain);

        int pad = 3;
        int boxW = tw + pad * 2;
        int boxH = 11;
        int bx = sx - boxW / 2;
        int by = sy - boxH;

        // Background
        ctx.fill(bx, by, bx + boxW, by + boxH, 0x99000000);
        // Accent bar left
        int accent = tags.colorArgb();
        ctx.fill(bx, by, bx + 1, by + boxH, accent);

        ctx.drawTextWithShadow(tr, label, bx + pad, by + 2, 0xFFFFFFFF);

        // HP bar under name
        if (tags.health.get()) {
            float hp = p.getHealth() + p.getAbsorptionAmount();
            float max = Math.max(1f, p.getMaxHealth());
            float pct = Math.max(0f, Math.min(1f, hp / max));
            int barY = by + boxH;
            int barH = 2;
            ctx.fill(bx, barY, bx + boxW, barY + barH, 0xFF222222);
            int fillW = Math.max(1, (int) (boxW * pct));
            int hpCol = pct > 0.5f ? 0xFF55FF55 : (pct > 0.25f ? 0xFFFFFF55 : 0xFFFF5555);
            ctx.fill(bx, barY, bx + fillW, barY + barH, hpCol);
        }
    }
}
