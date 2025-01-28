package net.swimmingtuna.lotm.entity.Renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.swimmingtuna.lotm.entity.HurricaneOfLightEntity;

public class HurricaneOfLightEntityRenderer extends EntityRenderer<HurricaneOfLightEntity> {

    public HurricaneOfLightEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(HurricaneOfLightEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.popPose();
        poseStack.pushPose();
        super.render(entity, entityYaw, partialTicks, poseStack, bufferSource, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(HurricaneOfLightEntity entity) {
        return null;
    }
}
