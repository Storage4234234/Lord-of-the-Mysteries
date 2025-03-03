package net.swimmingtuna.lotm.entity.Renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.swimmingtuna.lotm.LOTM;
import net.swimmingtuna.lotm.entity.DivineHandLeftEntity;
import net.swimmingtuna.lotm.entity.Model.DivineHandLeftModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class DivineHandLeftRenderer extends GeoEntityRenderer<DivineHandLeftEntity> {
    public DivineHandLeftRenderer(EntityRendererProvider.Context context) {
        super(context, new DivineHandLeftModel());
    }

    @Override
    public ResourceLocation getTextureLocation(DivineHandLeftEntity animatable) {
        return new ResourceLocation(LOTM.MOD_ID, "textures/entity/divine_hand_right_entity.png");
    }

    @Override
    protected void applyRotations(DivineHandLeftEntity entity, PoseStack poseStack, float ageInTicks, float rotationYaw, float partialTicks) {
        super.applyRotations(entity, poseStack, ageInTicks, rotationYaw, partialTicks);
        poseStack.scale(-1.0F, 1.0F, 1.0F);
        poseStack.mulPose(Axis.YP.rotationDegrees(entity.getYaw()));
        poseStack.mulPose(Axis.XP.rotationDegrees(-entity.getPitch()));
    }
}