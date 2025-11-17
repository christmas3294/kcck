package net.kaupenjoe.mccourse.entity;

import net.kaupenjoe.mccourse.MCCourseMod;
import net.kaupenjoe.mccourse.entity.custom.WheelChairEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, MCCourseMod.MOD_ID);

    public static final RegistryObject<EntityType<WheelChairEntity>> WHEEL_CHAIR = ENTITY_TYPES.register(
            "wheel_chair",
            () -> EntityType.Builder.<WheelChairEntity>of(WheelChairEntity::new, MobCategory.MISC)
                    .sized(1.0f, 1.0f)
                    .setTrackingRange(64)
                    .setUpdateInterval(2)
                    .build("wheel_chair"));

    public static void register(IEventBus eventBus) {
        ENTITY_TYPES.register(eventBus);
    }
}
