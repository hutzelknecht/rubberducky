package de.immerdieses.rubberducky.registry;

import de.immerdieses.rubberducky.RubberDucky;
import net.minecraft.core.component.DataComponents;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.minecraft.world.item.component.TypedEntityData;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {

    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(RubberDucky.MODID);

    // SpawnEggItem in 1.21.x uses DataComponents.ENTITY_DATA to store the entity type
    public static final DeferredHolder<Item, SpawnEggItem> RUBBER_DUCK_SPAWN_EGG =
            ITEMS.register("rubber_duck_spawn_egg", () -> new SpawnEggItem(
                    new Item.Properties().component(
                            DataComponents.ENTITY_DATA,
                            TypedEntityData.of(ModEntities.RUBBER_DUCK.get(), new CompoundTag()))));
}
