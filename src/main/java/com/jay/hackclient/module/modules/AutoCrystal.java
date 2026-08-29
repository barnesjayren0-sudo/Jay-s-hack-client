package com.jay.hackclient.module.modules;

import com.jay.hackclient.JayHackClient;
import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.NumberSetting;
import net.minecraft.block.Blocks;
import net.minecraft.entity.Entity;
import net.minecraft.entity.decoration.EndCrystalEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Items;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

/** Place + break end crystals near enemies (anarchy). */
public class AutoCrystal extends Module {

    public final NumberSetting range = new NumberSetting("Range", "Crystal range", 4.5, 2.0, 6.0, 0.1);
    public final NumberSetting delay = new NumberSetting("Delay", "Ms between actions", 50, 0, 200, 5);

    private long lastAction;

    public AutoCrystal() {
        super("AutoCrystal", "Place/break crystals on targets", Category.ANARCHY);
        addSetting(range);
        addSetting(delay);
    }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        long now = System.currentTimeMillis();
        if (now - lastAction < delay.getInt()) return;

        // Break nearest crystal first
        EndCrystalEntity crystal = nearestCrystal();
        if (crystal != null) {
            mc.interactionManager.attackEntity(mc.player, crystal);
            mc.player.swingHand(Hand.MAIN_HAND);
            lastAction = now;
            return;
        }

        PlayerEntity target = nearestEnemy();
        if (target == null) return;

        int slot = findCrystalSlot();
        if (slot < 0) return;

        BlockPos base = target.getBlockPos().down();
        BlockPos[] candidates = {
                base, base.north(), base.south(), base.east(), base.west(),
                target.getBlockPos().north(), target.getBlockPos().south(),
                target.getBlockPos().east(), target.getBlockPos().west()
        };

        int prev = 0;
        try { prev = mc.player.getInventory().getSelectedSlot(); } catch (Throwable ignored) {}
        try { mc.player.getInventory().setSelectedSlot(slot); } catch (Throwable ignored) {}

        for (BlockPos floor : candidates) {
            if (!canPlaceCrystal(floor)) continue;
            BlockHitResult hit = new BlockHitResult(Vec3d.ofCenter(floor), Direction.UP, floor, false);
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
            mc.player.swingHand(Hand.MAIN_HAND);
            lastAction = now;
            break;
        }

        try { mc.player.getInventory().setSelectedSlot(prev); } catch (Throwable ignored) {}
    }

    private boolean canPlaceCrystal(BlockPos floor) {
        if (mc.world.getBlockState(floor).getBlock() != Blocks.OBSIDIAN
                && mc.world.getBlockState(floor).getBlock() != Blocks.BEDROCK) return false;
        BlockPos a = floor.up();
        BlockPos b = floor.up(2);
        return mc.world.getBlockState(a).isAir() && mc.world.getBlockState(b).isAir();
    }

    private EndCrystalEntity nearestCrystal() {
        EndCrystalEntity best = null;
        double bestD = range.get();
        for (Entity e : mc.world.getEntities()) {
            if (!(e instanceof EndCrystalEntity c)) continue;
            double d = mc.player.distanceTo(c);
            if (d < bestD) {
                bestD = d;
                best = c;
            }
        }
        return best;
    }

    private PlayerEntity nearestEnemy() {
        PlayerEntity best = null;
        double bestD = range.get() + 1.5;
        for (PlayerEntity p : mc.world.getPlayers()) {
            if (p == mc.player || !p.isAlive()) continue;
            if (JayHackClient.friendManager != null
                    && JayHackClient.friendManager.isFriend(p.getName().getString())) continue;
            double d = mc.player.distanceTo(p);
            if (d < bestD) {
                bestD = d;
                best = p;
            }
        }
        return best;
    }

    private int findCrystalSlot() {
        for (int i = 0; i < 9; i++) {
            if (mc.player.getInventory().getStack(i).isOf(Items.END_CRYSTAL)) return i;
        }
        return -1;
    }
}
