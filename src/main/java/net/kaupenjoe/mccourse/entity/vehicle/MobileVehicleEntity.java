package net.kaupenjoe.mccourse.entity.vehicle;

import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * 缁ф壙鑷?VehicleEntity 鐨勭畝鍖栫Щ鍔ㄥ疄鐜般€? */
public abstract class MobileVehicleEntity extends VehicleEntity {
    protected boolean leftInputDown;
    protected boolean rightInputDown;
    protected boolean forwardInputDown;
    protected boolean backInputDown;
    protected boolean upInputDown;
    protected boolean downInputDown;
    protected double lastTickSpeed;
    protected int collisionCooldown;

    protected MobileVehicleEntity(EntityType<?> type, Level level) {
        super(type, level);
    }

    @Override
    public void baseTick() {
        lastTickSpeed = getDeltaMovement().length();
        if (collisionCooldown > 0) {
            collisionCooldown--;
        }
        super.baseTick();
        setDeltaMovement(getDeltaMovement().add(0, -0.05, 0));
        move(MoverType.SELF, getDeltaMovement());
    }

    @Override
    public void move(MoverType type, Vec3 movement) {
        super.move(type, movement);
        if (collisionCooldown > 0) {
            return;
        }
        boolean collided = horizontalCollision || verticalCollision;
        if (!collided) {
            return;
        }
        if (movement.lengthSqr() > 0.01) {
            onVehicleCollision();
        }
    }

    protected void onVehicleCollision() {
        if (level().isClientSide) return;
        collisionCooldown = 10;
        level().playSound(null, blockPosition(), SoundEvents.ANVIL_PLACE, SoundSource.PLAYERS, 0.6f, 0.7f);
        hurt(level().damageSources().generic(), (float) Math.max(1.0, lastTickSpeed * 2));
    }



    public void resetInputs() {
        leftInputDown = rightInputDown = forwardInputDown = backInputDown = upInputDown = downInputDown = false;
    }

    protected Vec3 forwardVector() {
        float yaw = getYRot() * ((float) Math.PI / 180F);
        //：yaw 只是角度=>标量
        // 驱动车辆的移动需要把标量 转换成方向向量
        return new Vec3(-Mth.sin(yaw), 0.0, Mth.cos(yaw));
    }

    protected Vec3 rightVector() {
        Vec3 forward = forwardVector();
        // forwardVector() 已经给你一个单位向量 (fx, 0, fz) 指向实体面朝方向。想得到“右侧”的方向，本质就是把这个向量在水平面上旋
        //  转 90°（顺时针），这样才能和左/右输入配合。对于 2D 向量 (x, z)，顺时针旋转 90°的结果是 (z, -x)；逆时针则是 (-z, x)。代
        //  码里的
        //正是把 (fx, fz) 转成 (fz, -fx)，也就是“右手方向”。这样 forwardVector() 和 rightVector() 始终保持正交、长度相同。随后在
        //  travel() 里就能写：
        return new Vec3(forward.z, 0.0, -forward.x);
    }

    protected void clampYawToPassenger(Player passenger) {
        setYRot(passenger.getYHeadRot());
        setXRot(passenger.getXRot() * 0.25f);
        setRot(getYRot(), getXRot());
        passenger.setYBodyRot(getYRot());
    }
}

