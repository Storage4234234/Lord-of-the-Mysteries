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
    // Store previous motion for smooth interpolation
    private double prevMotionX = 0;
    private double prevMotionY = 0;
    private double prevMotionZ = 0;

    public SpearOfDawnRenderer(EntityRendererProvider.Context context) {
        super(context, new SpearOfDawnModel());
        this.shadowRadius = 0.0F; // Disable shadow
    }

    @Override
    public ResourceLocation getTextureLocation(SpearOfDawnEntity animatable) {
        return new ResourceLocation(LOTM.MOD_ID, "textures/entity/spear_of_dawn.png");
    }

    @Override
    public void render(SpearOfDawnEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        // Save the current state
        poseStack.pushPose();

        // Calculate direction from motion instead of relying on entity rotation
        Vec3 motion = entity.getDeltaMovement();

        // Update previous motion for next frame interpolation
        if (prevMotionX == 0 && prevMotionY == 0 && prevMotionZ == 0) {
            // Initialize on first render
            prevMotionX = motion.x;
            prevMotionY = motion.y;
            prevMotionZ = motion.z;
        }

        // Interpolate motion for smooth rotation
        double lerpX = Mth.lerp(partialTick, prevMotionX, motion.x);
        double lerpY = Mth.lerp(partialTick, prevMotionY, motion.y);
        double lerpZ = Mth.lerp(partialTick, prevMotionZ, motion.z);

        // Store for next frame
        prevMotionX = motion.x;
        prevMotionY = motion.y;
        prevMotionZ = motion.z;

        // Calculate rotation based on interpolated motion
        double horizontalDist = Math.sqrt(lerpX * lerpX + lerpZ * lerpZ);
        float yaw = (float) (Math.atan2(lerpX, lerpZ) * (180.0D / Math.PI));
        float pitch = (float) (Math.atan2(lerpY, horizontalDist) * (180.0D / Math.PI));

        // Apply rotations to the pose stack
        poseStack.mulPose(Axis.YP.rotationDegrees(yaw - 90.0F));
        poseStack.mulPose(Axis.ZP.rotationDegrees(pitch + 90.0F));
        poseStack.mulPose(Axis.XP.rotationDegrees(180.0F));

        // Do not modify entity's rotation at all
        // Instead pass 0 for entityYaw to avoid GeckoLib applying its own rotations
        super.render(entity, 0, partialTick, poseStack, bufferSource, packedLight);

        // Restore the pose stack state
        poseStack.popPose();
    }

    @Override
    protected void applyRotations(SpearOfDawnEntity entity, PoseStack poseStack, float ageInTicks, float rotationYaw, float partialTicks) {
        // Skip the default rotation behavior completely
        // Everything is handled in render()
    }

    @Override
    public void preRender(PoseStack poseStack, SpearOfDawnEntity animatable, BakedGeoModel model, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        super.preRender(poseStack, animatable, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
    }
}