package net.swimmingtuna.lotm.entity.Renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.swimmingtuna.lotm.LOTM;
import net.swimmingtuna.lotm.entity.SpearOfDawnEntity;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class SpearOfDawnRenderer extends EntityRenderer<SpearOfDawnEntity> {
    private static final ResourceLocation TEXTURE_LOCATION = new ResourceLocation(LOTM.MOD_ID, "textures/entity/spear_of_dawn.png");

    public SpearOfDawnRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(SpearOfDawnEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        poseStack.pushPose();
        poseStack.mulPose(Axis.YP.rotationDegrees(entity.getYaw() - 90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(entity.getPitch()));
        poseStack.mulPose(Axis.XP.rotationDegrees(45.0F));
        poseStack.scale(0.05625F, 0.05625F, 0.05625F);
        poseStack.translate(-4.0F, 0.0F, 0.0F);
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.entityCutout(getTextureLocation(entity)));
        PoseStack.Pose poseEntry = poseStack.last();
        Matrix4f pose = poseEntry.pose();
        Matrix3f normal = poseEntry.normal();
        vertex(pose, normal, vertexConsumer, -7, -2, -2, 0.0F, 0.15625F, -1, 0, 0, packedLight);
        vertex(pose, normal, vertexConsumer, -7, -2, 2, 0.15625F, 0.15625F, -1, 0, 0, packedLight);
        vertex(pose, normal, vertexConsumer, -7, 2, 2, 0.15625F, 0.3125F, -1, 0, 0, packedLight);
        vertex(pose, normal, vertexConsumer, -7, 2, -2, 0.0F, 0.3125F, -1, 0, 0, packedLight);
        vertex(pose, normal, vertexConsumer, -7, 2, -2, 0.0F, 0.15625F, 1, 0, 0, packedLight);
        vertex(pose, normal, vertexConsumer, -7, 2, 2, 0.15625F, 0.15625F, 1, 0, 0, packedLight);
        vertex(pose, normal, vertexConsumer, -7, -2, 2, 0.15625F, 0.3125F, 1, 0, 0, packedLight);
        vertex(pose, normal, vertexConsumer, -7, -2, -2, 0.0F, 0.3125F, 1, 0, 0, packedLight);
        for (int i = 0; i < 4; ++i) {
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0F));
            vertex(pose, normal, vertexConsumer, -12, -2, 0, 0.0F, 0.0F, 0, 1, 0, packedLight);
            vertex(pose, normal, vertexConsumer, 12, -2, 0, 1.0F, 0.0F, 0, 1, 0, packedLight);
            vertex(pose, normal, vertexConsumer, 12, 2, 0, 1.0F, 0.15625F, 0, 1, 0, packedLight);
            vertex(pose, normal, vertexConsumer, -12, 2, 0, 0.0F, 0.15625F, 0, 1, 0, packedLight);
        }
        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    private void vertex(Matrix4f matrix, Matrix3f normal, VertexConsumer consumer, int x, int y, int z, float u, float v, int normalX, int normalY, int normalZ, int packedLight) {
        consumer.vertex(matrix, (float)x, (float)y, (float)z).color(255, 255, 255, 255).uv(u, v).overlayCoords(OverlayTexture.NO_OVERLAY).uv2(packedLight).normal(normal, (float)normalX, (float)normalY, (float)normalZ).endVertex();
    }

    @Override
    public ResourceLocation getTextureLocation(SpearOfDawnEntity entity) {
        return TEXTURE_LOCATION;
    }
}