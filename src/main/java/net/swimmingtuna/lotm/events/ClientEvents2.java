package net.swimmingtuna.lotm.events;

import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.client.event.MovementInputUpdateEvent;
import net.minecraftforge.client.event.RegisterParticleProvidersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.swimmingtuna.lotm.LOTM;
import net.swimmingtuna.lotm.entity.Model.*;
import net.swimmingtuna.lotm.entity.Renderers.*;
import net.swimmingtuna.lotm.entity.Renderers.PlayerMobRenderer.PlayerMobRenderer;
import net.swimmingtuna.lotm.init.EntityInit;
import net.swimmingtuna.lotm.init.ParticleInit;
import net.swimmingtuna.lotm.particle.*;
import net.swimmingtuna.lotm.util.effect.ModEffects;


@OnlyIn(Dist.CLIENT)
public class ClientEvents2 {


}
