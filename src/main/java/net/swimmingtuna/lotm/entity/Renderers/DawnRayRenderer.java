package net.swimmingtuna.lotm.entity.Renderers;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.swimmingtuna.lotm.entity.DawnRayEntity;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class DawnRayRenderer extends EntityRenderer<DawnRayEntity> {
    private static final ResourceLocation BEAM_LOCATION = new ResourceLocation("lotm:textures/entity/dawn_ray_entity.png");
    private static final float BEAM_RADIUS = 0.5F;
    private static final float BEAM_LENGTH = 250.0F;

    public DawnRayRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(DawnRayEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        float brightness = 0.6F;
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.beaconBeam(BEAM_LOCATION, true));

        poseStack.pushPose();

        // Get current target position
        float targetX = entity.getCurrentX();
        float targetZ = entity.getCurrentZ();

        // Render the beam from origin to target
        renderBeam(poseStack, vertexConsumer, 0, 0, 0, targetX, -BEAM_LENGTH, targetZ, brightness, packedLight);

        poseStack.popPose();

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }

    // Rest of the rendering code remains the same
    private void renderBeam(PoseStack poseStack, VertexConsumer consumer,
                            float x1, float y1, float z1,
                            float x2, float y2, float z2,
                            float brightness, int packedLight) {
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix4f = pose.pose();
        Matrix3f matrix3f = pose.normal();

        float dx = x2 - x1;
        float dy = y2 - y1;
        float dz = z2 - z1;
        float length = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);

        dx /= length;
        dy /= length;
        dz /= length;

        for (int i = 0; i < 4; i++) {
            float wx1 = BEAM_RADIUS * (float) Math.cos(i * Math.PI / 2);
            float wz1 = BEAM_RADIUS * (float) Math.sin(i * Math.PI / 2);
            float wx2 = BEAM_RADIUS * (float) Math.cos((i + 1) * Math.PI / 2);
            float wz2 = BEAM_RADIUS * (float) Math.sin((i + 1) * Math.PI / 2);

            float x1w = x1 + wx1;
            float z1w = z1 + wz1;
            float x2w = x2 + wx1;
            float z2w = z2 + wz1;
            float x3w = x2 + wx2;
            float z3w = z2 + wz2;
            float x4w = x1 + wx2;
            float z4w = z1 + wz2;

            drawQuad(matrix4f, matrix3f, consumer, x1w, y1, z1w, x2w, y2, z2w, x3w, y2, z3w, x4w, y1, z4w,
                    0.0F, 0.0F, 1.0F, length / BEAM_LENGTH, brightness, packedLight, dx, dy, dz);
        }
    }

    private void drawQuad(Matrix4f matrix4f, Matrix3f matrix3f, VertexConsumer consumer, float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, float x4, float y4, float z4, float minU, float minV, float maxU, float maxV, float brightness, int packedLight, float normalX, float normalY, float normalZ) {
        // More vibrant yellow while maintaining transparency
        consumer.vertex(matrix4f, x1, y1, z1)
                .color(1.0F, 0.9F, 0.2F, brightness) // Vibrant yellow with transparency
                .uv(minU, minV)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(matrix3f, normalX, normalY, normalZ)
                .endVertex();
        consumer.vertex(matrix4f, x2, y2, z2)
                .color(1.0F, 0.9F, 0.2F, brightness)
                .uv(minU, maxV)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(matrix3f, normalX, normalY, normalZ)
                .endVertex();
        consumer.vertex(matrix4f, x3, y3, z3)
                .color(1.0F, 0.9F, 0.2F, brightness)
                .uv(maxU, maxV)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(matrix3f, normalX, normalY, normalZ)
                .endVertex();
        consumer.vertex(matrix4f, x4, y4, z4)
                .color(1.0F, 0.9F, 0.2F, brightness)
                .uv(maxU, minV)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(matrix3f, normalX, normalY, normalZ)
                .endVertex();
    }


    @Override
    public ResourceLocation getTextureLocation(DawnRayEntity entity) {
        return BEAM_LOCATION;
    }
}