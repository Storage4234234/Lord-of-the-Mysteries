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
        float yaw = entity.getYaw();
        float pitch = entity.getPitch();
        poseStack.mulPose(Axis.YP.rotationDegrees(-yaw));
        poseStack.mulPose(Axis.ZP.rotationDegrees(-pitch));
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.lines());
        PoseStack.Pose pose = poseStack.last();
        Matrix4f poseMatrix = pose.pose();
        Matrix3f normalMatrix = pose.normal();
        float lineLength = 5.0F;
        vertexConsumer.vertex(poseMatrix, 0, 0, 0).color(255, 165, 0, 255).normal(normalMatrix, 1, 0, 0).endVertex();
        vertexConsumer.vertex(poseMatrix, lineLength, 0, 0).color(255, 165, 0, 255).normal(normalMatrix, 1, 0, 0).endVertex();
        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(SpearOfDawnEntity entity) {
        return TEXTURE_LOCATION;
    }
}