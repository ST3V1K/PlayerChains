package net.st3v1k.playerchains.mixin;

import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.render.entity.EntityRenderer;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.st3v1k.playerchains.PlayerChainRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin({EntityRenderer.class})
public abstract class EntityRendererMixin {
    @Inject(
            method = "render",
            at = @At("HEAD")
    )
    public void render(Entity entity, float f, float g, MatrixStack poseStack, VertexConsumerProvider multiBufferSource, int i, CallbackInfo ci) {
        if (entity instanceof PlayerEntity) {
            PlayerChainRenderer.renderConnections(poseStack, entity);
        }
    }
}
