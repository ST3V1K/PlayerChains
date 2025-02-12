package net.st3v1k.playerchains.mixin;

import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.entity.effect.StatusEffects;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.world.World;
import net.st3v1k.playerchains.ChainComponent;
import net.st3v1k.playerchains.PlayerChains;
import net.st3v1k.playerchains.PlayerChainsComponents;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.UUID;

@Mixin(ClientPlayerEntity.class)
public abstract class DoubleJumpMixin {
    @Unique
    private boolean playerchains$jumpingLastTick = false;

    @Inject(
            method = {"tickMovement"},
            at = {@At("HEAD")}
    )
    private void playerchains$tickMovement(CallbackInfo info) {
        ClientPlayerEntity player = (ClientPlayerEntity) (Object) this;
        if (!player.isOnGround() && !player.isClimbing() && !this.playerchains$jumpingLastTick && player.input.playerInput.jump() && !player.getAbilities().flying && this.canPerformJump(player)) {
            player.jump();
        }

        this.playerchains$jumpingLastTick = player.input.playerInput.jump();
    }

    @Unique
    private boolean canPerformJump(ClientPlayerEntity player) {
        World level = player.getWorld();
        ChainComponent chainComponent = PlayerChainsComponents.CHAIN.get(player);
        boolean canDoubleJump = false;
        double maxLen = PlayerChains.MAX_LENGTH;

        for (UUID uuid : chainComponent.getConnections()) {
            PlayerEntity other = level.getPlayerByUuid(uuid);
            if (other != null) {
                boolean farEnough = other.getBoundingBox().getCenter().distanceTo(player.getBoundingBox().getCenter()) > maxLen + 0.1;
                boolean below = player.getY() < other.getY() - maxLen * 0.8;
                if (farEnough && below) {
                    canDoubleJump = true;
                    break;
                }
            }
        }

        return canDoubleJump && !player.canGlide() && !player.isTouchingWater() && !player.hasStatusEffect(StatusEffects.LEVITATION);
    }
}
