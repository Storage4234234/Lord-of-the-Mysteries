package net.swimmingtuna.lotm.entity.Renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.swimmingtuna.lotm.LOTM;
import net.swimmingtuna.lotm.entity.GlobeOfTwilightEntity;
import net.swimmingtuna.lotm.entity.Model.GlobeOfTwilightModel;
import org.jetbrains.annotations.Nullable;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class GlobeOfTwilightRenderer extends GeoEntityRenderer<GlobeOfTwilightEntity> {
    public GlobeOfTwilightRenderer(EntityRendererProvider.Context context) {
        super(context, new GlobeOfTwilightModel());
    }

    @Override
    public ResourceLocation getTextureLocation(GlobeOfTwilightEntity animatable) {
        return new ResourceLocation(LOTM.MOD_ID, "textures/entity/globe_of_twilight_entity.png");
    }

    @Override
    protected void applyRotations(GlobeOfTwilightEntity entity, PoseStack poseStack, float ageInTicks, float rotationYaw, float partialTicks) {
        super.applyRotations(entity, poseStack, ageInTicks, rotationYaw, partialTicks);
        poseStack.scale(-1.0F, 1.0F, 1.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(entity.getYaw()));
        poseStack.mulPose(Axis.XP.rotationDegrees(-entity.getPitch()));
    }

    @Override
    public RenderType getRenderType(GlobeOfTwilightEntity animatable, ResourceLocation texture, @Nullable MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityTranslucent(texture);
    }

    @Override
    public void renderRecursively(PoseStack poseStack, GlobeOfTwilightEntity animatable, GeoBone bone, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        super.renderRecursively(poseStack, animatable, bone, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, 0.3f);
    }
}