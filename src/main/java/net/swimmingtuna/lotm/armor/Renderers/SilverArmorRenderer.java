package net.swimmingtuna.lotm.armor.Renderers;

import net.swimmingtuna.lotm.armor.Model.SilverArmorModel;
import net.swimmingtuna.lotm.armor.SilverArmorItem;
import software.bernie.geckolib.renderer.GeoArmorRenderer;

public class SilverArmorRenderer extends GeoArmorRenderer<SilverArmorItem> {
    public SilverArmorRenderer() {
        super(new SilverArmorModel());
    }
}