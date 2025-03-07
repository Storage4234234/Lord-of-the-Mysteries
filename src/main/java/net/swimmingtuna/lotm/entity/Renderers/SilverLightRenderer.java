package net.swimmingtuna.lotm.entity.Renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.swimmingtuna.lotm.LOTM;
import net.swimmingtuna.lotm.entity.Model.SilverLightModel;
import net.swimmingtuna.lotm.entity.SilverLightEntity;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class SilverLightRenderer extends GeoEntityRenderer<SilverLightEntity> {
    public SilverLightRenderer(EntityRendererProvider.Context context) {
        super(context, new SilverLightModel());
        this.shadowRadius = 0.3f;
    }

    @Override
    public ResourceLocation getTextureLocation(SilverLightEntity animatable) {
        return new ResourceLocation(LOTM.MOD_ID, "textures/entity/silver_sword_entity.png");
    }

    @Override
    public RenderType getRenderType(SilverLightEntity animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucent(getTextureLocation(animatable));
    }

    @Override
    protected void applyRotations(SilverLightEntity entity, PoseStack poseStack, float ageInTicks, float rotationYaw, float partialTicks) {
        // Apply base rotation for the model to match the forward direction
        poseStack.mulPose(Axis.YP.rotationDegrees(180.0F));

        // Apply the entity's stored yaw and pitch
        poseStack.mulPose(Axis.YP.rotationDegrees(-entity.getYaw()));
        poseStack.mulPose(Axis.XP.rotationDegrees(entity.getPitch()));

        // Apply model-specific correction to make the sword point forward
        poseStack.mulPose(Axis.ZP.rotationDegrees(90.0F));
    }
}