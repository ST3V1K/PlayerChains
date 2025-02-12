package net.st3v1k.playerchains.mixin;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.entity.Entity;
import net.minecraft.server.PlayerManager;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.st3v1k.playerchains.ChainComponent;
import net.st3v1k.playerchains.PlayerChainsComponents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Set;
import java.util.UUID;

@Mixin({PlayerManager.class})
public abstract class PlayerListMixin {
    @Inject(
            method = {"respawnPlayer"},
            at = {@At("RETURN")}
    )
    public void respawn(ServerPlayerEntity serverPlayer, boolean alive, Entity.RemovalReason removalReason, CallbackInfoReturnable<ServerPlayerEntity> cir) {
        ServerPlayerEntity player = cir.getReturnValue();
        ServerWorld level = player.getServerWorld();
        Set<UUID> visited = new ObjectOpenHashSet<>();
        Deque<UUID> queue = new ArrayDeque<>();
        UUID rootUUID = player.getGameProfile().getId();
        visited.add(rootUUID);
        queue.add(rootUUID);

        while (true) {
            ServerPlayerEntity currentPlayer;
            do {
                if (queue.isEmpty()) {
                    return;
                }

                UUID current = queue.poll();
                currentPlayer = level.getServer().getPlayerManager().getPlayer(current);
            } while (currentPlayer == null);

            ChainComponent component = PlayerChainsComponents.CHAIN.get(currentPlayer);

            for (UUID uuid : component.getConnections()) {
                if (!visited.contains(uuid)) {
                    visited.add(uuid);
                    queue.add(uuid);
                }
            }

            if (currentPlayer != player) {
                currentPlayer.teleport((ServerWorld) player.getWorld(), player.getX(), player.getY(), player.getZ(), Set.of(), player.getYaw(), player.getPitch(), false);
            }
        }
    }
}

