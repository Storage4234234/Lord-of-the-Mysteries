package net.swimmingtuna.lotm.entity.Renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.swimmingtuna.lotm.LOTM;
import net.swimmingtuna.lotm.entity.Model.SwordOfTwilightEntityModel;
import net.swimmingtuna.lotm.entity.SwordOfTwilightEntity;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class SwordOfTwilightEntityRenderer extends GeoEntityRenderer<SwordOfTwilightEntity> {
    public SwordOfTwilightEntityRenderer(EntityRendererProvider.Context context) {
        super(context, new SwordOfTwilightEntityModel());
    }

    @Override
    public ResourceLocation getTextureLocation(SwordOfTwilightEntity animatable) {
        return new ResourceLocation(LOTM.MOD_ID, "textures/entity/sword_of_twilight_entity.png");
    }

    @Override
    protected void applyRotations(SwordOfTwilightEntity entity, PoseStack poseStack, float ageInTicks, float rotationYaw, float partialTicks) {
        super.applyRotations(entity, poseStack, ageInTicks, rotationYaw, partialTicks);
        poseStack.mulPose(Axis.YP.rotationDegrees(entity.getYaw()));
        poseStack.mulPose(Axis.XP.rotationDegrees(entity.getPitch()));
    }
}