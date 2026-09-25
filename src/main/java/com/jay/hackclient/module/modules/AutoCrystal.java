package com.jay.hackclient.module.modules;

import com.jay.hackclient.JayHackClient;
import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.BoolSetting;
import com.jay.hackclient.module.setting.NumberSetting;
import com.jay.hackclient.util.RealPackets;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;

/** AutoCrystal — place + break. Real attack/slot/look packets. */
public class AutoCrystal extends Module {

    public final NumberSetting range = new NumberSetting("Range", "Crystal range", 4.5, 2.0, 6.0, 0.1);
    public final NumberSetting placeDelay = new NumberSetting("Place Delay", "Ms between places", 50, 0, 250, 5);
    public final NumberSetting breakDelay = new NumberSetting("Break Delay", "Ms between breaks", 40, 0, 250, 5);
    public final BoolSetting autoSwitch = new BoolSetting("Auto Switch", "Switch to crystal", true);
    public final BoolSetting rotate = new BoolSetting("Rotate", "Look at crystal/place", true);
    public final BoolSetting silent = new BoolSetting("Silent", "Server look only", true);
    public final BoolSetting breakOnly = new BoolSetting("Break Only", "Only break", false);
    public final BoolSetting realPackets = new BoolSetting("Real Packets", "Vanilla C2S", true);

    private long lastPlace, lastBreak;
    private int restoreSlot = -1;

    public AutoCrystal() {
        super("AutoCrystal", "Place/break crystals on targets", Category.ANARCHY);
        addSetting(range); addSetting(placeDelay); addSetting(breakDelay);
        addSetting(autoSwitch); addSetting(rotate); addSetting(silent); addSetting(breakOnly); addSetting(realPackets);
    }

    @Override public void onDisable() {
        if (restoreSlot >= 0 && mc.player != null) {
            if (realPackets.get()) RealPackets.selectSlot(restoreSlot);
            else mc.player.getInventory().selectedSlot = restoreSlot;
        }
        restoreSlot = -1; setTag(null);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        long now = System.currentTimeMillis();

        EndCrystalEntity crystal = bestCrystal();
        if (crystal != null && now - lastBreak >= breakDelay.get()) {
            if (rotate.get()) lookAt(crystal.getPos().add(0, 0.5, 0));
            if (realPackets.get()) RealPackets.attackEntity(crystal);
            else { mc.interactionManager.attackEntity(mc.player, crystal); mc.player.swingHand(Hand.MAIN_HAND); }
            lastBreak = now; setTag("break"); return;
        }
        if (breakOnly.get()) return;
        if (now - lastPlace < placeDelay.get()) return;

        PlayerEntity target = nearestEnemy();
        if (target == null) { setTag(null); return; }
        int slot = findCrystalSlot();
        if (slot < 0) { setTag("no crystal"); return; }

        BlockPos best = bestPlacePos(target);
        if (best == null) { setTag(target.getName().getString()); return; }

        int prev = mc.player.getInventory().selectedSlot;
        if (autoSwitch.get() && slot != prev) {
            restoreSlot = prev;
            if (realPackets.get()) RealPackets.selectSlot(slot);
            else mc.player.getInventory().selectedSlot = slot;
        }
        if (rotate.get()) lookAt(Vec3d.ofCenter(best).add(0, 1, 0));
        BlockHitResult hit = new BlockHitResult(Vec3d.ofCenter(best).add(0, 0.5, 0), Direction.UP, best, false);
        mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
        mc.player.swingHand(Hand.MAIN_HAND);
        lastPlace = now; setTag("place");
    }

    private void lookAt(Vec3d pos) {
        Vec3d eyes = mc.player.getEyePos();
        double dx = pos.x - eyes.x, dy = pos.y - eyes.y, dz = pos.z - eyes.z;
        double h = Math.sqrt(dx*dx + dz*dz);
        float yaw = (float)(MathHelper.atan2(dz, dx)*(180.0/Math.PI)) - 90f;
        float pitch = MathHelper.clamp((float)-(MathHelper.atan2(dy, h)*(180.0/Math.PI)), -90f, 90f);
        if (silent.get() && realPackets.get()) RealPackets.sendLook(yaw, pitch, mc.player.isOnGround());
        else {
            mc.player.setYaw(yaw); mc.player.setPitch(pitch);
            if (realPackets.get()) RealPackets.sendLook(yaw, pitch, mc.player.isOnGround());
        }
    }

    private BlockPos bestPlacePos(PlayerEntity target) {
        BlockPos base = target.getBlockPos();
        BlockPos[] candidates = {
            base.down(), base.north(), base.south(), base.east(), base.west(),
            base.north().down(), base.south().down(), base.east().down(), base.west().down()
        };
        BlockPos best = null; double bestScore = -1;
        for (BlockPos floor : candidates) {
            if (!canPlaceCrystal(floor)) continue;
            double dist = mc.player.squaredDistanceTo(floor.getX()+0.5, floor.getY()+1, floor.getZ()+0.5);
            if (dist > range.get()*range.get()) continue;
            double enemyDist = target.squaredDistanceTo(floor.getX()+0.5, floor.getY()+1, floor.getZ()+0.5);
            double score = 100.0/(1.0+enemyDist) - dist*0.05;
            if (score > bestScore) { bestScore = score; best = floor; }
        }
        return best;
    }

    private boolean canPlaceCrystal(BlockPos floor) {
        var state = mc.world.getBlockState(floor);
        if (state.getBlock() != Blocks.OBSIDIAN && state.getBlock() != Blocks.BEDROCK) return false;
        return mc.world.getBlockState(floor.up()).isAir() && mc.world.getBlockState(floor.up(2)).isAir();
    }

    private EndCrystalEntity bestCrystal() {
        EndCrystalEntity best = null; double bestD = range.get();
        PlayerEntity enemy = nearestEnemy();
        for (Entity e : mc.world.getEntities()) {
            if (!(e instanceof EndCrystalEntity c)) continue;
            double d = mc.player.distanceTo(c);
            if (d > bestD) continue;
            if (enemy != null && c.distanceTo(enemy) > 6) continue;
            bestD = d; best = c;
        }
        return best;
    }

    private PlayerEntity nearestEnemy() {
        PlayerEntity best = null; double bestD = range.get() + 2.0;
        for (PlayerEntity p : mc.world.getPlayers()) {
            if (p == mc.player || !p.isAlive()) continue;
            try { if (JayHackClient.friendManager != null && JayHackClient.friendManager.isFriend(p.getName().getString())) continue; } catch (Throwable ignored) {}
            try { if (AntiBot.isBot(p)) continue; } catch (Throwable ignored) {}
            double d = mc.player.distanceTo(p);
            if (d < bestD) { bestD = d; best = p; }
        }
        return best;
    }

    private int findCrystalSlot() {
        for (int i = 0; i < 9; i++)
            if (mc.player.getInventory().getStack(i).isOf(Items.END_CRYSTAL)) return i;
        return -1;
    }

    private void setTag(String t) {
        try { var f = Module.class.getDeclaredField("tag"); f.setAccessible(true); f.set(this, t); } catch (Throwable ignored) {}
    }
}
