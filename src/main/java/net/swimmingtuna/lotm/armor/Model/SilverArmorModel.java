package net.swimmingtuna.lotm.armor.Model;

import net.minecraft.resources.ResourceLocation;
import net.swimmingtuna.lotm.LOTM;
import net.swimmingtuna.lotm.armor.SilverArmorItem;
import software.bernie.geckolib.model.GeoModel;

public class SilverArmorModel extends GeoModel<SilverArmorItem> {

    @SuppressWarnings("removal")
    @Override
    public ResourceLocation getModelResource(SilverArmorItem silverArmorItem) {
        return new ResourceLocation(LOTM.MOD_ID, "geo/silver_armor.geo.json");
    }

    @SuppressWarnings("removal")
    @Override
    public ResourceLocation getTextureResource(SilverArmorItem silverArmorItem) {
        return new ResourceLocation(LOTM.MOD_ID, "textures/armor/silver_armor.png");
    }

    @SuppressWarnings("removal")
    @Override
    public ResourceLocation getAnimationResource(SilverArmorItem silverArmorItem) {
        return new ResourceLocation(LOTM.MOD_ID, "animations/silver_armor.animation.json");
    }
}