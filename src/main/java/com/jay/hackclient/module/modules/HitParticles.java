package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.BoolSetting;
import com.jay.hackclient.module.setting.ModeSetting;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.util.math.Vec3d;

/** Simple hit particles when you attack (visual flair). */
public class HitParticles extends Module {

    public final ModeSetting style = new ModeSetting("Style", "Particle style", "Crit", "Crit", "Heart", "Flame");
    public final BoolSetting playersOnly = new BoolSetting("PlayersOnly", "Only on players", true);

    private int lastAttackTicker = -1;

    public HitParticles() {
        super("HitParticles", "Particles on attack", Category.RENDER);
        addSetting(style);
        addSetting(playersOnly);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null) return;
        // Detect swing + looking at entity roughly
        if (!mc.options.attackKey.isPressed()) return;
        Entity target = mc.targetedEntity;
        if (target == null) return;
        if (playersOnly.get() && !(target instanceof PlayerEntity)) return;
        if (mc.player.age == lastAttackTicker) return;
        if (mc.player.getAttackCooldownProgress(0) < 0.9f) return;
        lastAttackTicker = mc.player.age;

        Vec3d p = new Vec3d(target.getX(), target.getY() + target.getHeight() * 0.5, target.getZ());
        switch (style.get().toLowerCase()) {
            case "heart" -> {
                for (int i = 0; i < 6; i++) {
                    mc.world.addParticleClient(ParticleTypes.HEART,
                            p.x + (Math.random() - 0.5) * 0.6,
                            p.y + (Math.random() - 0.5) * 0.6,
                            p.z + (Math.random() - 0.5) * 0.6,
                            0, 0.05, 0);
                }
            }
            case "flame" -> {
                for (int i = 0; i < 10; i++) {
                    mc.world.addParticleClient(ParticleTypes.FLAME,
                            p.x + (Math.random() - 0.5) * 0.5,
                            p.y + (Math.random() - 0.5) * 0.5,
                            p.z + (Math.random() - 0.5) * 0.5,
                            0, 0.02, 0);
                }
            }
            default -> {
                for (int i = 0; i < 8; i++) {
                    mc.world.addParticleClient(ParticleTypes.CRIT,
                            p.x + (Math.random() - 0.5) * 0.5,
                            p.y + (Math.random() - 0.5) * 0.5,
                            p.z + (Math.random() - 0.5) * 0.5,
                            0, 0, 0);
                }
            }
        }
    }
}
