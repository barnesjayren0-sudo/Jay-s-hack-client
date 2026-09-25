package com.jay.hackclient.module.modules;

import com.jay.hackclient.module.Module;
import com.jay.hackclient.module.setting.BoolSetting;
import com.jay.hackclient.module.setting.ModeSetting;
import com.jay.hackclient.module.setting.NumberSetting;
import com.jay.hackclient.util.RealPackets;
import net.minecraft.item.BlockItem;
import net.minecraft.item.ItemStack;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

/** Scaffold — Normal / Telly / Godbridge / Tower / Expand. Real look + slot packets. */
public class Scaffold extends Module {

    public final ModeSetting mode = new ModeSetting("Mode", "Place style", "Telly", "Normal", "Telly", "Godbridge", "Tower", "Expand");
    public final NumberSetting delay = new NumberSetting("Delay", "Base place ms", 45, 20, 150, 5);
    public final NumberSetting expand = new NumberSetting("Expand", "Blocks ahead", 1, 0, 3, 1);
    public final NumberSetting towerSpeed = new NumberSetting("Tower Speed", "Tower boost", 0.42, 0.3, 0.6, 0.01);
    public final BoolSetting autoJump = new BoolSetting("Auto Jump", "Jump on Telly/Tower", true);
    public final BoolSetting rotate = new BoolSetting("Rotate", "Look at place face", true);
    public final BoolSetting silentRotate = new BoolSetting("Silent", "Server look only", false);
    public final BoolSetting sprint = new BoolSetting("Sprint", "Keep sprint", true);
    public final BoolSetting autoSwitch = new BoolSetting("Auto Switch", "Switch to blocks", true);
    public final BoolSetting tower = new BoolSetting("Tower", "Tower when space held", true);
    public final BoolSetting realPackets = new BoolSetting("Real Packets", "Vanilla look/slot", true);

    private long lastPlace;
    private int ticksInAir;
    private float savedPitch = Float.NaN, savedYaw = Float.NaN;
    private int prevSlot = -1;
    private int placesThisSecond;
    private long secondStamp;

    public Scaffold() {
        super("Scaffold", "Telly / Normal / Godbridge / Tower / Expand", Category.WORLD);
        addSetting(mode); addSetting(delay); addSetting(expand); addSetting(towerSpeed);
        addSetting(autoJump); addSetting(rotate); addSetting(silentRotate); addSetting(sprint);
        addSetting(autoSwitch); addSetting(tower); addSetting(realPackets);
    }

    @Override public void onDisable() { restoreLook(); restoreSlot(); ticksInAir = 0; placesThisSecond = 0; setTag(null); }

    @Override
    public void onTick() {
        if (mc.player == null || mc.world == null || mc.interactionManager == null) return;
        if (mc.currentScreen != null) { restoreLook(); return; }
        if (autoSwitch.get() && !holdingBlock()) {
            int slot = findBlockSlot();
            if (slot >= 0) switchSlot(slot);
        }
        if (!holdingBlock()) { restoreLook(); setTag("no blocks"); return; }

        String m = mode.get();
        long now = System.currentTimeMillis();
        if (now - secondStamp >= 1000) { secondStamp = now; placesThisSecond = 0; }
        if (placesThisSecond >= 14) return;

        if (mc.player.isOnGround()) {
            ticksInAir = 0;
            if ("Telly".equals(m) && autoJump.get() && mc.options.forwardKey.isPressed()) mc.player.jump();
            if ("Tower".equals(m) || (tower.get() && mc.options.jumpKey.isPressed())) towerTick();
        } else ticksInAir++;

        if (now - lastPlace < delay.get()) return;
        if ("Telly".equals(m) && !mc.player.isOnGround() && ticksInAir < 2) return;

        if (sprint.get() && mc.options.forwardKey.isPressed()) {
            mc.player.setSprinting(true);
            if (realPackets.get()) RealPackets.startSprint();
        }

        BlockPos below = BlockPos.ofFloored(mc.player.getX(), mc.player.getY() - 0.2, mc.player.getZ());
        if ("Tower".equals(m) || (tower.get() && mc.options.jumpKey.isPressed())) below = mc.player.getBlockPos().down();

        boolean ok = tryPlace(below);
        if (!ok || "Expand".equals(m) || "Godbridge".equals(m)) {
            Vec3d look = mc.player.getRotationVector();
            int n = "Expand".equals(m) ? (int) expand.get() : 1;
            for (int i = 1; i <= Math.max(1, n); i++) {
                BlockPos ahead = below.add((int) Math.round(look.x * i), 0, (int) Math.round(look.z * i));
                if (tryPlace(ahead)) { ok = true; break; }
            }
        }
        if (ok) { lastPlace = now; placesThisSecond++; setTag(m + " " + placesThisSecond + "/s"); }
    }

