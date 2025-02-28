package net.swimmingtuna.lotm.entity.Model;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.swimmingtuna.lotm.LOTM;
import net.swimmingtuna.lotm.entity.LowSequenceDoorEntity;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;

public class LowSequenceDoorModel extends GeoModel<LowSequenceDoorEntity> {

    @SuppressWarnings("removal")
    @Override
    public ResourceLocation getModelResource(LowSequenceDoorEntity lowSequenceDoorEntity) {
        return new ResourceLocation(LOTM.MOD_ID, "geo/low_sequence_door.geo.json");
    }

    @SuppressWarnings("removal")
    @Override
    public ResourceLocation getTextureResource(LowSequenceDoorEntity lowSequenceDoorEntity) {
        return new ResourceLocation(LOTM.MOD_ID, "textures/entity/low_sequence_door.png");
    }

    @Override
    public ResourceLocation getAnimationResource(LowSequenceDoorEntity lowSequenceDoorEntity) {
        return new ResourceLocation(LOTM.MOD_ID, "animations/low_sequence_door.animation.json");
    }

    @Override
    public void setCustomAnimations(LowSequenceDoorEntity animatable, long instanceId, AnimationState<LowSequenceDoorEntity> animationState) {
        CoreGeoBone door = getAnimationProcessor().getBone("door");

        if (door != null) {
            float yaw = animatable.getYRot();
            door.setRotY(yaw * Mth.DEG_TO_RAD);
        }
    }
}
