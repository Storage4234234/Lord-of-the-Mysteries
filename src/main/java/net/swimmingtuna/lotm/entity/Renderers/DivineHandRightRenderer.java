package net.swimmingtuna.lotm.entity.Renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.swimmingtuna.lotm.LOTM;
import net.swimmingtuna.lotm.entity.DivineHandRightEntity;
import net.swimmingtuna.lotm.entity.Model.DivineHandRightModel;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class DivineHandRightRenderer extends GeoEntityRenderer<DivineHandRightEntity> {
    public DivineHandRightRenderer(EntityRendererProvider.Context context) {
        super(context, new DivineHandRightModel());
    }

    @Override
    public ResourceLocation getTextureLocation(DivineHandRightEntity animatable) {
        return new ResourceLocation(LOTM.MOD_ID, "textures/entity/divine_hand_right_entity.png");
    }

    @Override
    protected void applyRotations(DivineHandRightEntity entity, PoseStack poseStack, float ageInTicks, float rotationYaw, float partialTicks) {
        super.applyRotations(entity, poseStack, ageInTicks, rotationYaw, partialTicks);
        poseStack.mulPose(Axis.YP.rotationDegrees(-entity.getYaw()));
        poseStack.mulPose(Axis.XP.rotationDegrees(-entity.getPitch()));

    }

    @Override
    public void preRender(PoseStack poseStack, DivineHandRightEntity animatable, BakedGeoModel model, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
