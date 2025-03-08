package net.swimmingtuna.lotm.entity.Renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.swimmingtuna.lotm.LOTM;
import net.swimmingtuna.lotm.entity.LowSequenceDoorEntity;
import net.swimmingtuna.lotm.entity.MercuryCageEntity;
import net.swimmingtuna.lotm.entity.Model.LowSequenceDoorModel;
import net.swimmingtuna.lotm.entity.Model.MercuryCageModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class MercuryCageRenderer extends GeoEntityRenderer<MercuryCageEntity> {
    public MercuryCageRenderer(EntityRendererProvider.Context context) {
        super(context, new MercuryCageModel());
    }

    @Override
    public ResourceLocation getTextureLocation(MercuryCageEntity animatable) {
        return new ResourceLocation(LOTM.MOD_ID, "textures/entity/mercury_cage.png");
    }

    @Override
    protected void applyRotations(MercuryCageEntity entity, PoseStack poseStack, float ageInTicks, float rotationYaw, float partialTicks) {
        super.applyRotations(entity, poseStack, ageInTicks, rotationYaw, partialTicks);
        poseStack.mulPose(Axis.XP.rotationDegrees(entity.getPitch()));
        poseStack.mulPose(Axis.YP.rotationDegrees(entity.getYaw()));
    }
}
