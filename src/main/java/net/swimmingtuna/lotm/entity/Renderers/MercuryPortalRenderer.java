package net.swimmingtuna.lotm.entity.Renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.swimmingtuna.lotm.LOTM;
import net.swimmingtuna.lotm.entity.MercuryPortalEntity;
import net.swimmingtuna.lotm.entity.Model.MercuryPortalModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class MercuryPortalRenderer extends GeoEntityRenderer<MercuryPortalEntity> {
    public MercuryPortalRenderer(EntityRendererProvider.Context context) {
        super(context, new MercuryPortalModel());
    }

    @Override
    public ResourceLocation getTextureLocation(MercuryPortalEntity animatable) {
        return new ResourceLocation(LOTM.MOD_ID, "textures/entity/mercury_portal.png");
    }

    @Override
    protected void applyRotations(MercuryPortalEntity entity, PoseStack poseStack, float ageInTicks, float rotationYaw, float partialTicks) {
        super.applyRotations(entity, poseStack, ageInTicks, rotationYaw, partialTicks);
        poseStack.mulPose(Axis.XP.rotationDegrees(entity.getPitch()));
        poseStack.mulPose(Axis.YP.rotationDegrees(entity.getYaw()));
    }
}
