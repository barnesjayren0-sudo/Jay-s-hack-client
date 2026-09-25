package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.BoolSetting;
import com.jay.hackclient.module.setting.NumberSetting;
import com.jay.hackclient.util.RealPackets;
import net.minecraft.block.Block;
import net.minecraft.block.Blocks;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

/** Surround — obsidian box. Real slot/look packets. */
public class Surround extends Module {

    public final NumberSetting delay = new NumberSetting("Delay", "Ms between places", 50, 20, 150, 5);
    public final BoolSetting center = new BoolSetting("Center", "Center before surround", true);
    public final BoolSetting autoSwitch = new BoolSetting("Auto Switch", "Switch to obsidian", true);
    public final BoolSetting realPackets = new BoolSetting("Real Packets", "Vanilla slot packets", true);
    public final BoolSetting disableComplete = new BoolSetting("Auto Disable", "Off when done", true);

    private long lastPlace;
    private int restoreSlot = -1;

    public Surround() {
        super("Surround", "Box yourself with obsidian", Category.ANARCHY);
        addSetting(delay); addSetting(center); addSetting(autoSwitch); addSetting(realPackets); addSetting(disableComplete);
    }

    @Override public void onEnable() {
        if (center.get() && mc.player != null) {
            BlockPos bp = mc.player.getBlockPos();
            mc.player.setPosition(bp.getX()+0.5, mc.player.getY(), bp.getZ()+0.5);
            if (realPackets.get()) RealPackets.syncPosition();
        }
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
        if (!mc.player.isOnGround()) return;
        long now = System.currentTimeMillis();
        if (now - lastPlace < delay.get()) return;

        BlockPos feet = mc.player.getBlockPos();
        Direction[] dirs = { Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST };
        int missing = 0;
        for (Direction d : dirs)
            if (mc.world.getBlockState(feet.offset(d)).isReplaceable()) missing++;
        if (missing == 0) {
            setTag("done");
            if (disableComplete.get()) setEnabled(false);
            return;
        }

        int slot = findBlockSlot();
        if (slot < 0) { setTag("no blocks"); return; }

        int prev = mc.player.getInventory().selectedSlot;
        if (autoSwitch.get() && slot != prev) {
            restoreSlot = prev;
            if (realPackets.get()) RealPackets.selectSlot(slot);
            else mc.player.getInventory().selectedSlot = slot;
        }

        for (Direction d : dirs) {
            BlockPos target = feet.offset(d);
            if (!mc.world.getBlockState(target).isReplaceable()) continue;
            BlockHitResult hit;
            if (!mc.world.getBlockState(target.down()).isAir() && !mc.world.getBlockState(target.down()).isReplaceable())
                hit = new BlockHitResult(Vec3d.ofCenter(target.down()).add(0, 0.5, 0), Direction.UP, target.down(), false);
            else
                hit = new BlockHitResult(Vec3d.ofCenter(feet).add(d.getOffsetX()*0.5, 0, d.getOffsetZ()*0.5), d, feet, false);
            if (realPackets.get()) RealPackets.sendLook(mc.player.getYaw(), 70f, true);
            mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
            mc.player.swingHand(Hand.MAIN_HAND);
            lastPlace = now; setTag(d.getName() + " " + missing); break;
        }
    }

    private int findBlockSlot() {
        for (int i = 0; i < 9; i++) {
            ItemStack s = mc.player.getInventory().getStack(i);
            if (s.isEmpty() || !(s.getItem() instanceof BlockItem bi)) continue;
            Block b = bi.getBlock();
            if (b == Blocks.OBSIDIAN || b == Blocks.CRYING_OBSIDIAN || b == Blocks.ENDER_CHEST || b == Blocks.ANCIENT_DEBRIS)
                return i;
        }
        return -1;
    }

    private void setTag(String t) {
        try { var f = Module.class.getDeclaredField("tag"); f.setAccessible(true); f.set(this, t); } catch (Throwable ignored) {}
    }
}
