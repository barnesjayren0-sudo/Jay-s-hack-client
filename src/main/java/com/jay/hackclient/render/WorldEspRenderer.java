package com.jay.hackclient.render;

import com.jay.hackclient.JayHackClient;
import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.modules.AntiBot;
import com.jay.hackclient.module.modules.ESP;
import com.jay.hackclient.module.modules.Nametags;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.entity.Entity;
import net.minecraft.entity.mob.HostileEntity;
import net.minecraft.entity.passive.PassiveEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.Box;
import net.minecraft.util.math.Vec3d;

/** HUD-space ESP: 2D boxes, tracers, distance-scaled nametags. */
public final class WorldEspRenderer {

    private static boolean registered;

    private WorldEspRenderer() {}

    public static void register() {
        registered = true;
    }

    public static void drawHudOverlay(DrawContext ctx) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc.player == null || mc.world == null || JayHackClient.moduleManager == null) return;
        if (mc.options.hudHidden) return;

        Module espMod = JayHackClient.moduleManager.getModuleByName("ESP");
        Module ntMod = JayHackClient.moduleManager.getModuleByName("Nametags");

        ESP esp = espMod instanceof ESP e && e.isEnabled() ? e : null;
        Nametags tags = ntMod instanceof Nametags n && n.isEnabled() ? n : null;

        int sw = mc.getWindow().getScaledWidth();
        int sh = mc.getWindow().getScaledHeight();
        int cx = sw / 2;
        int cy = sh / 2;

        if (esp == null && tags == null) {
            try { com.jay.hackclient.module.modules.PearlTrajectory.draw(ctx); } catch (Throwable ignored) {}
            return;
        }

        double maxEsp = esp != null ? esp.range.get() : 64;
        double maxNt = tags != null ? tags.range.get() : 64;
        double max = Math.max(maxEsp, maxNt);

        for (Entity ent : mc.world.getEntities()) {
            if (ent == mc.player || !ent.isAlive()) continue;
            double dist = mc.player.distanceTo(ent);
            if (dist > max) continue;

            if (ent instanceof PlayerEntity p) {
                try {
                    if (AntiBot.isBot(p)) continue;
                } catch (Throwable ignored) {}
                boolean friend = JayHackClient.friendManager != null
                        && JayHackClient.friendManager.isFriend(p.getName().getString());

                if (esp != null && esp.players.get() && dist <= maxEsp) {
                    if (!(friend && !esp.friends.get())) {
                        int col = friend ? 0xFF55FF55 : esp.colorArgb();
                        drawEntityBox(ctx, ent, col, sw, sh);
                        drawTracer(ctx, ent, col, cx, cy, sh);
                    }
                }
                if (tags != null && dist <= maxNt) {
                    drawNametag(ctx, p, mc.player, tags, sw, sh, dist);
                }
            } else if (esp != null && dist <= maxEsp) {
                if (ent instanceof HostileEntity && esp.hostiles.get()) {
                    drawEntityBox(ctx, ent, 0xFFFF5555, sw, sh);
                    drawTracer(ctx, ent, 0xFFFF5555, cx, cy, sh);
                } else if (ent instanceof PassiveEntity && esp.passives.get()) {
                    drawEntityBox(ctx, ent, 0xFF55FF55, sw, sh);
                }
            }
        }

        try { com.jay.hackclient.module.modules.PearlTrajectory.draw(ctx); } catch (Throwable ignored) {}
        try { com.jay.hackclient.module.modules.CombatHUD.draw(ctx); } catch (Throwable ignored) {}
    }

    private static void drawTracer(DrawContext ctx, Entity ent, int color, int cx, int cy, int sh) {
        Vec3d feet = new Vec3d(ent.getX(), ent.getY() + ent.getHeight() * 0.5, ent.getZ());
        int[] s = RenderUtil.worldToScreen(feet);
        if (s == null) return;
        // Simple stepped line from bottom-center toward target
        int x0 = cx;
        int y0 = sh - 1;
        int x1 = s[0];
        int y1 = s[1];
        int steps = Math.max(Math.abs(x1 - x0), Math.abs(y1 - y0));
        steps = Math.min(steps, 120);
        if (steps < 1) return;
        int a = 0xAA000000 | (color & 0x00FFFFFF);
        for (int i = 0; i <= steps; i++) {
            float t = i / (float) steps;
            int x = (int) (x0 + (x1 - x0) * t);
            int y = (int) (y0 + (y1 - y0) * t);
            ctx.fill(x, y, x + 1, y + 1, a);
        }
    }

    private static void drawEntityBox(DrawContext ctx, Entity ent, int color, int sw, int sh) {
        Box box = ent.getBoundingBox();
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

        minX = Math.max(0, minX);
        minY = Math.max(0, minY);
        maxX = Math.min(sw - 1, maxX);
        maxY = Math.min(sh - 1, maxY);
        if (maxX - minX < 2 || maxY - minY < 2) return;
        if (maxX - minX > sw * 0.85 || maxY - minY > sh * 0.85) return;

        int line = 0xFF000000 | (color & 0x00FFFFFF);
        int fill = 0x28000000 | (color & 0x00FFFFFF);

        ctx.fill(minX, minY, maxX, maxY, fill);
        ctx.fill(minX, minY, maxX, minY + 1, line);
        ctx.fill(minX, maxY - 1, maxX, maxY, line);
        ctx.fill(minX, minY, minX + 1, maxY, line);
        ctx.fill(maxX - 1, minY, maxX, maxY, line);
    }

    private static void drawNametag(DrawContext ctx, PlayerEntity p, PlayerEntity self,
                                    Nametags tags, int sw, int sh, double dist) {
        Vec3d top = new Vec3d(p.getX(), p.getY() + p.getHeight() + 0.35, p.getZ());
        int[] s = RenderUtil.worldToScreen(top);
        if (s == null) return;

        int sx = s[0];
        int sy = s[1];
        if (sx < 2 || sx > sw - 2 || sy < 2 || sy > sh - 2) return;

        // Distance scale: far = smaller text box
        float scale = 1.0f;
        if (dist > 12) scale = (float) Math.max(0.65, 1.0 - (dist - 12) / 80.0);

        String label = Nametags.formatTag(p, self);
        var tr = MinecraftClient.getInstance().textRenderer;
        String plain = label.replaceAll("§.", "");
        int tw = tr.getWidth(plain);
        int pad = 3;
        int boxW = Math.max(20, (int) ((tw + pad * 2) * scale));
        int boxH = Math.max(9, (int) (11 * scale));
        int bx = sx - boxW / 2;
        int by = sy - boxH;

        ctx.fill(bx, by, bx + boxW, by + boxH, 0x99000000);
        ctx.fill(bx, by, bx + 1, by + boxH, tags.colorArgb());
        ctx.drawTextWithShadow(tr, label, bx + pad, by + 2, 0xFFFFFFFF);

        if (tags.health.get()) {
            float hp = p.getHealth() + p.getAbsorptionAmount();
            float maxHp = Math.max(1f, p.getMaxHealth());
            float pct = Math.max(0f, Math.min(1f, hp / maxHp));
            int barY = by + boxH;
            ctx.fill(bx, barY, bx + boxW, barY + 2, 0xFF222222);
            int fillW = Math.max(1, (int) (boxW * pct));
            int hpCol = pct > 0.5f ? 0xFF55FF55 : (pct > 0.25f ? 0xFFFFFF55 : 0xFFFF5555);
            ctx.fill(bx, barY, bx + fillW, barY + 2, hpCol);
        }
    }
}