    private void towerTick() {
        if (!mc.options.jumpKey.isPressed() && !autoJump.get()) return;
        if (mc.player.isOnGround()) mc.player.jump();
        var v = mc.player.getVelocity();
        if (v.y < 0.25) mc.player.setVelocity(v.x, towerSpeed.get(), v.z);
    }

    private boolean tryPlace(BlockPos target) {
        if (target == null || !mc.world.getBlockState(target).isReplaceable()) return false;
        Direction[] order = { Direction.DOWN, Direction.NORTH, Direction.SOUTH, Direction.EAST, Direction.WEST, Direction.UP };
        for (Direction dir : order) {
            BlockPos neighbor = target.offset(dir);
            if (mc.world.getBlockState(neighbor).isAir() || mc.world.getBlockState(neighbor).isReplaceable()) continue;
            Direction face = dir.getOpposite();
            if (rotate.get()) aimAt(neighbor, face);
            if (placeAgainst(neighbor, face)) return true;
        }
        return false;
    }

    private void aimAt(BlockPos neighbor, Direction face) {
        Vec3d hit = Vec3d.ofCenter(neighbor).add(face.getOffsetX()*0.5, face.getOffsetY()*0.5, face.getOffsetZ()*0.5);
        Vec3d eyes = mc.player.getEyePos();
        double dx = hit.x - eyes.x, dy = hit.y - eyes.y, dz = hit.z - eyes.z;
        double horiz = Math.sqrt(dx*dx + dz*dz);
        float yaw = (float)(Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
        float pitch = Math.max(-90f, Math.min(90f, (float)-Math.toDegrees(Math.atan2(dy, horiz))));
        if (Float.isNaN(savedPitch)) { savedPitch = mc.player.getPitch(); savedYaw = mc.player.getYaw(); }
        if (silentRotate.get() && realPackets.get()) RealPackets.sendLook(yaw, pitch, mc.player.isOnGround());
        else {
            mc.player.setYaw(yaw); mc.player.setPitch(pitch);
            if (realPackets.get()) RealPackets.sendLook(yaw, pitch, mc.player.isOnGround());
        }
    }

    private void restoreLook() {
        if (mc.player == null || Float.isNaN(savedPitch)) return;
        if (!silentRotate.get()) {
            mc.player.setPitch(savedPitch);
            if (!Float.isNaN(savedYaw)) mc.player.setYaw(savedYaw);
        }
        if (realPackets.get()) RealPackets.sendLook(Float.isNaN(savedYaw)?mc.player.getYaw():savedYaw, savedPitch, mc.player.isOnGround());
        savedPitch = Float.NaN; savedYaw = Float.NaN;
    }

    private boolean placeAgainst(BlockPos neighbor, Direction face) {
        Hand hand = Hand.MAIN_HAND;
        if (!(mc.player.getMainHandStack().getItem() instanceof BlockItem) && mc.player.getOffHandStack().getItem() instanceof BlockItem)
            hand = Hand.OFF_HAND;
        Vec3d hit = Vec3d.ofCenter(neighbor).add(face.getOffsetX()*0.5, face.getOffsetY()*0.5, face.getOffsetZ()*0.5);
        BlockHitResult bhr = new BlockHitResult(hit, face, neighbor, false);
        try {
            var result = mc.interactionManager.interactBlock(mc.player, hand, bhr);
            mc.player.swingHand(hand);
            return result != null && result.isAccepted();
        } catch (Exception e) {
            try { mc.interactionManager.interactBlock(mc.player, hand, bhr); mc.player.swingHand(hand); return true; }
            catch (Exception e2) { return false; }
        }
    }

    private boolean holdingBlock() {
        return mc.player.getMainHandStack().getItem() instanceof BlockItem || mc.player.getOffHandStack().getItem() instanceof BlockItem;
    }
    private int findBlockSlot() {
        for (int i = 0; i < 9; i++) {
            ItemStack s = mc.player.getInventory().getStack(i);
            if (!s.isEmpty() && s.getItem() instanceof BlockItem) return i;
        }
        return -1;
    }
    private void switchSlot(int slot) {
        if (prevSlot < 0) prevSlot = mc.player.getInventory().selectedSlot;
        if (realPackets.get()) RealPackets.selectSlot(slot); else mc.player.getInventory().selectedSlot = slot;
    }
    private void restoreSlot() {
        if (prevSlot >= 0 && mc.player != null) {
            if (realPackets.get()) RealPackets.selectSlot(prevSlot); else mc.player.getInventory().selectedSlot = prevSlot;
        }
        prevSlot = -1;
    }
    private void setTag(String t) {
        try { var f = Module.class.getDeclaredField("tag"); f.setAccessible(true); f.set(this, t); } catch (Throwable ignored) {}
    }
}
