package com.jay.hackclient.util;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.ClientPlayNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.network.packet.c2s.play.ClientCommandC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerActionC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerInteractEntityC2SPacket;
import net.minecraft.network.packet.c2s.play.PlayerMoveC2SPacket;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.entity.Entity;

/**
 * RealPackets — ONLY vanilla Fabric/Minecraft C2S packets.
 * No custom packet classes. Orchard / Ghost style.
 */
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

    public static void startSneak() {
        ClientPlayNetworkHandler n = net();
        ClientPlayerEntity p = player();
        if (n == null || p == null) return;
        n.sendPacket(new ClientCommandC2SPacket(p, ClientCommandC2SPacket.Mode.PRESS_SHIFT_KEY));
    }

    public static void stopSneak() {
        ClientPlayNetworkHandler n = net();
        ClientPlayerEntity p = player();
        if (n == null || p == null) return;
        n.sendPacket(new ClientCommandC2SPacket(p, ClientCommandC2SPacket.Mode.RELEASE_SHIFT_KEY));
    }

    public static void attackEntity(Entity target) {
        ClientPlayNetworkHandler n = net();
        ClientPlayerEntity p = player();
        if (n == null || p == null || target == null) return;
        n.sendPacket(PlayerInteractEntityC2SPacket.attack(target, p.isSneaking()));
        p.swingHand(Hand.MAIN_HAND);
    }

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
}
