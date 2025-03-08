package net.swimmingtuna.lotm.entity.Model;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.swimmingtuna.lotm.LOTM;
import net.swimmingtuna.lotm.entity.DivineHandLeftEntity;
import net.swimmingtuna.lotm.entity.LowSequenceDoorEntity;
import net.swimmingtuna.lotm.entity.MercuryCageEntity;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;

public class MercuryCageModel extends GeoModel<MercuryCageEntity> {

    @SuppressWarnings("removal")
    @Override
    public ResourceLocation getModelResource(MercuryCageEntity mercuryCageEntity) {
        return new ResourceLocation(LOTM.MOD_ID, "geo/silver_cage.geo.json");
    }

    @SuppressWarnings("removal")
    @Override
    public ResourceLocation getTextureResource(MercuryCageEntity mercuryCageEntity) {
        return new ResourceLocation(LOTM.MOD_ID, "textures/entity/divine_hand_right_entity.png");
    }

    @Override
    public ResourceLocation getAnimationResource(MercuryCageEntity mercuryCageEntity) {
        return new ResourceLocation(LOTM.MOD_ID, "animations/silver_cage.animation.json");
    }

    @Override
    public void setCustomAnimations(MercuryCageEntity animatable, long instanceId, AnimationState<MercuryCageEntity> animationState) {
        CoreGeoBone door = getAnimationProcessor().getBone("cage");
        if (door != null) {
            float yaw = animatable.getYRot();
            door.setRotY(yaw * Mth.DEG_TO_RAD);
        }
    }
}
