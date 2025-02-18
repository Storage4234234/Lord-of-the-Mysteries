package net.swimmingtuna.lotm.item.Model;

import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.swimmingtuna.lotm.LOTM;
import net.swimmingtuna.lotm.entity.LowSequenceDoorEntity;
import net.swimmingtuna.lotm.item.OtherItems.SwordOfDawn;
import software.bernie.geckolib.core.animatable.model.CoreGeoBone;
import software.bernie.geckolib.core.animation.AnimationState;
import software.bernie.geckolib.model.GeoModel;

public class SwordOfDawnModel extends GeoModel<SwordOfDawn> {

    public SwordOfDawnModel() {

    }

    @SuppressWarnings("removal")
    @Override
    public ResourceLocation getModelResource(SwordOfDawn swordOfDawn) {
        return new ResourceLocation(LOTM.MOD_ID, "geo/sword_of_dawn.geo.json");
    }

    @SuppressWarnings("removal")
    @Override
    public ResourceLocation getTextureResource(SwordOfDawn swordOfDawn) {
        return new ResourceLocation(LOTM.MOD_ID, "textures/item/sword_of_dawn.png");
    }

    @SuppressWarnings("removal")
    @Override
    public ResourceLocation getAnimationResource(SwordOfDawn swordOfDawn) {
        return null;
    }
}
