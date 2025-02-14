package net.swimmingtuna.lotm.armor.Renderers;

import net.swimmingtuna.lotm.armor.DawnArmorItem;
import net.swimmingtuna.lotm.armor.Model.DawnArmorModel;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public class DawnArmorRenderer extends GeoArmorRenderer<DawnArmorItem> {
    public DawnArmorRenderer() {
        super(new DawnArmorModel());
    }
}
