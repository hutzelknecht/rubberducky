package de.immerdieses.rubberducky;

import de.immerdieses.rubberducky.entity.RubberDuckEntity;
import de.immerdieses.rubberducky.registry.ModEntities;
import de.immerdieses.rubberducky.registry.ModItems;
import de.immerdieses.rubberducky.registry.ModSounds;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.CreativeModeTabs;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.entity.EntityAttributeCreationEvent;
import net.neoforged.neoforge.event.entity.EntityMountEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;

@Mod(RubberDucky.MODID)
public class RubberDucky {

    public static final String MODID = "rubberducky";
    public static final Logger LOGGER = LogUtils.getLogger();

    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS =
            DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);

    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> RUBBER_DUCKY_TAB =
            CREATIVE_MODE_TABS.register("rubber_ducky_tab", () -> CreativeModeTab.builder()
                    .title(Component.translatable("itemGroup.rubberducky"))
                    .withTabsBefore(CreativeModeTabs.SPAWN_EGGS)
                    .icon(() -> ModItems.RUBBER_DUCK_SPAWN_EGG.get().getDefaultInstance())
                    .displayItems((params, output) -> output.accept(ModItems.RUBBER_DUCK_SPAWN_EGG.get()))
                    .build());

    public RubberDucky(IEventBus modEventBus, ModContainer modContainer) {
        modEventBus.addListener(this::commonSetup);

        ModEntities.ENTITY_TYPES.register(modEventBus);
        ModItems.ITEMS.register(modEventBus);
        ModSounds.SOUND_EVENTS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);

        modEventBus.addListener(this::registerAttributes);

        // Block mid-air dismount: Shift only descends; dismount happens once duck lands
        NeoForge.EVENT_BUS.addListener(this::onEntityMount);
    }

    /** Prevent the player from dismounting mid-air. Shift descends; landing allows dismount. */
    private void onEntityMount(EntityMountEvent event) {
        if (event.isDismounting()
                && event.getEntityBeingMounted() instanceof RubberDuckEntity duck
                && !duck.onGround()) {
            event.setCanceled(true);
        }
    }

    private void commonSetup(FMLCommonSetupEvent event) {
        LOGGER.info("Rubber Ducky mod loaded — quack!");
    }

    @SubscribeEvent
    public void registerAttributes(EntityAttributeCreationEvent event) {
        event.put(ModEntities.RUBBER_DUCK.get(), RubberDuckEntity.createAttributes().build());
    }
}
