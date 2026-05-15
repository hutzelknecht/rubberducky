# Rubber Duck Creature — Implementation Plan

## Attribution
Model and texture assets are sourced from **[salvadormg15/Rubber-Duck](https://github.com/salvadormg15/Rubber-Duck)** (MIT License).
Original design by [@salvadormg15](https://github.com/salvadormg15). Used with permission per [issue #11](https://github.com/salvadormg15/Rubber-Duck/issues/11).
A courtesy credit comment must appear wherever these assets are referenced in code and resources.

## Summary
Add a rideable rubber duck mob to the mod. It floats on still water surfaces naturally, can be mounted, and when mounted it flies (WASD horizontal, Jump/Sneak vertical) at twice the speed of a ghast. It makes a squeaky toy sound on interaction. A custom spawn egg is also provided.

The rubber duck visual is based on salvadormg15's original model, which consists of six parts: **body** (6×4×6), **head** (4×4×4), **beak** (3.2×1×1.6), **left wing**, **right wing**, and **tail** — all textured with `rubber_duck.png` (MIT-licensed, sourced from the original repo).

## Design Decisions
- **Base class:** `Animal` — supports riding, passive AI, standard mob lifecycle
- **Flying speed:** ~2× ghast attribute speed (~0.5 → 1.0 blocks/tick, tuneable)
- **Water floating:** Custom `aiStep()` / `travel()` applying upward force when standing on water surface
- **Riding:** Override `travel()` to consume player steering input (like `Pig` / `Strider` but 3D)
- **Dismount mid-air:** Standard dismount — player takes fall damage normally (no special handling)
- **Sound:** Custom sound event pointing to a squeaky OGG file (placeholder path; file to be provided)
- **Model/Texture:** Use salvadormg15's MIT-licensed model geometry and `rubber_duck.png` texture (fetched from [salvadormg15/Rubber-Duck](https://github.com/salvadormg15/Rubber-Duck/blob/main/src/main/resources/assets/rubber_duck/textures/block/rubber_duck.png))
- **Natural spawn:** Biome modifier — surface water spawn in all biomes with lakes (tag `#has_structure/village` exclusion not needed; just use the `is_overworld` biome tag + spawn light/water conditions)

---

## Phases & Todos

### Phase 1 — Registry Infrastructure
1. Create `ModEntities.java` — `DeferredRegister<EntityType<?>>`, register `RUBBER_DUCK` EntityType
2. Create `ModItems.java` — `DeferredRegister.Items` for mod-specific items, register `RUBBER_DUCK_SPAWN_EGG` (`SpawnEggItem`)
3. Create `ModSounds.java` — `DeferredRegister<SoundEvent>`, register `RUBBER_DUCK_SQUEAK`
4. Update `RubberDucky.java` — wire all three new registries into the constructor, clean up boilerplate example entries, add spawn egg + duck to a creative tab

### Phase 2 — Entity Logic
5. Create `RubberDuckEntity.java` (package `entity`)
   - Extends `Animal`
   - Static `createAttributes()` — movement speed 0.2, flying speed 1.0
   - Goals: `FloatGoal`, `RandomLookAroundGoal`, custom `WaterSurfaceWanderGoal` (wander on water only)
   - `travel(Vec3 input)`: when a rider is present, read `player.xxa` / `player.zza` + jump/sneak keys → apply 3D velocity; when no rider, standard water-float behavior
   - `aiStep()`: buoyancy — if entity is touching water surface (not submerged), push upward slightly to keep it floating
   - `canBeControlledByRider()` → true
   - Sound overrides: ambient/hurt/death all return `RUBBER_DUCK_SQUEAK`
   - `getAddEntityPacket()` for client sync

### Phase 3 — Client Side
6. Create `RubberDuckModel.java` (package `client.model`)
   - Extends `EntityModel<RubberDuckEntity>`
   - Port salvadormg15's 6-part geometry (body, head, beak, left wing, right wing, tail) from the block model JSON to `ModelPart` definitions
   - Add credit comment: `// Model geometry ported from salvadormg15/Rubber-Duck (MIT) https://github.com/salvadormg15/Rubber-Duck`
   - `setupAnim()` — subtle bob/rock animation when on water
7. Create `RubberDuckRenderer.java` (package `client.renderer`)
   - Extends `MobRenderer<RubberDuckEntity, RubberDuckModel>`
   - Reference placeholder texture path `rubberducky:textures/entity/rubber_duck.png`
8. Update `RubberDuckyClient.java` — register renderer via `EntityRenderers.register()`

### Phase 4 — Data & Resources
9. `sounds.json` — declare `rubberducky:rubber_duck.squeak` sound event (placeholder OGG path)
10. Download and place `rubber_duck.png` from salvadormg15's repo at `assets/rubberducky/textures/entity/rubber_duck.png`
    - Add `CREDITS.md` or in-code comment crediting salvadormg15 (MIT License)
11. Biome modifier JSON `data/rubberducky/neoforge/biome_modifier/rubber_duck_spawns.json`
    - Type: `neoforge:add_spawns`
    - Biomes: `#minecraft:is_overworld`
    - SpawnData: category `creature`, weight 4, minCount 1, maxCount 2
    - Spawn placement: `ON_GROUND` with water fluid restriction (surface of water — use `Heightmap.Types.MOTION_BLOCKING` + `Type.ON_GROUND` which NeoForge biome modifier spawners support, or handle via `checkSpawnRules` in entity)
12. Entity spawn rule override in `RubberDuckEntity.checkSpawnRules()` — only allow spawning if block below is water
13. Update `en_us.json` — add entity name, spawn egg name, sound subtitle

---

## File Tree (new files)
```
src/main/java/de/immerdieses/rubberducky/
  entity/
    RubberDuckEntity.java
  client/
    model/RubberDuckModel.java
    renderer/RubberDuckRenderer.java
  registry/
    ModEntities.java
    ModItems.java
    ModSounds.java

src/main/resources/
  assets/rubberducky/
    sounds.json
    textures/entity/rubber_duck.png  (placeholder)
    lang/en_us.json  (updated)
  data/rubberducky/
    neoforge/biome_modifier/rubber_duck_spawns.json
```

## Notes
- The existing `EXAMPLE_BLOCK`, `EXAMPLE_ITEM`, `EXAMPLE_TAB` boilerplate in `RubberDucky.java` should be removed/replaced during Phase 1 cleanup (confirm with user if desired)
- Squeaky OGG sound file to be provided; placeholder `sounds.json` entry added so the game doesn't crash on missing resource
- **Asset credit:** All visual assets (model geometry + texture) are ported from [salvadormg15/Rubber-Duck](https://github.com/salvadormg15/Rubber-Duck), MIT License. Original design by @salvadormg15. Must be credited in `CREDITS.md` and in relevant source file headers.
