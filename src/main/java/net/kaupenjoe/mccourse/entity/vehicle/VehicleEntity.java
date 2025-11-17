package net.kaupenjoe.mccourse.entity.vehicle;

import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.DismountHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;

/**
 * 精简的载具基础类，用于承载 WheelChairEntity 的公共逻辑。
 */
public abstract class VehicleEntity extends Entity {
    private static final EntityDataAccessor<Float> HEALTH = SynchedEntityData.defineId(VehicleEntity.class, EntityDataSerializers.FLOAT);
    private static final EntityDataAccessor<String> LAST_ATTACKER = SynchedEntityData.defineId(VehicleEntity.class, EntityDataSerializers.STRING);

    protected int interpolationSteps;
    protected double clientX;
    protected double clientY;
    protected double clientZ;
    protected double clientYaw;
    protected double clientPitch;
    protected float roll;
    protected float prevRoll;

    protected VehicleEntity(EntityType<?> type, Level level) {
        super(type, level);
        setHealth(getMaxHealth());
    }

    @Override
    protected void defineSynchedData() {
        this.entityData.define(HEALTH, getMaxHealth());
        this.entityData.define(LAST_ATTACKER, "");
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        setHealth(tag.getFloat("VehicleHealth"));
        this.entityData.set(LAST_ATTACKER, tag.getString("LastAttacker"));
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        tag.putFloat("VehicleHealth", getHealth());
        tag.putString("LastAttacker", this.entityData.get(LAST_ATTACKER));
    }

    @Override
    public InteractionResult interact(Player player, InteractionHand hand) {
        if (player.isSecondaryUseActive()) {
            return InteractionResult.PASS;
        }
        if (!level().isClientSide) {
            player.startRiding(this);
        }
        return InteractionResult.sidedSuccess(level().isClientSide);
    }

    @Override
    public boolean isPickable() {
        return true;
    }

    @Override
    public boolean canBeCollidedWith() {
        return true;
    }

    @Override
    public LivingEntity getControllingPassenger() {
        Entity firstPassenger = getFirstPassenger();
        return firstPassenger instanceof LivingEntity living ? living : null;
    }

    @Override
    public boolean hurt(DamageSource source, float amount) {
        if (level().isClientSide) {
            return true;
        }
        if (source.getEntity() != null) {
            entityData.set(LAST_ATTACKER, source.getEntity().getStringUUID());
        }
        setHealth(getHealth() - amount);
        if (getHealth() <= 0 && level() instanceof ServerLevel serverLevel) {
            serverLevel.playSound(null, blockPosition(), SoundEvents.ANVIL_BREAK, SoundSource.PLAYERS, 1.0f, 0.8f);
            destroy();
        }
        return true;
    }

    protected void destroy() {
        discard();
    }

    @Override
    public void baseTick() {
        super.baseTick();
        prevRoll = roll;
        roll *= 0.9f;
        handleClientSync();
        if (!level().isClientSide && getHealth() < getMaxHealth() && tickCount % 40 == 0) {
            heal(1.0f);
        }
        travel();
        refreshDimensions();
    }

    protected void travel() {
    }

    protected void heal(float amount) {
        setHealth(getHealth() + amount);
    }

    public float getHealth() {
        return entityData.get(HEALTH);
    }

    public void setHealth(float value) {
        entityData.set(HEALTH, Mth.clamp(value, 0, getMaxHealth()));
    }

    public float getMaxHealth() {
        return 50.0f;
    }

    public float getRoll(float partial) {
        return Mth.lerp(partial, prevRoll, roll);
    }

    protected void handleClientSync() {
        if (isControlledByLocalInstance()) {
            interpolationSteps = 0;
            syncPacketPositionCodec(getX(), getY(), getZ());
        }
        if (interpolationSteps <= 0) {
            return;
        }
        double x = getX() + (clientX - getX()) / interpolationSteps;
        double y = getY() + (clientY - getY()) / interpolationSteps;
        double z = getZ() + (clientZ - getZ()) / interpolationSteps;
        float yaw = (float) (Mth.wrapDegrees(clientYaw - getYRot()) / interpolationSteps);
        float pitch = (float) ((clientPitch - getXRot()) / interpolationSteps);
        setPos(x, y, z);
        setRot(getYRot() + yaw, getXRot() + pitch);
        --interpolationSteps;
    }

    @Override
    public void lerpTo(double x, double y, double z, float yaw, float pitch, int steps, boolean interpolate) {
        clientX = x;
        clientY = y;
        clientZ = z;
        clientYaw = yaw;
        clientPitch = pitch;
        interpolationSteps = 10;
    }

    @Override
    public Vec3 getDismountLocationForPassenger(LivingEntity passenger) {
        Vec3 offset = getDismountOffset(getBbWidth(), passenger.getBbWidth());
        double ox = getX() - offset.x;
        double oz = getZ() + offset.z;
        BlockPos pos = BlockPos.containing(ox, getY(), oz);
        BlockPos below = pos.below();
        ArrayList<Vec3> options = new ArrayList<>();
        double exitHeight = level().getBlockFloorHeight(pos);
        if (DismountHelper.isBlockFloorValid(exitHeight)) {
            options.add(new Vec3(ox, pos.getY() + exitHeight, oz));
        }
        double floorHeight = level().getBlockFloorHeight(below);
        if (DismountHelper.isBlockFloorValid(floorHeight)) {
            options.add(new Vec3(ox, below.getY() + floorHeight, oz));
        }
        for (Pose pose : passenger.getDismountPoses()) {
            for (Vec3 candidate : options) {
                if (DismountHelper.canDismountTo(level(), candidate, passenger, pose)) {
                    passenger.setPose(pose);
                    return candidate;
                }
            }
        }
        return super.getDismountLocationForPassenger(passenger);
    }

    protected Vec3 getDismountOffset(double vehicleWidth, double passengerWidth) {
        double offset = (vehicleWidth + passengerWidth + 1.0E-5f) / 2.0;
        float yaw = getYRot() + 90.0f;
        float x = -Mth.sin(yaw * ((float) Math.PI / 180));
        float z = Mth.cos(yaw * ((float) Math.PI / 180));
        float max = Math.max(Math.abs(x), Math.abs(z));
        return new Vec3(x * offset / max, 0.0, z * offset / max);
    }

    @Override
    protected boolean canAddPassenger(Entity entity) {
        return getPassengers().size() < getMaxPassengers();
    }

    protected int getMaxPassengers() {
        return 1;
    }
}
