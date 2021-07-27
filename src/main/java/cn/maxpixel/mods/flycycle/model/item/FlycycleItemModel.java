package cn.maxpixel.mods.flycycle.model.item;

import cn.maxpixel.mods.flycycle.Flycycle;
import cn.maxpixel.mods.flycycle.util.ModelRendererUtil;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.IEntityRenderer;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.entity.model.PlayerModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Quaternion;
import net.minecraft.util.math.vector.TransformationMatrix;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.animation.Animation;

@OnlyIn(Dist.CLIENT)
public class FlycycleItemModel<T extends AbstractClientPlayerEntity> extends EntityModel<T> {
    public static final ResourceLocation MODEL = new ResourceLocation(Flycycle.MODID, "textures/entity/flycycle.png");

    private float timeWhenStateChanged;
    private float time;

    private byte state = STOPPED_STATE;
    public static final byte STOPPED_STATE = 1;
    public static final byte STARTING_STATE = 2;
    public static final byte STARTED_STATE = 4;
    public static final byte STOPPING_STATE = 8;

    private final ModelRenderer engine;
    private final ModelRenderer animation;
    private final ModelRenderer stick_straight;
    private final ModelRenderer stick_left;
    private final ModelRenderer stick_right;
    private final ModelRenderer prop_left;
    private final ModelRenderer prop_right;

    public float partialTicks;

    public FlycycleItemModel() {
        texWidth = 64;
        texHeight = 64;

        engine = new ModelRenderer(this);
        engine.texOffs(0, 0).addBox(-2.5F, 11.0F, 2.0F, 5.0F, 1.0F, 5.0F, 0.0F, false);
        engine.texOffs(20, 6).addBox(-1.5F, -2.0F, 2.0F, 4.0F, 13.0F, 1.0F, 0.0F, false);
        engine.texOffs(10, 6).addBox(1.5F, -2.0F, 3.0F, 1.0F, 13.0F, 4.0F, 0.0F, false);
        engine.texOffs(30, 6).addBox(-2.5F, -2.0F, 2.0F, 1.0F, 13.0F, 4.0F, 0.0F, false);
        engine.texOffs(0, 6).addBox(-2.5F, -2.0F, 6.0F, 4.0F, 13.0F, 1.0F, 0.0F, false);
        engine.texOffs(0, 0).addBox(-0.5F, -9.0F, 4.0F, 1.0F, 20.0F, 1.0F, 0.0F, false);

        animation = new ModelRenderer(this);

        stick_straight = new ModelRenderer(this);
        animation.addChild(stick_straight);
        stick_straight.texOffs(0, 0).addBox(-0.5F, -19.0F, 4.0F, 1.0F, 10.0F, 1.0F, 0.0F, false);

        stick_left = new ModelRenderer(this);
        animation.addChild(stick_left);
        stick_left.texOffs(0, 0).addBox(0.5F, -0.5F, 4.0F, 15.0F, 1.0F, 1.0F, 0.0F, false);

        stick_right = new ModelRenderer(this);
        animation.addChild(stick_right);
        stick_right.texOffs(0, 0).addBox(-15.5F, -0.5F, 4.0F, 15.0F, 1.0F, 1.0F, 0.0F, false);

        prop_left = new ModelRenderer(this);
        prop_left.setPos(15.0F, 0.0F, 4.5F);
        animation.addChild(prop_left);
        prop_left.texOffs(0, 0).addBox(-0.5F, -21.0F, -0.5F, 1.0F, 2.0F, 1.0F, 0.0F, false);
        prop_left.texOffs(0, 0).addBox(0.5F, -21.0F, -0.5F, 6.0F, 1.0F, 1.0F, 0.0F, false);
        prop_left.texOffs(0, 0).addBox(-0.5F, -21.0F, -6.5F, 1.0F, 1.0F, 6.0F, 0.0F, false);
        prop_left.texOffs(0, 0).addBox(-6.5F, -21.0F, -0.5F, 6.0F, 1.0F, 1.0F, 0.0F, false);
        prop_left.texOffs(0, 0).addBox(-0.5F, -21.0F, 0.5F, 1.0F, 1.0F, 6.0F, 0.0F, false);

        prop_right = new ModelRenderer(this);
        prop_right.setPos(-15.0F, 0.0F, 4.5F);
        animation.addChild(prop_right);
        prop_right.texOffs(0, 0).addBox(-0.5F, -21.0F, -0.5F, 1.0F, 2.0F, 1.0F, 0.0F, false);
        prop_right.texOffs(0, 0).addBox(0.5F, -21.0F, -0.5F, 6.0F, 1.0F, 1.0F, 0.0F, false);
        prop_right.texOffs(0, 0).addBox(-0.5F, -21.0F, -6.5F, 1.0F, 1.0F, 6.0F, 0.0F, false);
        prop_right.texOffs(0, 0).addBox(-6.5F, -21.0F, -0.5F, 6.0F, 1.0F, 1.0F, 0.0F, false);
        prop_right.texOffs(0, 0).addBox(-0.5F, -21.0F, 0.5F, 1.0F, 1.0F, 6.0F, 0.0F, false);
    }

    public void startingState() {
        if(state != STOPPED_STATE) return;
        state = STARTING_STATE;
        timeWhenStateChanged = Animation.getWorldTime(Minecraft.getInstance().level, partialTicks);
    }

    private void startedState() {
        if(state != STARTING_STATE) throw new IllegalStateException();
        state = STARTED_STATE;
        timeWhenStateChanged = Animation.getWorldTime(Minecraft.getInstance().level, partialTicks);
    }

