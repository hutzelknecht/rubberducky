package de.immerdieses.rubberducky.registry;

import de.immerdieses.rubberducky.RubberDucky;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModSounds {

    public static final DeferredRegister<SoundEvent> SOUND_EVENTS =
            DeferredRegister.create(Registries.SOUND_EVENT, RubberDucky.MODID);

    public static final DeferredHolder<SoundEvent, SoundEvent> RUBBER_DUCK_SQUEAK =
            SOUND_EVENTS.register("rubber_duck.squeak",
                    () -> SoundEvent.createFixedRangeEvent(
                            ResourceLocation.fromNamespaceAndPath(RubberDucky.MODID, "rubber_duck.squeak"), 16f));
}
