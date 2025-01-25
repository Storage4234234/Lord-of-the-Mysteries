package net.swimmingtuna.lotm.entity.Renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.swimmingtuna.lotm.LOTM;
import net.swimmingtuna.lotm.entity.GuardianBoxEntity;
import org.jetbrains.annotations.NotNull;
import org.joml.Matrix4f;

public class GuardianBoxEntityRenderer extends EntityRenderer<GuardianBoxEntity> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(LOTM.MOD_ID, "textures/entity/guardian_box.png");

    public GuardianBoxEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(GuardianBoxEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();

        // Make horizontal dimensions smaller while keeping height same
        float baseSize = 10f; // Your base max size
        float scaleFactor = Math.max(1f, entity.getMaxSize() / baseSize);
        float size = (float) entity.getMaxSize() / scaleFactor;

        float halfHeight = size / 2.0F;
        float halfWidth = halfHeight * 0.75f;

        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityTranslucent(getTextureLocation(entity)));

        Matrix4f matrix = poseStack.last().pose();

        // Front face
        renderFace(vertexConsumer, matrix,
                -halfWidth, -halfHeight, halfWidth,
                halfWidth, -halfHeight, halfWidth,
                halfWidth, halfHeight, halfWidth,
                -halfWidth, halfHeight, halfWidth,
                packedLight);

        // Back face
        renderFace(vertexConsumer, matrix,
                -halfWidth, -halfHeight, -halfWidth,
                -halfWidth, halfHeight, -halfWidth,
                halfWidth, halfHeight, -halfWidth,
                halfWidth, -halfHeight, -halfWidth,
                packedLight);

        // Left face
        renderFace(vertexConsumer, matrix,
                -halfWidth, -halfHeight, -halfWidth,
                -halfWidth, -halfHeight, halfWidth,
                -halfWidth, halfHeight, halfWidth,
                -halfWidth, halfHeight, -halfWidth,
                packedLight);

        // Right face
        renderFace(vertexConsumer, matrix,
                halfWidth, -halfHeight, -halfWidth,
                halfWidth, halfHeight, -halfWidth,
                halfWidth, halfHeight, halfWidth,
                halfWidth, -halfHeight, halfWidth,
                packedLight);

        // Top face
        renderFace(vertexConsumer, matrix,
                -halfWidth, halfHeight, -halfWidth,
                -halfWidth, halfHeight, halfWidth,
                halfWidth, halfHeight, halfWidth,
                halfWidth, halfHeight, -halfWidth,
                packedLight);

        // Bottom face
        renderFace(vertexConsumer, matrix,
                -halfWidth, -halfHeight, -halfWidth,
                halfWidth, -halfHeight, -halfWidth,
                halfWidth, -halfHeight, halfWidth,
                -halfWidth, -halfHeight, halfWidth,
                packedLight);

        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    private void renderFace(com.mojang.blaze3d.vertex.VertexConsumer vertexConsumer, Matrix4f matrix, float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, float x4, float y4, float z4, int packedLight) {
        vertexConsumer.vertex(matrix, x1, y1, z1)
                .color(1.0F, 1.0F, 1.0F, 0.2F)
                .uv(0.0F, 1.0F)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(0.0F, 1.0F, 0.0F)
                .endVertex();

        vertexConsumer.vertex(matrix, x2, y2, z2)
                .color(1.0F, 1.0F, 1.0F, 0.2F)
                .uv(1.0F, 1.0F)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(0.0F, 1.0F, 0.0F)
                .endVertex();

        vertexConsumer.vertex(matrix, x3, y3, z3)
                .color(1.0F, 1.0F, 1.0F, 0.2F)
                .uv(1.0F, 0.0F)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(0.0F, 1.0F, 0.0F)
                .endVertex();

        vertexConsumer.vertex(matrix, x4, y4, z4)
                .color(1.0F, 1.0F, 1.0F, 0.2F)
                .uv(0.0F, 0.0F)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(0.0F, 1.0F, 0.0F)
                .endVertex();
    }

    @Override
    public @NotNull ResourceLocation getTextureLocation(@NotNull GuardianBoxEntity entity) {
        return TEXTURE;
    }
}