package net.swimmingtuna.lotm.entity.Renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.swimmingtuna.lotm.LOTM;
import net.swimmingtuna.lotm.entity.LowSequenceDoorEntity;
import net.swimmingtuna.lotm.entity.MidSequenceDoorEntity;
import net.swimmingtuna.lotm.entity.Model.LowSequenceDoorModel;
import net.swimmingtuna.lotm.entity.Model.MidSequenceDoorModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class MidSequenceDoorRenderer extends GeoEntityRenderer<MidSequenceDoorEntity> {
    public MidSequenceDoorRenderer(EntityRendererProvider.Context context) {
        super(context, new MidSequenceDoorModel());
    }

    @Override
    public ResourceLocation getTextureLocation(MidSequenceDoorEntity animatable) {
        return new ResourceLocation(LOTM.MOD_ID, "textures/entity/mid_sequence_door.png");
    }

    @Override
    protected void applyRotations(MidSequenceDoorEntity entity, PoseStack poseStack, float ageInTicks, float rotationYaw, float partialTicks) {
        super.applyRotations(entity, poseStack, ageInTicks, rotationYaw, partialTicks);

        poseStack.mulPose(Axis.YP.rotationDegrees(entity.getYaw()));
    }
}