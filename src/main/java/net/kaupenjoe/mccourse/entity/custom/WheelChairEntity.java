package net.kaupenjoe.mccourse.entity.custom;

import net.kaupenjoe.mccourse.entity.ModEntities;
import net.kaupenjoe.mccourse.entity.vehicle.MobileVehicleEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.network.NetworkHooks;
import software.bernie.geckolib.animatable.GeoEntity;
import software.bernie.geckolib.core.animatable.instance.AnimatableInstanceCache;
import software.bernie.geckolib.core.animation.AnimatableManager;
import software.bernie.geckolib.util.GeckoLibUtil;

public class WheelChairEntity extends MobileVehicleEntity implements GeoEntity {
    private static final float MAX_HEALTH = 50f;
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    private float leftWheelRot;
    private float rightWheelRot;
    private float prevLeftWheelRot;
    private float prevRightWheelRot;
    private float prevVisualYaw;
    private float visualYaw;
    private int engineSoundCooldown;

    public WheelChairEntity(EntityType<? extends WheelChairEntity> type, Level level) {
        super(type, level);
        setMaxUpStep(1.1f);
    }

    public WheelChairEntity(Level level) {
        this(ModEntities.WHEEL_CHAIR.get(), level);
    }

    @Override
    public void baseTick() {
        super.baseTick();
//        prevLeftWheelRot = leftWheelRot;
//        prevRightWheelRot = rightWheelRot;
//        if (Math.abs(getDeltaMovement().horizontalDistance()) > 0.05f) {
//            leftWheelRot -= 0.35f;
//            rightWheelRot -= 0.35f;
//        }
//        if (!level().isClientSide) {
//            crushEntities(getDeltaMovement());
//        }
        prevVisualYaw = visualYaw;
        visualYaw = getYRot() * Mth.DEG_TO_RAD;
    }
    @Override
    protected void travel() {
        Entity controllingPassenger = getControllingPassenger();
        LivingEntity rider = controllingPassenger instanceof LivingEntity ? (LivingEntity) controllingPassenger : null;
        if (rider == null) {
            resetInputs();
            setDeltaMovement(getDeltaMovement().multiply(0.8, 1.0, 0.8));
            return;
        }

        if (rider instanceof Player player) {
            clampYawToPassenger(player);
        } else {
            setYRot(rider.getYRot());
            setXRot(rider.getXRot() * 0.25f);
            setRot(getYRot(), getXRot());
        }
        float forward = rider.zza;
         float strafe = rider.xxa;
        forwardInputDown = forward > 0.01f;
        backInputDown = forward < -0.01f;
        leftInputDown = strafe > 0.01f;
        rightInputDown = strafe < -0.01f;

     //   upInputDown = rider.jumping;
        Vec3 motion = getDeltaMovement();
        Vec3 dampedMotion = new Vec3(motion.x * 0.8, motion.y, motion.z * 0.8);
        Vec3 addition = forwardVector().scale(forward * 0.2).add(rightVector().scale(strafe * 0.1));
//        if (upInputDown && onGround()) {
//            addition = addition.add(0, 0.4, 0);
//            playJumpSound();
//        }

        setDeltaMovement(dampedMotion.add(addition));
        playEngineSound();
    }

    private void playEngineSound() {
        if (level().isClientSide) return;
        if (engineSoundCooldown > 0) {
            engineSoundCooldown--;
            return;
        }
        if (getDeltaMovement().horizontalDistance() > 0.05f) {
         //   SoundEvent soundEvent = ModSounds.WHEEL_CHAIR_ENGINE.get();
           // level().playSound(null, blockPosition(), soundEvent, SoundSource.PLAYERS, 0.6f, 0.9f + random.nextFloat() * 0.2f);
            engineSoundCooldown = 20;
        }
    }

    private void playJumpSound() {
     //   level().playSound(null, blockPosition(), ModSounds.WHEEL_CHAIR_JUMP.get(), SoundSource.PLAYERS, 0.8f, 1.0f);
    }

    private void resetInputFlags() {
        leftInputDown = false;
        rightInputDown = false;
        forwardInputDown = false;
        backInputDown = false;
        upInputDown = false;
        downInputDown = false;
    }

    @Override
    public boolean hurt(net.minecraft.world.damagesource.DamageSource source, float amount) {
        level().playSound(null, blockPosition(), SoundEvents.ANVIL_HIT, SoundSource.PLAYERS, 0.6f, 1.2f);
        return super.hurt(source, amount);
    }

    @Override
    public float getMaxHealth() {
        return MAX_HEALTH;
    }

    @Override
    public Packet<ClientGamePacketListener> getAddEntityPacket() {
        return NetworkHooks.getEntitySpawningPacket(this);
    }

    public float getLeftWheelRot() {
        return prevLeftWheelRot;
    }

    public float getRightWheelRot() {
        return rightWheelRot;
    }

    @Override
    protected void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        leftWheelRot = tag.getFloat("LeftWheel");
        rightWheelRot = tag.getFloat("RightWheel");
    }

    @Override
    protected void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        tag.putFloat("LeftWheel", leftWheelRot);
        tag.putFloat("RightWheel", rightWheelRot);
    }

    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {
        return cache;
    }
    public float getVisualYaw(float partialTick) {
        return Mth.lerp(partialTick, prevVisualYaw, visualYaw);
    }

    public boolean isMoving() {
        return getDeltaMovement().horizontalDistanceSqr() > 1.0E-4;
    }
}
