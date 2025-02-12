package net.st3v1k.playerchains.mixin;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.GameRules;
import net.minecraft.world.World;
import net.minecraft.world.dimension.PortalManager;
import net.st3v1k.playerchains.ChainComponent;
import net.st3v1k.playerchains.PlayerChainsComponents;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayDeque;
import java.util.Deque;
import java.util.Set;
import java.util.UUID;

@Mixin(Entity.class)
public abstract class PortalProcessorMixin {

    @Shadow
    private World world;

    @Shadow
    @Nullable
    public PortalManager portalManager;

    @Shadow
    @Nullable
    public abstract MinecraftServer getServer();

    @Inject(
            method = "tickPortalTeleportation",
            at = @At("RETURN"),
            cancellable = true
    )
    private void postOnProcessPortalTeleportation(CallbackInfo ci) {
        if ((Object) this instanceof PlayerEntity player) {
            boolean anyOutOfPortal = false;
            Set<UUID> visited = new ObjectOpenHashSet<>();
            Deque<UUID> queue = new ArrayDeque<>();
            UUID rootUUID = player.getGameProfile().getId();
            visited.add(rootUUID);
            queue.add(rootUUID);

            label46:
            {
                PlayerEntity currentPlayer;
                do {
                    do {
                        do {
                            if (queue.isEmpty()) {
                                break label46;
                            }

                            UUID current = queue.poll();
                            currentPlayer = this.world.getPlayerByUuid(current);
                        } while (currentPlayer == null);

                        ChainComponent component = PlayerChainsComponents.CHAIN.get(currentPlayer);

                        for (UUID uuid : component.getConnections()) {
                            if (!visited.contains(uuid)) {
                                visited.add(uuid);
                                queue.add(uuid);
                            }
                        }
                    } while (currentPlayer == player);
                } while (this.portalManager != null && this.portalManager.getTicksInPortal() >= getPortalDelay(this.getServer(), (Entity) (Object) this));

                anyOutOfPortal = true;
            }

            if (anyOutOfPortal) {
                ci.cancel();
            }
        }
    }

    @Unique
    private int getPortalDelay(MinecraftServer server, Entity entity) {
        if (entity instanceof PlayerEntity playerEntity && server != null) {
            return Math.max(1, server.getGameRules().getInt(playerEntity.isCreative() ? GameRules.PLAYERS_NETHER_PORTAL_CREATIVE_DELAY : GameRules.PLAYERS_NETHER_PORTAL_DEFAULT_DELAY));
        } else {
            return 0;
        }
    }
}
