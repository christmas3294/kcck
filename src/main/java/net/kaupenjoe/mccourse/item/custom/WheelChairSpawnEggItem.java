package net.kaupenjoe.mccourse.item.custom;

import net.kaupenjoe.mccourse.entity.ModEntities;
import net.kaupenjoe.mccourse.entity.custom.WheelChairEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

/**
 * Custom spawn egg for spawning the wheelchair entity from the spawn egg creative tab.
 */
public class WheelChairSpawnEggItem extends Item {
    public WheelChairSpawnEggItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        WheelChairEntity entity = ModEntities.WHEEL_CHAIR.get().create(level);
        if (entity == null) {
            return InteractionResult.FAIL;
        }

        BlockPos spawnPos = context.getClickedPos().relative(context.getClickedFace());
        double x = spawnPos.getX() + 0.5;
        double y = spawnPos.getY();
        double z = spawnPos.getZ() + 0.5;
        float yaw = context.getPlayer() != null ? context.getPlayer().getYRot() : 0.0f;
        entity.moveTo(x, y, z, yaw, 0.0f);
        level.addFreshEntity(entity);

        if (context.getPlayer() == null || !context.getPlayer().getAbilities().instabuild) {
            context.getItemInHand().shrink(1);
        }

        return InteractionResult.CONSUME;
    }
}
