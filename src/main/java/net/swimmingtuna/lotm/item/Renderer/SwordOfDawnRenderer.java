package net.swimmingtuna.lotm.item.Renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.swimmingtuna.lotm.LOTM;
import net.swimmingtuna.lotm.entity.LowSequenceDoorEntity;
import net.swimmingtuna.lotm.entity.Model.LowSequenceDoorModel;
import net.swimmingtuna.lotm.item.Model.SwordOfDawnModel;
import net.swimmingtuna.lotm.item.OtherItems.SwordOfDawn;
import software.bernie.geckolib.model.GeoModel;
import software.bernie.geckolib.renderer.GeoEntityRenderer;
import software.bernie.geckolib.renderer.GeoItemRenderer;
import software.bernie.geckolib.renderer.layer.AutoGlowingGeoLayer;

public class SwordOfDawnRenderer extends GeoItemRenderer<SwordOfDawn> {

    public SwordOfDawnRenderer() {
        super(new SwordOfDawnModel());
    }
}
