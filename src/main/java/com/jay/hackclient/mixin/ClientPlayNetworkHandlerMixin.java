package com.jay.hackclient.mixin;

import net.minecraft.client.network.ClientPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Velocity is handled in EntityVelocityUpdateS2CPacketMixin (getVelocity).
 * This class kept empty so older mixin refs do not double-apply KB.
 */
@Mixin(ClientPlayNetworkHandler.class)
public class ClientPlayNetworkHandlerMixin {
}
