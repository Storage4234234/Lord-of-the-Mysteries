package net.swimmingtuna.lotm.entity.Renderers;


import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.resources.ResourceLocation;
import net.swimmingtuna.lotm.LOTM;
import net.swimmingtuna.lotm.entity.AqueousLightEntityPull;
import net.swimmingtuna.lotm.entity.MercuryEntity;
import org.joml.Matrix3f;
import org.joml.Matrix4f;

public class MercuryEntityRenderer extends EntityRenderer<MercuryEntity> {

    public MercuryEntityRenderer(EntityRendererProvider.Context context) {
        super(context);
    }

    @Override
    public void render(MercuryEntity entity, float entityYaw, float partialTicks, PoseStack poseStack, MultiBufferSource buffers, int packedLight) {
        poseStack.pushPose();
        poseStack.popPose();
        super.render(entity, entityYaw, partialTicks, poseStack, buffers, packedLight);
    }


    @Override
    public ResourceLocation getTextureLocation(MercuryEntity entity) {
        return null;
    }

}