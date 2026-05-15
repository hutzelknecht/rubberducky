package de.immerdieses.rubberducky;

import de.immerdieses.rubberducky.client.model.RubberDuckModel;
import de.immerdieses.rubberducky.client.renderer.RubberDuckRenderer;
import de.immerdieses.rubberducky.registry.ModEntities;
import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.EntityRenderersEvent;
import net.neoforged.neoforge.client.gui.ConfigurationScreen;
import net.neoforged.neoforge.client.gui.IConfigScreenFactory;

@Mod(value = RubberDucky.MODID, dist = Dist.CLIENT)
@EventBusSubscriber(modid = RubberDucky.MODID, value = Dist.CLIENT)
public class RubberDuckyClient {

    public RubberDuckyClient(ModContainer container) {
        container.registerExtensionPoint(IConfigScreenFactory.class, ConfigurationScreen::new);
    }

    @SubscribeEvent
    static void onClientSetup(FMLClientSetupEvent event) {
        RubberDucky.LOGGER.info("Rubber Ducky client setup — quack!");
    }

    @SubscribeEvent
    static void registerRenderers(EntityRenderersEvent.RegisterRenderers event) {
        event.registerEntityRenderer(ModEntities.RUBBER_DUCK.get(), RubberDuckRenderer::new);
    }

    @SubscribeEvent
    static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event) {
        event.registerLayerDefinition(RubberDuckModel.LAYER_LOCATION, RubberDuckModel::createBodyLayer);
    }
}
