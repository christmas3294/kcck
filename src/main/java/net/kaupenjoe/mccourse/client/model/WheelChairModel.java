package net.kaupenjoe.mccourse.client.model;

import net.kaupenjoe.mccourse.MCCourseMod;
import net.kaupenjoe.mccourse.entity.custom.WheelChairEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib.model.GeoModel;

public class WheelChairModel extends GeoModel<WheelChairEntity> {
    private static final ResourceLocation MODEL = new ResourceLocation(MCCourseMod.MOD_ID, "geo/wheel_chair.geo.json");
    private static final ResourceLocation TEXTURE = new ResourceLocation(MCCourseMod.MOD_ID, "textures/entity/wheel_chair.png");

    @Override
    public ResourceLocation getModelResource(WheelChairEntity animatable) {
        return MODEL;
    }

    @Override
    public ResourceLocation getTextureResource(WheelChairEntity animatable) {
        return TEXTURE;
    }

    @Override
    public ResourceLocation getAnimationResource(WheelChairEntity animatable) {
        return null;
    }
}
