package com.jay.hackclient.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.network.packet.c2s.play.UpdateSelectedSlotC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.hit.BlockHitResult;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.util.math.Vec3d;

/** RealPackets — ONLY vanilla Fabric/Minecraft C2S packets. No custom packet classes. */
public final class RealPackets {

    private RealPackets() {}

    private static ClientPlayNetworkHandler net() {
        MinecraftClient mc = MinecraftClient.getInstance();
        return mc != null ? mc.getNetworkHandler() : null;
    }

    private static ClientPlayerEntity player() {
        MinecraftClient mc = MinecraftClient.getInstance();
        return mc != null ? mc.player : null;
    }

    // ------------------------------------------------------------------ move

    public static void sendPosition(double x, double y, double z, boolean onGround) {
        ClientPlayNetworkHandler n = net();
        if (n == null) return;
        n.sendPacket(new PlayerMoveC2SPacket.PositionAndOnGround(x, y, z, onGround, false));
    }

    public static void sendLook(float yaw, float pitch, boolean onGround) {
        ClientPlayNetworkHandler n = net();
        if (n == null) return;
        n.sendPacket(new PlayerMoveC2SPacket.LookAndOnGround(yaw, pitch, onGround, false));
    }

    public static void sendFull(double x, double y, double z, float yaw, float pitch, boolean onGround) {
        ClientPlayNetworkHandler n = net();
        if (n == null) return;
        n.sendPacket(new PlayerMoveC2SPacket.Full(x, y, z, yaw, pitch, onGround, false));
    }

    public static void sendOnGround(boolean onGround) {
        ClientPlayNetworkHandler n = net();
        if (n == null) return;
        n.sendPacket(new PlayerMoveC2SPacket.OnGroundOnly(onGround, false));
    }

    public static void syncPosition() {
        ClientPlayerEntity p = player();
        if (p == null) return;
        sendFull(p.getX(), p.getY(), p.getZ(), p.getYaw(), p.getPitch(), p.isOnGround());
    }

    // ---------------------------------------------------------------- sprint/sneak

    public static void startSprint() {
        ClientPlayNetworkHandler n = net();
        ClientPlayerEntity p = player();
        if (n == null || p == null) return;
        n.sendPacket(new ClientCommandC2SPacket(p, ClientCommandC2SPacket.Mode.START_SPRINTING));
    }

    public static void stopSprint() {
        ClientPlayNetworkHandler n = net();
        ClientPlayerEntity p = player();
        if (n == null || p == null) return;
        n.sendPacket(new ClientCommandC2SPacket(p, ClientCommandC2SPacket.Mode.STOP_SPRINTING));
    }

    // NOTE: 1.21.11 removed PRESS_SHIFT_KEY / RELEASE_SHIFT_KEY from
    // ClientCommandC2SPacket.Mode — sneak state is now carried by movement packets.

    // ---------------------------------------------------------------- attack

    public static void attackEntity(Entity target) {
        ClientPlayNetworkHandler n = net();
        ClientPlayerEntity p = player();
        if (n == null || p == null || target == null) return;
        n.sendPacket(PlayerInteractEntityC2SPacket.attack(target, p.isSneaking()));
        p.swingHand(Hand.MAIN_HAND);
    }

    public static void attackEntity(Entity target, boolean swing) {
        ClientPlayNetworkHandler n = net();
        ClientPlayerEntity p = player();
        if (n == null || p == null || target == null) return;
        n.sendPacket(PlayerInteractEntityC2SPacket.attack(target, p.isSneaking()));
        if (swing) p.swingHand(Hand.MAIN_HAND);
    }

    public static void interactEntity(Entity target, Hand hand) {
        ClientPlayNetworkHandler n = net();
        ClientPlayerEntity p = player();
        if (n == null || p == null || target == null) return;
        n.sendPacket(PlayerInteractEntityC2SPacket.interact(target, p.isSneaking(), hand));
    }

    // ---------------------------------------------------------------- slot

    /** Send UpdateSelectedSlotC2SPacket AND apply locally via setSelectedSlot (1.21.11: field is private). */
    public static void selectSlot(int slot) {
        if (slot < 0 || slot > 8) return;
        ClientPlayNetworkHandler n = net();
        if (n == null) return;
        n.sendPacket(new UpdateSelectedSlotC2SPacket(slot));
        ClientPlayerEntity p = player();
        if (p == null) return;
        try {
            p.getInventory().setSelectedSlot(slot);
        } catch (Throwable ignored) {}
    }

    /** Read the selected hotbar slot through the public getter (field is private in 1.21.11). */
    public static int getSelectedSlot() {
        ClientPlayerEntity p = player();
        if (p == null) return 0;
        try {
            return p.getInventory().getSelectedSlot();
        } catch (Throwable t) {
            return 0;
        }
    }

    /** Swap to slot, run action, restore previous slot. Null-safe. */
    public static void withSlot(int slot, Runnable action) {
        ClientPlayerEntity p = player();
        if (p == null || action == null) return;
        int prev = getSelectedSlot();
        if (slot != prev) selectSlot(slot);
        try {
            action.run();
        } finally {
            if (slot != prev) selectSlot(prev);
        }
    }

    // ---------------------------------------------------------------- block actions

    public static void startDestroyBlock(BlockPos pos, Direction face) {
        ClientPlayNetworkHandler n = net();
        if (n == null) return;
        n.sendPacket(new PlayerActionC2SPacket(
                PlayerActionC2SPacket.Action.START_DESTROY_BLOCK, pos, face));
    }

    public static void stopDestroyBlock(BlockPos pos, Direction face) {
        ClientPlayNetworkHandler n = net();
        if (n == null) return;
        n.sendPacket(new PlayerActionC2SPacket(
                PlayerActionC2SPacket.Action.STOP_DESTROY_BLOCK, pos, face));
    }

    public static void abortDestroyBlock(BlockPos pos, Direction face) {
        ClientPlayNetworkHandler n = net();
        if (n == null) return;
        n.sendPacket(new PlayerActionC2SPacket(
                PlayerActionC2SPacket.Action.ABORT_DESTROY_BLOCK, pos, face));
    }

    // ---------------------------------------------------------------- interaction convenience

    /** Look packet at a point (silent server-side look). */
    public static void lookAt(Vec3d from, Vec3d to, boolean onGround) {
        double dx = to.x - from.x, dy = to.y - from.y, dz = to.z - from.z;
        double horiz = Math.sqrt(dx * dx + dz * dz);
        if (horiz < 1.0E-6 && Math.abs(dy) < 1.0E-6) return;
        float yaw = (float) (Math.toDegrees(Math.atan2(dz, dx)) - 90.0);
        float pitch = (float) (-Math.toDegrees(Math.atan2(dy, Math.max(horiz, 1.0E-6))));
        sendLook(yaw, Math.max(-90f, Math.min(90f, pitch)), onGround);
    }

    /** Place a block via interaction manager (real PlayerInteractBlockC2S goes out through it). */
    public static boolean placeBlock(BlockHitResult hit) {
        MinecraftClient mc = MinecraftClient.getInstance();
        if (mc == null || mc.player == null || mc.interactionManager == null) return false;
        try {
            var result = mc.interactionManager.interactBlock(mc.player, Hand.MAIN_HAND, hit);
            mc.player.swingHand(Hand.MAIN_HAND);
            return result != null && result.isAccepted();
        } catch (Throwable t) {
            return false;
        }
    }
}
