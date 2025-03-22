package net.swimmingtuna.lotm.entity.mob;

import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.resources.ResourceLocation;
import net.swimmingtuna.lotm.entity.mob.BeyonderEntity;

public class MonsterBeyonderZombieRenderer extends MobRenderer<BeyonderEntity, HumanoidModel<BeyonderEntity>> {
    private static final ResourceLocation ZOMBIE_TEXTURE = new ResourceLocation("textures/entity/zombie/zombie.png");

    public MonsterBeyonderZombieRenderer(EntityRendererProvider.Context context) {

        super(context,
                new HumanoidModel<>(context.bakeLayer(ModelLayers.ZOMBIE)),
                0.5F);
    }

    @Override
    public ResourceLocation getTextureLocation(BeyonderEntity entity) {
        return ZOMBIE_TEXTURE;
    }
}