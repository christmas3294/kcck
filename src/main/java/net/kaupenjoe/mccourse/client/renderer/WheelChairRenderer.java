package net.kaupenjoe.mccourse.client.renderer;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import net.kaupenjoe.mccourse.client.model.WheelChairModel;
import net.kaupenjoe.mccourse.entity.custom.WheelChairEntity;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import software.bernie.geckolib.cache.object.BakedGeoModel;
import software.bernie.geckolib.cache.object.GeoBone;
import software.bernie.geckolib.renderer.GeoEntityRenderer;

public class WheelChairRenderer extends GeoEntityRenderer<WheelChairEntity> {
    public WheelChairRenderer(EntityRendererProvider.Context ctx) {
        super(ctx, new WheelChairModel());
        this.shadowRadius = 0.6f;
    }

    @Override
    public RenderType getRenderType(WheelChairEntity animatable, ResourceLocation texture, MultiBufferSource bufferSource, float partialTick) {
        return RenderType.entityCutout(getTextureLocation(animatable));
    }

    @Override
    public void render(WheelChairEntity entity, float entityYaw, float partialTick, PoseStack poseStack, MultiBufferSource bufferSource, int packedLight) {
        poseStack.pushPose();
        super.render(entity, entityYaw, partialTick, poseStack, bufferSource, packedLight);
        poseStack.popPose();
    }

    @Override
    public void preRender(PoseStack poseStack, WheelChairEntity entity, BakedGeoModel model, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        this.scaleHeight = 1.0f;
        this.scaleWidth = 1.0f;
        super.preRender(poseStack, entity, model, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
    }
public static int index = 0;
    @Override
    public void renderRecursively(PoseStack poseStack, WheelChairEntity animatable, GeoBone bone, RenderType renderType, MultiBufferSource bufferSource, VertexConsumer buffer, boolean isReRender, float partialTick, int packedLight, int packedOverlay, float red, float green, float blue, float alpha) {
        String name = bone.getName();
        index++;
        if (animatable.isMoving()){
            if (name.equals("w_rb")) {
                bone.setRotX(index);
            }
            if (name.equals("w_lb")) {
                bone.setRotX(index);
            }
            if (name.equals("w_rr")) {
                bone.setRotX(index);
            }
            if (name.equals("w_lr")) {
                bone.setRotX(index);
            }
        }

        if (name.equals("root")) {
            bone.setRotY(-animatable.getVisualYaw(partialTick));

        } else if (!animatable.isMoving()) {
            index = 0;
        }


        super.renderRecursively(poseStack, animatable, bone, renderType, bufferSource, buffer, isReRender, partialTick, packedLight, packedOverlay, red, green, blue, alpha);
    }
}
