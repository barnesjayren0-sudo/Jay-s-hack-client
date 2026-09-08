package com.jay.client18.module.modules;

import com.jay.client18.module.Module;
import com.jay.client18.util.Humanizer;
import net.minecraft.block.Block;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemStack;
import net.minecraft.util.BlockPos;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.Vec3;
import org.lwjgl.input.Keyboard;

/** Quiet bridge assist — soft pitch, humanized place delay. */
public class Scaffold extends Module {

    private long lastPlace;
    private float savedPitch = Float.NaN;
    private int pitchTicks;
    private int placeDelay = 55;

    public Scaffold() {
        super("Scaffold", "Silent bridge assist", Category.MOVEMENT);
        setKeyBind(Keyboard.KEY_G);
    }

    @Override
    public void onDisable() {
        restorePitch();
    }

    @Override
    public void onTick() {
        if (mc.thePlayer == null || mc.theWorld == null) return;
        if (mc.currentScreen != null) return;
        if (!holdingBlock()) {
            restorePitch();
            return;
        }

        if (pitchTicks > 0) {
            pitchTicks--;
            if (pitchTicks == 0) softRestore();
        }

        long now = System.currentTimeMillis();
        if (now - lastPlace < placeDelay) return;

        BlockPos below = new BlockPos(mc.thePlayer.posX, mc.thePlayer.posY - 0.05, mc.thePlayer.posZ);
        if (!mc.theWorld.isAirBlock(below)) return;

        if (tryPlace(below)) {
            lastPlace = now;
            placeDelay = 45 + Humanizer.delay(12, 8);
            pitchTicks = 3;
        }
    }

    private boolean tryPlace(BlockPos target) {
        EnumFacing[] faces = {
                EnumFacing.DOWN, EnumFacing.NORTH, EnumFacing.SOUTH,
                EnumFacing.EAST, EnumFacing.WEST
        };
        for (int i = 0; i < faces.length; i++) {
            EnumFacing face = faces[i];
            BlockPos n = target.offset(face);
            if (mc.theWorld.isAirBlock(n)) continue;
            Block b = mc.theWorld.getBlockState(n).getBlock();
            if (b.getMaterial().isReplaceable()) continue;

            aimDown();
            EnumFacing placeFace = face.getOpposite();
            Vec3 hit = new Vec3(
                    n.getX() + 0.5 + placeFace.getFrontOffsetX() * 0.5,
                    n.getY() + 0.5 + placeFace.getFrontOffsetY() * 0.5,
                    n.getZ() + 0.5 + placeFace.getFrontOffsetZ() * 0.5
            );
            try {
                boolean ok = mc.playerController.onPlayerRightClick(
                        mc.thePlayer, mc.theWorld, mc.thePlayer.getHeldItem(),
                        n, placeFace, hit);
                if (ok) {
                    mc.thePlayer.swingItem();
                    return true;
                }
            } catch (Throwable ignored) {}
        }
        return false;
    }

    private void aimDown() {
        if (Float.isNaN(savedPitch)) savedPitch = mc.thePlayer.rotationPitch;
        float target = 74f + (float) (Math.random() * 4);
        float cur = mc.thePlayer.rotationPitch;
        mc.thePlayer.rotationPitch = cur + (target - cur) * 0.22f;
    }

    private void softRestore() {
        if (Float.isNaN(savedPitch)) return;
        float cur = mc.thePlayer.rotationPitch;
        mc.thePlayer.rotationPitch = cur + (savedPitch - cur) * 0.4f;
        if (Math.abs(mc.thePlayer.rotationPitch - savedPitch) < 2f) restorePitch();
    }

    private void restorePitch() {
        if (!Float.isNaN(savedPitch) && mc.thePlayer != null) {
            mc.thePlayer.rotationPitch = savedPitch;
        }
        savedPitch = Float.NaN;
        pitchTicks = 0;
    }

    private boolean holdingBlock() {
        ItemStack s = mc.thePlayer.getHeldItem();
        return s != null && s.getItem() instanceof ItemBlock;
    }
}
