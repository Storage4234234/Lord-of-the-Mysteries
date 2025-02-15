package net.swimmingtuna.lotm.entity.Model;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.swimmingtuna.lotm.LOTM;
import net.swimmingtuna.lotm.entity.MidSequenceDoorEntity;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;

public class MidSequenceDoorModel extends GeoModel<MidSequenceDoorEntity> {

    @SuppressWarnings("removal")
    @Override
    public ResourceLocation getModelResource(MidSequenceDoorEntity midSequenceDoorEntity) {
        return new ResourceLocation(LOTM.MOD_ID, "geo/mid_sequence_door.geo.json");
    }

    @SuppressWarnings("removal")
    @Override
    public ResourceLocation getTextureResource(MidSequenceDoorEntity midSequenceDoorEntity) {
        return new ResourceLocation(LOTM.MOD_ID, "textures/entity/mid_sequence_door.png");
    }

    @Override
    public ResourceLocation getAnimationResource(MidSequenceDoorEntity midSequenceDoorEntity) {
        return new ResourceLocation(LOTM.MOD_ID, "animations/mid_sequence_door.animation.json");
    }

    @Override
    public void setCustomAnimations(MidSequenceDoorEntity animatable, long instanceId, AnimationState<MidSequenceDoorEntity> animationState) {
        CoreGeoBone door = getAnimationProcessor().getBone("door");

        if (door != null) {
            float yaw = animatable.getYRot();
            door.setRotY(yaw * Mth.DEG_TO_RAD);
        }
    }
}