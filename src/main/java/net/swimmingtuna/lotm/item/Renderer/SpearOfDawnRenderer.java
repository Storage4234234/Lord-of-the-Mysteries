package net.swimmingtuna.lotm.item.Renderer;

import net.swimmingtuna.lotm.item.Model.SpearOfDawnModel;
import net.swimmingtuna.lotm.item.Model.SwordOfDawnModel;
import net.swimmingtuna.lotm.item.OtherItems.SpearOfDawn;
import net.swimmingtuna.lotm.item.OtherItems.SwordOfDawn;
import software.bernie.geckolib.renderer.GeoItemRenderer;

public class SpearOfDawnRenderer extends GeoItemRenderer<SpearOfDawn> {

    public SpearOfDawnRenderer() {
        super(new SpearOfDawnModel());
    }
}
