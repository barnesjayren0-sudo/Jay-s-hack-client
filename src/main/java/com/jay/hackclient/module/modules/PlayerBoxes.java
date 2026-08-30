package com.jay.hackclient.module.modules;

import com.jay.hackclient.JayHackClient;
import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.BoolSetting;
import com.jay.hackclient.module.setting.NumberSetting;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.render.Camera;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.MathHelper;

/** 2D player ESP boxes on HUD (works without world render API). */
public class PlayerBoxes extends Module {

    public final NumberSetting range = new NumberSetting("Range", "Max distance", 64, 16, 128, 4);
    public final BoolSetting fill = new BoolSetting("Fill", "Soft fill", true);
    public final BoolSetting friends = new BoolSetting("Friends", "Show friends", true);
    public final NumberSetting colorR = new NumberSetting("ColorR", "Red", 61, 0, 255, 1);
    public final NumberSetting colorG = new NumberSetting("ColorG", "Green", 220, 0, 255, 1);
    public final NumberSetting colorB = new NumberSetting("ColorB", "Blue", 255, 0, 255, 1);

    public PlayerBoxes() {
        super("PlayerBoxes", "2D ESP boxes on players", Category.RENDER);
        addSetting(range);
        addSetting(fill);
        addSetting(friends);
        addSetting(colorR);
        addSetting(colorG);
        addSetting(colorB);
    }

    public static void draw(DrawContext ctx) {
        if (JayHackClient.moduleManager == null) return;
        Module m = JayHackClient.moduleManager.getModuleByName("PlayerBoxes");
        if (!(m instanceof PlayerBoxes pb) || !m.isEnabled()) return;
        if (pb.mc.player == null || pb.mc.world == null || pb.mc.gameRenderer == null) return;

        Camera cam = pb.mc.gameRenderer.getCamera();
        int sw = pb.mc.getWindow().getScaledWidth();
        int sh = pb.mc.getWindow().getScaledHeight();
        float yaw = cam.getYaw();
        float pitch = cam.getPitch();
        float fov = 70f;
        try { fov = (float) pb.mc.options.getFov().getValue(); } catch (Throwable ignored) {}
        double scale = (sh / 2.0) / Math.tan(Math.toRadians(fov / 2.0));
        var camPos = cam.getCameraPos();

        int color = 0xFF000000
                | ((pb.colorR.getInt() & 255) << 16)
                | ((pb.colorG.getInt() & 255) << 8)
                | (pb.colorB.getInt() & 255);

        for (PlayerEntity p : pb.mc.world.getPlayers()) {
            if (p == pb.mc.player || !p.isAlive()) continue;
            try { if (AntiBot.isBot(p)) continue; } catch (Throwable ignored) {}
            boolean isFriend = JayHackClient.friendManager != null
                    && JayHackClient.friendManager.isFriend(p.getName().getString());
            if (isFriend && !pb.friends.get()) continue;
            double dist = pb.mc.player.distanceTo(p);
            if (dist > pb.range.get()) continue;

            // feet + head
            double[] feet = project(p.getX(), p.getY(), p.getZ(), camPos.x, camPos.y, camPos.z, yaw, pitch, scale, sw, sh);
            double[] head = project(p.getX(), p.getY() + p.getHeight(), p.getZ(), camPos.x, camPos.y, camPos.z, yaw, pitch, scale, sw, sh);
            if (feet == null || head == null) continue;

            int x1 = (int) Math.min(feet[0], head[0]) - 8;
            int x2 = (int) Math.max(feet[0], head[0]) + 8;
            int y1 = (int) Math.min(feet[1], head[1]);
            int y2 = (int) Math.max(feet[1], head[1]);
            int h = Math.max(8, y2 - y1);
            int w = Math.max(6, Math.min(28, h / 2));
            int cx = (x1 + x2) / 2;
            x1 = cx - w / 2;
            x2 = cx + w / 2;
            y2 = y1 + h;

            if (x2 < 2 || x1 > sw - 2 || y2 < 2 || y1 > sh - 2) continue;

            int boxColor = isFriend ? 0xFF55FF55 : color;
            if (pb.fill.get()) {
                ctx.fill(x1, y1, x2, y2, (boxColor & 0x00FFFFFF) | 0x33000000);
            }
            // corners
            int t = 1;
            int len = Math.max(3, w / 4);
            ctx.fill(x1, y1, x1 + len, y1 + t, boxColor);
            ctx.fill(x1, y1, x1 + t, y1 + len, boxColor);
            ctx.fill(x2 - len, y1, x2, y1 + t, boxColor);
            ctx.fill(x2 - t, y1, x2, y1 + len, boxColor);
            ctx.fill(x1, y2 - t, x1 + len, y2, boxColor);
            ctx.fill(x1, y2 - len, x1 + t, y2, boxColor);
            ctx.fill(x2 - len, y2 - t, x2, y2, boxColor);
            ctx.fill(x2 - t, y2 - len, x2, y2, boxColor);
        }
    }

    private static double[] project(double wx, double wy, double wz,
                                    double cx, double cy, double cz,
                                    float yaw, float pitch, double scale, int sw, int sh) {
        double dx = wx - cx, dy = wy - cy, dz = wz - cz;
        double yawRad = Math.toRadians(yaw);
        double pitchRad = Math.toRadians(pitch);
        double cosY = Math.cos(-yawRad), sinY = Math.sin(-yawRad);
        double cosP = Math.cos(-pitchRad), sinP = Math.sin(-pitchRad);
        double x1 = dx * cosY - dz * sinY;
        double z1 = dx * sinY + dz * cosY;
        double y1 = dy * cosP - z1 * sinP;
        double z2 = dy * sinP + z1 * cosP;
        if (z2 <= 0.15) return null;
        double sx = sw / 2.0 + (x1 / z2) * scale;
        double sy = sh / 2.0 - (y1 / z2) * scale;
        return new double[]{sx, sy};
    }
}
