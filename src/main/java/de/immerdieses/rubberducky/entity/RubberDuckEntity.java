package de.immerdieses.rubberducky.entity;

import de.immerdieses.rubberducky.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.RandomLookAroundGoal;
import net.minecraft.world.entity.ai.goal.WaterAvoidingRandomStrollGoal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;

public class RubberDuckEntity extends Animal {

    // Twice the speed of a ghast's flying speed attribute (0.5 → 1.0)
    private static final double FLYING_SPEED = 1.0;
    private static final double WATER_FLOAT_FORCE = 0.08;

    public RubberDuckEntity(EntityType<? extends Animal> type, Level level) {
        super(type, level);
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 10.0)
                .add(Attributes.MOVEMENT_SPEED, 0.2)
                .add(Attributes.FLYING_SPEED, FLYING_SPEED);
    }

    @Override
    protected void registerGoals() {
        goalSelector.addGoal(0, new FloatGoal(this));
        goalSelector.addGoal(1, new WaterAvoidingRandomStrollGoal(this, 0.6));
        goalSelector.addGoal(2, new RandomLookAroundGoal(this));
    }

    // -------------------------------------------------------------------------
    // Riding / flying
    // -------------------------------------------------------------------------

    @Override
    public LivingEntity getControllingPassenger() {
        if (getFirstPassenger() instanceof Player player) return player;
        return null;
    }

    @Override
    public void travel(Vec3 travelVector) {
        if (isVehicle() && getControllingPassenger() instanceof Player rider) {

            // Yaw follows rider's look
            setYRot(rider.getYRot());
            yRotO = getYRot();
            setXRot(rider.getXRot() * 0.5f);
            setRot(getYRot(), getXRot());
            yBodyRot = getYRot();
            yHeadRot = getYRot();

            float forward = rider.zza;
            float strafe = rider.xxa;

            // Vertical: jump flag is set on the duck entity by the ride packet; sneak = descend
            double verticalInput = 0.0;
            if (this.jumping) {
                verticalInput = FLYING_SPEED * 0.5;
            } else if (rider.isShiftKeyDown()) {
                verticalInput = -FLYING_SPEED * 0.5;
            }

            if (forward <= 0f) forward *= 0.5f;

            // Rotate input by yaw
            double yawRad = Math.toRadians(getYRot());
            double dx = strafe * FLYING_SPEED * 0.5 * Math.cos(yawRad) + forward * FLYING_SPEED * Math.sin(yawRad);
            double dz = forward * FLYING_SPEED * Math.cos(yawRad) - strafe * FLYING_SPEED * 0.5 * Math.sin(yawRad);

            setDeltaMovement(dx, verticalInput, dz);
            move(MoverType.SELF, getDeltaMovement());
            setDeltaMovement(getDeltaMovement().scale(0.9));
            return;
        }

        super.travel(travelVector);
    }

    // -------------------------------------------------------------------------
    // Water floating when idle
    // -------------------------------------------------------------------------

    @Override
    public void aiStep() {
        super.aiStep();

        if (!isVehicle() && isEffectiveAi()) {
            BlockPos below = blockPosition().below();
            boolean onWaterSurface = level().getFluidState(below).is(Fluids.WATER)
                    && !level().getFluidState(blockPosition()).is(Fluids.WATER);

            if (isInWater() || onWaterSurface) {
                Vec3 current = getDeltaMovement();
                setDeltaMovement(current.x, Math.min(current.y + WATER_FLOAT_FORCE, 0.1), current.z);
            }
        }
    }

    // -------------------------------------------------------------------------
    // Spawn rules — only on water surface
    // -------------------------------------------------------------------------

    public static boolean checkSpawnRules(EntityType<RubberDuckEntity> type,
                                          net.minecraft.world.level.ServerLevelAccessor level,
                                          net.minecraft.world.entity.EntitySpawnReason reason,
                                          BlockPos pos,
                                          net.minecraft.util.RandomSource random) {
        return level.getFluidState(pos.below()).is(Fluids.WATER);
    }

    // -------------------------------------------------------------------------
    // Sounds — all map to squeak (credit: salvadormg15/Rubber-Duck, MIT)
    // -------------------------------------------------------------------------

    @Override
    protected SoundEvent getAmbientSound() {
        return ModSounds.RUBBER_DUCK_SQUEAK.get();
    }

    @Override
    protected SoundEvent getHurtSound(net.minecraft.world.damagesource.DamageSource source) {
        return ModSounds.RUBBER_DUCK_SQUEAK.get();
    }

    @Override
    protected SoundEvent getDeathSound() {
        return ModSounds.RUBBER_DUCK_SQUEAK.get();
    }

    @Override
    protected float getSoundVolume() {
        return 0.4f;
    }

    // -------------------------------------------------------------------------
    // Misc
    // -------------------------------------------------------------------------

    @Override
    public net.minecraft.world.entity.AgeableMob getBreedOffspring(
            net.minecraft.server.level.ServerLevel level, net.minecraft.world.entity.AgeableMob other) {
        return null;
    }

    @Override
    public boolean isFood(ItemStack stack) {
        return false;
    }

    @Override
    protected float getWaterSlowDown() {
        return 0.98f;
    }
}

