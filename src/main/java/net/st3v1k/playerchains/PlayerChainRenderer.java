package net.st3v1k.playerchains;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.LightmapTextureManager;
import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.render.VertexConsumerProvider;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.LightType;
import net.minecraft.world.World;
import org.joml.Matrix4f;

import java.util.UUID;

public class PlayerChainRenderer {

    public static void renderConnections(MatrixStack stack, Entity entity) {
        if (entity instanceof PlayerEntity player) {
            float pt = MinecraftClient.getInstance().getRenderTickCounter().getTickDelta(true);
            VertexConsumerProvider.Immediate buffers = MinecraftClient.getInstance().getBufferBuilders().getEntityVertexConsumers();
            World level = player.getWorld();
            ChainComponent var6 = PlayerChainsComponents.CHAIN.get(player);

            for (UUID uuid : var6.getConnections()) {
                PlayerEntity other = level.getPlayerByUuid(uuid);
                if (other != null) {
                    renderLeash(player, pt, stack, buffers, other);
                }
            }
        }
    }


    private static void renderLeash(PlayerEntity entity, float tickDelta, MatrixStack matrices, VertexConsumerProvider provider, PlayerEntity holdingEntity) {
        matrices.push();
        Vec3d vec3d = holdingEntity.getLerpedPos(tickDelta).add(0.0, holdingEntity.getBoundingBox().getLengthY() / 2.0, 0.0);
        double d = (double) (MathHelper.lerp(tickDelta, entity.prevBodyYaw, entity.bodyYaw) * 0.017453292F) + 1.5707963267948966;
        Vec3d vec3d2 = new Vec3d(0.0, holdingEntity.getBoundingBox().getLengthY() / 2.0, 0.0);
        double e = Math.cos(d) * vec3d2.z + Math.sin(d) * vec3d2.x;
        double f = Math.sin(d) * vec3d2.z - Math.cos(d) * vec3d2.x;
        double g = MathHelper.lerp(tickDelta, entity.prevX, entity.getX()) + e;
        double h = MathHelper.lerp(tickDelta, entity.prevY, entity.getY()) + vec3d2.y;
        double i = MathHelper.lerp(tickDelta, entity.prevZ, entity.getZ()) + f;
        matrices.translate(e, vec3d2.y, f);
        float j = (float) (vec3d.x - g);
        float k = (float) (vec3d.y - h);
        float l = (float) (vec3d.z - i);
        float m = 0.025F;
        VertexConsumer vertexConsumer = provider.getBuffer(RenderLayer.getLeash());
        Matrix4f matrix4f = matrices.peek().getPositionMatrix();
        float n = MathHelper.inverseSqrt(j * j + l * l) * 0.025F / 2.0F;
        float o = l * n;
        float p = j * n;
        BlockPos blockPos = BlockPos.ofFloored(entity.getCameraPosVec(tickDelta));
        BlockPos blockPos2 = BlockPos.ofFloored(holdingEntity.getCameraPosVec(tickDelta));
        int q = entity.getWorld().getLightLevel(LightType.BLOCK, blockPos);
        int r = holdingEntity.getWorld().getLightLevel(LightType.BLOCK, blockPos2);
        int s = entity.getWorld().getLightLevel(LightType.SKY, blockPos);
        int t = entity.getWorld().getLightLevel(LightType.SKY, blockPos2);

        int u;
        for (u = 0; u <= 24; ++u) {
            renderLeashPiece(vertexConsumer, matrix4f, j, k, l, q, r, s, t, 0.025F, 0.025F, o, p, u, false);
        }

        for (u = 24; u >= 0; --u) {
            renderLeashPiece(vertexConsumer, matrix4f, j, k, l, q, r, s, t, 0.025F, 0.0F, o, p, u, true);
        }

        matrices.pop();
    }

    private static void renderLeashPiece(VertexConsumer vertexConsumer, Matrix4f positionMatrix, float f, float g, float h, int leashedEntityBlockLight, int holdingEntityBlockLight, int leashedEntitySkyLight, int holdingEntitySkyLight, float i, float j, float k, float l, int pieceIndex, boolean isLeashKnot) {
        float m = (float) pieceIndex / 24.0F;
        int n = (int) MathHelper.lerp(m, (float) leashedEntityBlockLight, (float) holdingEntityBlockLight);
        int o = (int) MathHelper.lerp(m, (float) leashedEntitySkyLight, (float) holdingEntitySkyLight);
        int p = LightmapTextureManager.pack(n, o);
        float q = pieceIndex % 2 == (isLeashKnot ? 1 : 0) ? 0.7F : 1.0F;
        float r = 0.5F * q;
        float s = 0.4F * q;
        float t = 0.3F * q;
        float u = f * m;
        float v = g > 0.0F ? g * m * m : g - g * (1.0F - m) * (1.0F - m);
        float w = h * m;
        vertexConsumer.vertex(positionMatrix, u - k, v + j, w + l).color(r, s, t, 1.0F).light(p);
        vertexConsumer.vertex(positionMatrix, u + k, v + i - j, w - l).color(r, s, t, 1.0F).light(p);
    }
}
