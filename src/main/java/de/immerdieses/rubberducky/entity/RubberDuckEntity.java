package de.immerdieses.rubberducky.entity;

import de.immerdieses.rubberducky.registry.ModSounds;
import net.minecraft.core.BlockPos;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
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
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

public class RubberDuckEntity extends Animal {

    // Twice the speed of a ghast's flying speed attribute (0.5 → 1.0)
    private static final double FLYING_SPEED = 1.0;
    private static final double WATER_FLOAT_FORCE = 0.08;

    /** Set by EntityMountEvent when a mid-air dismount is cancelled; consumed in travel(). */
    public boolean riderWantsDescend = false;
    /** Last controlling rider; used as a fallback to re-seat them if dismount fires mid-air. */
    @Nullable private Player lastRider = null;

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

    /** Right-click to mount; shift-right-click passes through to super (no action). */
    @Override
    public InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!isVehicle() && !player.isShiftKeyDown()) {
            if (!level().isClientSide()) {
                player.startRiding(this);
            }
            return level().isClientSide() ? InteractionResult.SUCCESS : InteractionResult.SUCCESS_SERVER;
        }
        return super.mobInteract(player, hand);
    }

    @Override
    public LivingEntity getControllingPassenger() {
        if (getFirstPassenger() instanceof Player player) return player;
        return null;
    }

    @Override
    public void travel(Vec3 travelVector) {
        if (isVehicle() && getControllingPassenger() instanceof Player rider) {

            // Snap duck rotation to rider's look direction immediately (no interpolation lag)
            float yaw = rider.getYRot();
            setYRot(yaw);
            yRotO      = yaw;
            yBodyRot   = yaw;
            yBodyRotO  = yaw;
            yHeadRot   = yaw;
            setXRot(rider.getXRot() * 0.5f);

            float forward = rider.zza;
            float strafe  = rider.xxa;
            if (forward < 0f) forward *= 0.5f; // slower in reverse

            // Correct MC movement: forward direction = (-sinYaw, cosYaw)
            // Matches LivingEntity.getInputVector() / moveRelative() convention
            float yawRad = yaw * (float)(Math.PI / 180.0);
            float sinYaw = (float)Math.sin(yawRad);
            float cosYaw = (float)Math.cos(yawRad);
            double dx = (strafe * cosYaw - forward * sinYaw) * FLYING_SPEED;
            double dz = (forward * cosYaw + strafe * sinYaw) * FLYING_SPEED;

            // Vertical: Space = ascend, Shift = descend
            // riderWantsDescend is set by the EntityMountEvent handler each tick Shift is held
            // (in 1.21.x, rider.isShiftKeyDown() is not reliable during riding — the Shift key
            // triggers the dismount mechanism, not the shiftKeyDown flag on the server)
            double dy = 0.0;
            if (rider.jumping) {
                dy = FLYING_SPEED * 0.5;
            } else if (riderWantsDescend) {
                dy = -FLYING_SPEED * 0.5;
            }
            riderWantsDescend = false; // consumed

            setDeltaMovement(dx, dy, dz);
            move(MoverType.SELF, getDeltaMovement());
            setDeltaMovement(getDeltaMovement().scale(0.9));
            return;
        }
        super.travel(travelVector);
    }

    /**
     * Fallback dismount guard: if EntityMountEvent didn't fire/cancel, re-seat the rider
     * when the duck is still airborne and not on water.
     */
    @Override
    public void tick() {
        super.tick();
        if (!level().isClientSide()) {
            Entity passenger = getFirstPassenger();
            if (passenger instanceof Player player) {
                lastRider = player;
            } else if (passenger == null && lastRider != null && lastRider.isAlive() && !isSafeToDisMount()) {
                // Rider escaped mid-air — put them back (force=true bypasses entity-mount event)
                if (!lastRider.isPassenger()) {
                    lastRider.startRiding(this, true, true);
                }
            } else if (passenger == null) {
                lastRider = null;
            }
        }
    }

    /** True when the duck is on solid ground or floating on/in water — safe to dismount. */
    public boolean isSafeToDisMount() {
        return onGround() || isInWater();
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

