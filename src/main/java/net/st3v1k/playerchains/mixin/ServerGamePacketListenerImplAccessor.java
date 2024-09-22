package net.st3v1k.playerchains.mixin;

import net.minecraft.server.network.ServerPlayNetworkHandler;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin({ServerPlayNetworkHandler.class})
public interface ServerGamePacketListenerImplAccessor {
    @Accessor
    void setFloatingTicks(int var1);
}