package net.st3v1k.playerchains;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import org.jetbrains.annotations.NotNull;
import org.ladysnake.cca.api.v3.component.Component;
import org.ladysnake.cca.api.v3.component.ComponentProvider;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ClientTickingComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

import java.util.Set;
import java.util.UUID;

@SuppressWarnings("UnstableApiUsage")
public class ChainComponent implements AutoSyncedComponent, ClientTickingComponent, ServerTickingComponent, Component {
    private final PlayerEntity player;
    private final Set<UUID> connections = new ObjectOpenHashSet<>();

    public ChainComponent(PlayerEntity player) {
        this.player = player;
    }

    @NotNull
    public static Vec3d getCenter(PlayerEntity player) {
        return player.getPos().add(0.0, player.getBoundingBox().getLengthY() / 2.0, 0.0);
    }

    public void addConnection(UUID other) {
        this.connections.add(other);
        PlayerChainsComponents.CHAIN.sync(this.player);
        PlayerChainsComponents.CHAIN.syncWith((ServerPlayerEntity) this.player, (ComponentProvider) this.player);
    }

    public void removeConnection(UUID other) {
        this.connections.remove(other);
        PlayerChainsComponents.CHAIN.sync(this.player);
        PlayerChainsComponents.CHAIN.syncWith((ServerPlayerEntity) this.player, (ComponentProvider) this.player);
    }

    public void clientTick() {
        if (this.player.isMainPlayer()) {
            World level = this.player.getWorld();
            Vec3d resultingMotion = Vec3d.ZERO;

            for (UUID uuid : this.connections) {
                PlayerEntity other = level.getPlayerByUuid(uuid);
                if (other != null) {
                    Vec3d center = getCenter(this.player);
                    Vec3d otherCenter = getCenter(other);
                    double sqrDistance = center.squaredDistanceTo(otherCenter);
                    double chainMaxLength = PlayerChains.MAX_LENGTH;
                    if (sqrDistance > chainMaxLength * chainMaxLength) {
                        double distance = Math.sqrt(sqrDistance);
                        Vec3d diff = otherCenter.subtract(center).normalize();
                        resultingMotion = resultingMotion.add(diff.multiply((distance - chainMaxLength) * 0.1));
                    }
                }
            }

            double cap = 0.1;
            if (resultingMotion.length() > cap) {
                resultingMotion = resultingMotion.normalize().multiply(cap);
            }

            double horizontalVelocity = resultingMotion.multiply(1.0, 0.0, 1.0).multiply(20.0).length();
            if (this.player.isOnGround() && horizontalVelocity < 0.2) {
                return;
            }

            Entity applicationEntity = this.player;
            Entity vehicle = this.player.getVehicle();
            if (vehicle != null) {
                applicationEntity = vehicle;
            }

            applicationEntity.setVelocity(applicationEntity.getVelocity().add(resultingMotion));
        }

    }

    @Override
    public void readFromNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup wrapperLookup) {
        this.connections.clear();

        for (String key : nbt.getKeys()) {
            this.connections.add(UUID.fromString(key));
        }
    }

    @Override
    public void writeToNbt(NbtCompound nbt, RegistryWrapper.WrapperLookup wrapperLookup) {
        for (UUID uuid : this.connections) {
            nbt.putBoolean(uuid.toString(), true);
        }
    }

    public Set<UUID> getConnections() {
        return this.connections;
    }

    public void serverTick() {
        if (this.player instanceof ServerPlayerEntity serverPlayer) {
            serverPlayer.networkHandler.floatingTicks = 0;
        }
    }
}
