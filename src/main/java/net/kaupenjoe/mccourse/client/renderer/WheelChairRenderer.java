package net.kaupenjoe.mccourse.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import net.kaupenjoe.mccourse.MCCourseMod;
import net.kaupenjoe.mccourse.entity.custom.WheelChairEntity;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;

/**
 * Simple no-op renderer so the wheelchair entity can exist without GeckoLib.
 */
public class WheelChairRenderer extends EntityRenderer<WheelChairEntity> {
    private static final ResourceLocation TEXTURE = new ResourceLocation(MCCourseMod.MOD_ID, "textures/entity/wheel_chair.png");

    public WheelChairRenderer(EntityRendererProvider.Context context) {
        super(context);
        this.shadowRadius = 0.6f;
    }

    @Override
    public void render(WheelChairEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource buffer, int packedLight) {
        // Intentionally left blank. Without GeckoLib models we do not render geometry, but keeping
        // an empty renderer avoids client crashes when the entity is present.
        super.render(entity, entityYaw, partialTick, poseStack, buffer, packedLight);
    }

    @Override
    public ResourceLocation getTextureLocation(WheelChairEntity entity) {
        return TEXTURE;
    }
}
