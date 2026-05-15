package de.immerdieses.rubberducky.registry;

import de.immerdieses.rubberducky.RubberDucky;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.SpawnEggItem;
import net.neoforged.neoforge.registries.DeferredItem;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModItems {

    public static final DeferredRegister.Items ITEMS =
            DeferredRegister.createItems(RubberDucky.MODID);

    // registerItem() properly injects Item.Properties with ID set.
    // getType() is overridden to defer entity type lookup until after registration.
    public static final DeferredItem<SpawnEggItem> RUBBER_DUCK_SPAWN_EGG =
            ITEMS.registerItem("rubber_duck_spawn_egg", props -> new SpawnEggItem(props) {
                @Override
                public EntityType<?> getType(ItemStack stack) {
                    return ModEntities.RUBBER_DUCK.get();
                }
            });
}