    public void stoppingState() {
        if(state != STARTED_STATE) return;
        state = STOPPING_STATE;
        timeWhenStateChanged = Animation.getWorldTime(Minecraft.getInstance().level, partialTicks);
    }

    private void stoppedState() {
        if(state != STOPPING_STATE) throw new IllegalStateException();
        state = STOPPED_STATE;
        timeWhenStateChanged = Animation.getWorldTime(Minecraft.getInstance().level, partialTicks);
    }

    public byte getState() {
        return state;
    }

    @Override
    public void setupAnim(T player, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        EntityRenderer<? super T> renderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(player);
        engine.copyFrom(((IEntityRenderer<T, PlayerModel<T>>) renderer).getModel().body);
        animation.copyFrom(engine);

        time = Animation.getWorldTime(Minecraft.getInstance().level, partialTicks) - timeWhenStateChanged;
    }

    private static final float minY = 10.f / 16.f;

    @Override
    public void renderToBuffer(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
        engine.render(matrixStack, buffer, packedLight, packedOverlay);

        switch(state) {
            case STARTING_STATE:
                animation.visible = true;
                if(time > .25f) startedState();
                if(time <= .15f) {
                    stick_straight.visible = true;
                    animationRender(matrixStack, buffer, packedLight, packedOverlay, () -> {
                        ModelRendererUtil.render(stick_straight, matrixStack, buffer, packedLight, packedOverlay, new TransformationMatrix(
                                new Vector3f(0.f, MathHelper.lerp(time / .15f, minY, 0.f), 0.f),
                                Quaternion.ONE, ONE, Quaternion.ONE
                        ));
                    });
                } else {
                    animationRender(matrixStack, buffer, packedLight, packedOverlay, () -> {
                        stick_straight.render(matrixStack, buffer, packedLight, packedOverlay);
                        if(time <= .25f) {
                            stick_left.visible = true;
                            stick_right.visible = true;
                            renderSides(matrixStack, buffer, packedLight, packedOverlay, (time - .15f) / .1f);
                        } else {
                            renderSides(matrixStack, buffer, packedLight, packedOverlay, 1.f);
                            prop_left.visible = true;
                            prop_right.visible = true;
                        }
                    });
                }
                break;
            case STARTED_STATE:
                animationRender(matrixStack, buffer, packedLight, packedOverlay, () -> {
                    stick_straight.render(matrixStack, buffer, packedLight, packedOverlay);
                    renderSides(matrixStack, buffer, packedLight, packedOverlay, 1.f);
                    renderProps(matrixStack, buffer, packedLight, packedOverlay, time + 1.f);
                });
                break;
            case STOPPING_STATE:
                if(time > .3f) stoppedState();
                if(time <= .05f) {
                    animationRender(matrixStack, buffer, packedLight, packedOverlay, () -> {
                        stick_straight.render(matrixStack, buffer, packedLight, packedOverlay);
                        renderSides(matrixStack, buffer, packedLight, packedOverlay, 1.f);
                        renderProps(matrixStack, buffer, packedLight, packedOverlay, 1.f - (time / .05f));
                    });
                } else {
                    prop_left.visible = false;
                    prop_right.visible = false;
                    animationRender(matrixStack, buffer, packedLight, packedOverlay, () -> {
                        if(time <= .15f) {
                            stick_straight.render(matrixStack, buffer, packedLight, packedOverlay);
                            renderSides(matrixStack, buffer, packedLight, packedOverlay, 1.f - ((time - .05f) / .1f));
                        } else {
                            stick_left.visible = false;
                            stick_right.visible = false;
                            ModelRendererUtil.render(stick_straight, matrixStack, buffer, packedLight, packedOverlay, new TransformationMatrix(
                                    new Vector3f(0.f, MathHelper.lerp((time - .15f) / .15f, 0.f, minY), 0.f),
                                    Quaternion.ONE, ONE, Quaternion.ONE
                            ));
                        }
                    });
                }
                break;
            case STOPPED_STATE:
                stick_straight.visible = false;
                animation.visible = false;
                break;
        }
    }

    private static final Vector3f ZERO = new Vector3f();
    private static final Vector3f ONE = new Vector3f(1.f, 1.f, 1.f);

    private void animationRender(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay, Runnable children) {
        ModelRendererUtil.render(animation, matrixStack, buffer, packedLight, packedOverlay, null, children);
    }

    private static final Vector3f sideTranslate = new Vector3f(0.f, -18.5f / 16.f, 0.f);
    private void renderSides(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay, float progress) {
        float angle = progress >= 1.f ? 0.f : MathHelper.lerp(progress, 90.f, 0.f);
        ModelRendererUtil.render(stick_left, matrixStack, buffer, packedLight, packedOverlay, new TransformationMatrix(
                sideTranslate, Quaternion.ONE, ONE,
                new Quaternion(0.f, 0.f, angle, true)
        ));
        ModelRendererUtil.render(stick_right, matrixStack, buffer, packedLight, packedOverlay, new TransformationMatrix(
                sideTranslate, Quaternion.ONE, ONE,
                new Quaternion(0.f, 0.f, -angle, true)
        ));
    }

    private void renderProps(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay, float progress) {
        float angle = (float) (progress * 20 * Math.PI);
        ModelRendererUtil.render(prop_left, matrixStack, buffer, packedLight, packedOverlay, new TransformationMatrix(
                ZERO, Quaternion.ONE, ONE, new Quaternion(0.f, -angle, 0.f, false)
        ));
        ModelRendererUtil.render(prop_right, matrixStack, buffer, packedLight, packedOverlay, new TransformationMatrix(
                ZERO, Quaternion.ONE, ONE, new Quaternion(0.f, angle, 0.f, false)
        ));
    }
}