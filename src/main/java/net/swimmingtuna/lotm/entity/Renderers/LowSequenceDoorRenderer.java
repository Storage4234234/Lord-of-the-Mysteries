package net.swimmingtuna.lotm.entity.Renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.swimmingtuna.lotm.LOTM;
import net.swimmingtuna.lotm.entity.LowSequenceDoorEntity;
import net.swimmingtuna.lotm.entity.Model.LowSequenceDoorModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class LowSequenceDoorRenderer extends GeoEntityRenderer<LowSequenceDoorEntity> {
    public LowSequenceDoorRenderer(EntityRendererProvider.Context context) {
        super(context, new LowSequenceDoorModel());
    }

    @Override
    public ResourceLocation getTextureLocation(LowSequenceDoorEntity animatable) {
        return new ResourceLocation(LOTM.MOD_ID, "textures/entity/low_sequence_door.png");
    }

    @Override
    protected void applyRotations(LowSequenceDoorEntity entity, PoseStack poseStack, float ageInTicks, float rotationYaw, float partialTicks) {
        super.applyRotations(entity, poseStack, ageInTicks, rotationYaw, partialTicks);

        poseStack.mulPose(Axis.YP.rotationDegrees(entity.getYaw()));
    }
}
