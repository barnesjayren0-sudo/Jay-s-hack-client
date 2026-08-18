package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import net.minecraft.entity.projectile.thrown.EnderPearlEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.math.Vec3d;

/** Faces nearest flying pearl (yours or enemy) — useful in nethpot/UHC. */
public class PearlCatch extends Module {

    public PearlCatch() {
        super("PearlCatch", "Looks at nearby ender pearls", Category.PLAYER);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) return;

        EnderPearlEntity best = null;
        double bestDist = 48;

        for (var e : mc.world.getEntities()) {
            if (!(e instanceof EnderPearlEntity pearl)) continue;
            double d = mc.player.distanceTo(pearl);
            if (d < bestDist) {
                bestDist = d;
                best = pearl;
            }
        }

        if (best == null) return;

        Vec3d eyes = mc.player.getEyePos();
        Vec3d pos = best.getEntityPos();
        double dx = pos.x - eyes.x;
        double dy = pos.y - eyes.y;
        double dz = pos.z - eyes.z;
        double horiz = Math.sqrt(dx * dx + dz * dz);
        float yaw = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90);
        float pitch = (float) -Math.toDegrees(Math.atan2(dy, horiz));
        mc.player.setYaw(yaw);
        mc.player.setPitch(pitch);
    }
}
