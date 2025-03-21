package net.swimmingtuna.lotm.entity.Model;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.swimmingtuna.lotm.LOTM;
import net.swimmingtuna.lotm.entity.SwordOfTwilightEntity;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;

public class SwordOfTwilightEntityModel extends GeoModel<SwordOfTwilightEntity> {

    @SuppressWarnings("removal")
    @Override
    public ResourceLocation getModelResource(SwordOfTwilightEntity lowSequenceDoorEntity) {
        return new ResourceLocation(LOTM.MOD_ID, "geo/sword_of_twilight_entity.geo.json");
    }

    @SuppressWarnings("removal")
    @Override
    public ResourceLocation getTextureResource(SwordOfTwilightEntity lowSequenceDoorEntity) {
        return new ResourceLocation(LOTM.MOD_ID, "textures/entity/sword_of_twilight_entity.png");
    }

    @Override
    public ResourceLocation getAnimationResource(SwordOfTwilightEntity lowSequenceDoorEntity) {
        return new ResourceLocation(LOTM.MOD_ID, "animations/sword_of_twilight.animation.json");
    }

    @Override
    public void setCustomAnimations(SwordOfTwilightEntity animatable, long instanceId, AnimationState<SwordOfTwilightEntity> animationState) {
        CoreGeoBone sword = getAnimationProcessor().getBone("entity");
        if (sword != null) {
            float yaw = animatable.getYRot();
            sword.setRotY(yaw * Mth.DEG_TO_RAD);
        }
    }
}
