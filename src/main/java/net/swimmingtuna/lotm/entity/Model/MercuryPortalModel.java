package net.swimmingtuna.lotm.entity.Model;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.swimmingtuna.lotm.LOTM;
import net.swimmingtuna.lotm.entity.MercuryPortalEntity;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;

public class MercuryPortalModel extends GeoModel<MercuryPortalEntity> {

    @SuppressWarnings("removal")
    @Override
    public ResourceLocation getModelResource(MercuryPortalEntity mercuryPortalEntity) {
        return new ResourceLocation(LOTM.MOD_ID, "geo/mercury_portal.geo.json");
    }

    @SuppressWarnings("removal")
    @Override
    public ResourceLocation getTextureResource(MercuryPortalEntity mercuryPortalEntity) {
        return new ResourceLocation(LOTM.MOD_ID, "textures/entity/mercury_portal.png");
    }

    @Override
    public ResourceLocation getAnimationResource(MercuryPortalEntity mercuryPortalEntity) {
        return new ResourceLocation(LOTM.MOD_ID, "animations/mercury_portal.animation.json");
    }

    @Override
    public void setCustomAnimations(MercuryPortalEntity animatable, long instanceId, AnimationState<MercuryPortalEntity> animationState) {
        CoreGeoBone portal = getAnimationProcessor().getBone("portal");
        if (portal != null) {
            float yaw = animatable.getYRot();
            portal.setRotY(yaw * Mth.DEG_TO_RAD);
        }
    }
}
