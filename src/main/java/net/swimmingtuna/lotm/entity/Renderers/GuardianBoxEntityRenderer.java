package net.swimmingtuna.lotm.entity.Renderers;

import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderStateShard;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.swimmingtuna.lotm.LOTM;
import net.swimmingtuna.lotm.entity.GuardianBoxEntity;
import org.joml.Matrix4f;

public class GuardianBoxEntityRenderer extends EntityRenderer<GuardianBoxEntity> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(LOTM.MOD_ID, "textures/entity/guardian_box.png");

    public GuardianBoxEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(GuardianBoxEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        entity.noCulling = true;
        float baseSize = 10f;
        float scaleFactor = Math.max(1f, entity.getMaxSize() / baseSize);
        float size = (float) entity.getMaxSize() / scaleFactor;
        float halfHeight = size / 2.0F;
        float halfWidth = halfHeight * 0.75f;
        VertexConsumer vertexConsumer = buffer.getBuffer(CUSTOM_RENDER_TYPE);
        Matrix4f matrix = poseStack.last().pose();
        renderFace(vertexConsumer, matrix, -halfWidth, -halfHeight, halfWidth, halfWidth, -halfHeight, halfWidth, halfWidth, halfHeight, halfWidth, -halfWidth, halfHeight, halfWidth, packedLight);
        renderFace(vertexConsumer, matrix, -halfWidth, -halfHeight, -halfWidth, -halfWidth, halfHeight, -halfWidth, halfWidth, halfHeight, -halfWidth, halfWidth, -halfHeight, -halfWidth, packedLight);
        renderFace(vertexConsumer, matrix, -halfWidth, -halfHeight, -halfWidth, -halfWidth, -halfHeight, halfWidth, -halfWidth, halfHeight, halfWidth, -halfWidth, halfHeight, -halfWidth, packedLight);
        renderFace(vertexConsumer, matrix, halfWidth, -halfHeight, -halfWidth, halfWidth, halfHeight, -halfWidth, halfWidth, halfHeight, halfWidth, halfWidth, -halfHeight, halfWidth, packedLight);
        renderFace(vertexConsumer, matrix, -halfWidth, halfHeight, -halfWidth, -halfWidth, halfHeight, halfWidth, halfWidth, halfHeight, halfWidth, halfWidth, halfHeight, -halfWidth, packedLight);
        renderFace(vertexConsumer, matrix, -halfWidth, -halfHeight, -halfWidth, halfWidth, -halfHeight, -halfWidth, halfWidth, -halfHeight, halfWidth, -halfWidth, -halfHeight, halfWidth, packedLight);
        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    private void renderFace(com.mojang.blaze3d.vertex.VertexConsumer vertexConsumer, Matrix4f matrix, float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, float x4, float y4, float z4, int packedLight) {
        vertexConsumer.vertex(matrix, x1, y1, z1).color(1.0F, 1.0F, 1.0F, 0.5F).uv(0.0F, 1.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0.0F, 1.0F, 0.0F).endVertex();
        vertexConsumer.vertex(matrix, x2, y2, z2).color(1.0F, 1.0F, 1.0F, 0.5F).uv(1.0F, 1.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0.0F, 1.0F, 0.0F).endVertex();
        vertexConsumer.vertex(matrix, x3, y3, z3).color(1.0F, 1.0F, 1.0F, 0.5F).uv(1.0F, 0.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0.0F, 1.0F, 0.0F).endVertex();
        vertexConsumer.vertex(matrix, x4, y4, z4).color(1.0F, 1.0F, 1.0F, 0.5F).uv(0.0F, 0.0F).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(0.0F, 1.0F, 0.0F).endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(GuardianBoxEntity entity) {
        return new ResourceLocation(LOTM.MOD_ID, "textures/entity/guardian_box.png");
    }

    private static final RenderType CUSTOM_RENDER_TYPE = RenderType.create(
            "guardian_box",
            DefaultVertexFormat.NEW_ENTITY,
            VertexFormat.Mode.QUADS,
            256,
            true,
            true,
            RenderType.CompositeState.builder()
                    .setShaderState(RenderStateShard.RENDERTYPE_ENTITY_TRANSLUCENT_SHADER)
                    .setTextureState(new RenderStateShard.TextureStateShard(
                            new ResourceLocation(LOTM.MOD_ID, "textures/entity/guardian_box.png"), false, false))
                    .setTransparencyState(RenderStateShard.TRANSLUCENT_TRANSPARENCY)
                    .setDepthTestState(RenderStateShard.LEQUAL_DEPTH_TEST)
                    .setLightmapState(RenderStateShard.LIGHTMAP)
                    .setCullState(RenderStateShard.NO_CULL)
                    .setWriteMaskState(RenderStateShard.COLOR_DEPTH_WRITE)
                    .createCompositeState(true)
    );

}