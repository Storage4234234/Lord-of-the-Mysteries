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
import org.joml.Quaternionf;

public class DawnRayRenderer extends EntityRenderer<DawnRayEntity> {
    private static final ResourceLocation BEAM_LOCATION = new ResourceLocation("lotm:textures/entity/dawn_ray_entity.png");
    private static final float BEAM_RADIUS = 0.5F;
    private static final float BEAM_LENGTH = 200.0F;

    public DawnRayRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(DawnRayEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        float brightness = 0.6F;
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.beaconBeam(BEAM_LOCATION, true));

        poseStack.pushPose();

        // Calculate the angle in radians
        float angleRad = (float) Math.toRadians(entity.getAngle());

        // Calculate the bottom point position
        float bottomX = (float) (BEAM_LENGTH * Math.cos(angleRad));
        float bottomZ = (float) (BEAM_LENGTH * Math.sin(angleRad));

        // Render the beam from top to bottom
        renderBeam(poseStack, vertexConsumer,
                0, 0, 0,           // Top point (fixed)
                bottomX, -BEAM_LENGTH, bottomZ, // Bottom point (moving)
                brightness, packedLight);

        poseStack.popPose();

        super.render(entity, entityYaw, partialTicks, poseStack, buffer, packedLight);
    }
    private void renderBeam(PoseStack poseStack, VertexConsumer consumer,
                            float x1, float y1, float z1,
                            float x2, float y2, float z2,
                            float brightness, int packedLight) {
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix4f = pose.pose();
        Matrix3f matrix3f = pose.normal();

        // Calculate beam direction for proper face normals
        float dx = x2 - x1;
        float dy = y2 - y1;
        float dz = z2 - z1;
        float length = (float) Math.sqrt(dx * dx + dy * dy + dz * dz);

        // Normalize direction vector
        dx /= length;
        dy /= length;
        dz /= length;

        // Calculate perpendicular vectors for beam width
        float px = -dz;
        float pz = dx;
        float mag = (float) Math.sqrt(px * px + pz * pz);
        if (mag > 0) {
            px /= mag;
            pz /= mag;
        }

        // Render the four sides of the beam
        for (int i = 0; i < 4; i++) {
            float wx = BEAM_RADIUS * px;
            float wz = BEAM_RADIUS * pz;

            if (i == 1 || i == 2) {
                wx = -wx;
                wz = -wz;
            }

            float x1w = x1 + wx;
            float z1w = z1 + wz;
            float x2w = x2 + wx;
            float z2w = z2 + wz;

            // Draw quad
            consumer.vertex(matrix4f, x1w, y1, z1w)
                    .color(1.0F, 1.0F, 0.0F, brightness)
                    .uv(0, 0)
                    .overlayCoords(OverlayTexture.NO_OVERLAY)
                    .uv2(packedLight)
                    .normal(matrix3f, dx, dy, dz)
                    .endVertex();
            consumer.vertex(matrix4f, x2w, y2, z2w)
                    .color(1.0F, 1.0F, 0.0F, brightness)
                    .uv(0, 1)
                    .overlayCoords(OverlayTexture.NO_OVERLAY)
                    .uv2(packedLight)
                    .normal(matrix3f, dx, dy, dz)
                    .endVertex();

            if (i < 2) {
                px = -pz;
                pz = px;
            }
        }
    }


    private void renderCube(float length, PoseStack poseStack, VertexConsumer consumer, float brightness, int packedLight) {
        PoseStack.Pose pose = poseStack.last();
        Matrix4f matrix4f = pose.pose();
        Matrix3f matrix3f = pose.normal();

        float u0 = 0.0F;
        float u1 = 1.0F;
        float v0 = 0.0F;
        float v1 = 1.0F;

        // Front
        drawQuad(matrix4f, matrix3f, consumer,
                -BEAM_RADIUS, 0, -BEAM_RADIUS,          // top-left
                -BEAM_RADIUS, -length, -BEAM_RADIUS,    // bottom-left
                BEAM_RADIUS, -length, -BEAM_RADIUS,     // bottom-right
                BEAM_RADIUS, 0, -BEAM_RADIUS,           // top-right
                u0, v0, u1, v1, brightness, packedLight, 0.0F, 0.0F, -1.0F);

        // Back
        drawQuad(matrix4f, matrix3f, consumer,
                BEAM_RADIUS, 0, BEAM_RADIUS,
                BEAM_RADIUS, -length, BEAM_RADIUS,
                -BEAM_RADIUS, -length, BEAM_RADIUS,
                -BEAM_RADIUS, 0, BEAM_RADIUS,
                u0, v0, u1, v1, brightness, packedLight, 0.0F, 0.0F, 1.0F);

        // Left
        drawQuad(matrix4f, matrix3f, consumer,
                -BEAM_RADIUS, 0, BEAM_RADIUS,
                -BEAM_RADIUS, -length, BEAM_RADIUS,
                -BEAM_RADIUS, -length, -BEAM_RADIUS,
                -BEAM_RADIUS, 0, -BEAM_RADIUS,
                u0, v0, u1, v1, brightness, packedLight, -1.0F, 0.0F, 0.0F);

        // Right
        drawQuad(matrix4f, matrix3f, consumer,
                BEAM_RADIUS, 0, -BEAM_RADIUS,
                BEAM_RADIUS, -length, -BEAM_RADIUS,
                BEAM_RADIUS, -length, BEAM_RADIUS,
                BEAM_RADIUS, 0, BEAM_RADIUS,
                u0, v0, u1, v1, brightness, packedLight, 1.0F, 0.0F, 0.0F);

        // Bottom (now at -length)
        drawQuad(matrix4f, matrix3f, consumer,
                -BEAM_RADIUS, -length, -BEAM_RADIUS,
                -BEAM_RADIUS, -length, BEAM_RADIUS,
                BEAM_RADIUS, -length, BEAM_RADIUS,
                BEAM_RADIUS, -length, -BEAM_RADIUS,
                u0, v0, u1, v1, brightness, packedLight, 0.0F, -1.0F, 0.0F);

        // Top (stays at 0)
        drawQuad(matrix4f, matrix3f, consumer,
                -BEAM_RADIUS, 0, -BEAM_RADIUS,
                BEAM_RADIUS, 0, -BEAM_RADIUS,
                BEAM_RADIUS, 0, BEAM_RADIUS,
                -BEAM_RADIUS, 0, BEAM_RADIUS,
                u0, v0, u1, v1, brightness, packedLight, 0.0F, 1.0F, 0.0F);
    }

    private void drawQuad(Matrix4f matrix4f, Matrix3f matrix3f, VertexConsumer consumer, float x1, float y1, float z1, float x2, float y2, float z2, float x3, float y3, float z3, float x4, float y4, float z4, float minU, float minV, float maxU, float maxV, float brightness, int packedLight, float normalX, float normalY, float normalZ) {
        consumer.vertex(matrix4f, x1, y1, z1)
                .color(1.0F, 1.0F, 0.0F, brightness)
                .uv(minU, minV)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(matrix3f, normalX, normalY, normalZ)
                .endVertex();
        consumer.vertex(matrix4f, x2, y2, z2)
                .color(1.0F, 1.0F, 0.0F, brightness)
                .uv(minU, maxV)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(matrix3f, normalX, normalY, normalZ)
                .endVertex();
        consumer.vertex(matrix4f, x3, y3, z3)
                .color(1.0F, 1.0F, 0.0F, brightness)
                .uv(maxU, maxV)
                .overlayCoords(OverlayTexture.NO_OVERLAY)
                .uv2(packedLight)
                .normal(matrix3f, normalX, normalY, normalZ)
                .endVertex();
        consumer.vertex(matrix4f, x4, y4, z4)
                .color(1.0F, 1.0F, 0.0F, brightness)
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