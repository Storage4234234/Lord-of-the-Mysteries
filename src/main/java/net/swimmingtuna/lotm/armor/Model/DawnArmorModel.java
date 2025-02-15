package net.swimmingtuna.lotm.armor.Model;

import net.minecraft.resources.ResourceLocation;
import net.swimmingtuna.lotm.LOTM;
import net.swimmingtuna.lotm.armor.DawnArmorItem;
import software.bernie.geckolib.model.GeoModel;

public class DawnArmorModel extends GeoModel<DawnArmorItem> {
    @Override
    public ResourceLocation getModelResource(DawnArmorItem dawnArmorItem) {
        return new ResourceLocation(LOTM.MOD_ID, "geo/dawn_armor.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(DawnArmorItem dawnArmorItem) {
        return new ResourceLocation(LOTM.MOD_ID, "textures/armor/dawn_armor.png");
    }

    @Override
    public ResourceLocation getAnimationResource(DawnArmorItem dawnArmorItem) {
        return new ResourceLocation(LOTM.MOD_ID, "animations/dawn_armor.animation.json");
    }
}
