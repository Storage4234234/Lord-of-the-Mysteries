package net.swimmingtuna.lotm.entity.Renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.swimmingtuna.lotm.LOTM;
import net.swimmingtuna.lotm.entity.Model.SpearOfDawnModel;
import net.swimmingtuna.lotm.entity.SpearOfDawnEntity;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class SpearOfDawnRenderer extends GeoEntityRenderer<SpearOfDawnEntity> {
    public SpearOfDawnRenderer(EntityRendererProvider.Context context) {
        super(context, new SpearOfDawnModel());
    }

    @Override
    public ResourceLocation getTextureLocation(SpearOfDawnEntity animatable) {
        return new ResourceLocation(LOTM.MOD_ID, "textures/entity/spear_of_dawn.png");
    }

    @Override
    public void render(SpearOfDawnEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        super.render(entity, 0, partialTick, poseStack, bufferSource, packedLight);
    }

    @Override
    protected void applyRotations(SpearOfDawnEntity entity, PoseStack poseStack, float ageInTicks, float rotationYaw, float partialTicks) {
        float lerpYRot = Mth.lerp(partialTicks, entity.yRotO, entity.getYRot());
        float lerpXRot = Mth.lerp(partialTicks, entity.xRotO, entity.getXRot());
        entity.setYRot(0);
        entity.setXRot(0);
        entity.yRotO = 0;
        entity.xRotO = 0;
        poseStack.mulPose(Axis.YP.rotationDegrees(lerpYRot - 90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(lerpXRot + 90.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));
    }

    @Override
    public void preRender(PoseStack poseStack, SpearOfDawnEntity animatable, BakedGeoModel model, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
        animatable.xRotO = animatable.getXRot();
        animatable.yRotO = animatable.getYRot();
    }

    @Override
    public void postRender(PoseStack poseStack, SpearOfDawnEntity animatable, BakedGeoModel model, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        super.postRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
        Vec3 motion = animatable.getDeltaMovement();
        double horizontalDist = Math.sqrt(motion.x * motion.x + motion.z * motion.z);
        animatable.setYRot((float) (Math.atan2(motion.x, motion.z) * (180.0D / Math.PI)));
        animatable.setXRot((float) (Math.atan2(motion.y, horizontalDist) * (180.0D / Math.PI)));
    }
}