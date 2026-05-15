package de.immerdieses.rubberducky.registry;

import de.immerdieses.rubberducky.RubberDucky;
import de.immerdieses.rubberducky.entity.RubberDuckEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModEntities {

    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, RubberDucky.MODID);

    public static final DeferredHolder<EntityType<?>, EntityType<RubberDuckEntity>> RUBBER_DUCK =
            ENTITY_TYPES.register("rubber_duck", key -> EntityType.Builder
                    .<RubberDuckEntity>of(RubberDuckEntity::new, MobCategory.CREATURE)
                    .sized(0.9f, 0.9f)
                    .build(ResourceKey.create(Registries.ENTITY_TYPE, key)));
}
