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
    private static final byte STOPPED_STATE = 0;
    public static final byte STARTING_STATE = 1;
    private static final byte STARTED_STATE = 2;
    public static final byte STOPPING_STATE = 3;

    public final ModelRenderer engine;
    private final ModelRenderer stick_straight;

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

        stick_straight = new ModelRenderer(this);
        stick_straight.visible = false;
        stick_straight.texOffs(0, 0).addBox(-0.5F, -9.0F, 4.0F, 1.0F, 10.0F, 1.0F, 0.0F, false);
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
        stick_straight.copyFrom(engine);

        time = Animation.getWorldTime(Minecraft.getInstance().level, partialTicks) - timeWhenStateChanged;
    }

    @Override
    public void renderToBuffer(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
        engine.render(matrixStack, buffer, packedLight, packedOverlay);

        float maxY = -10.f / 16.f;
        switch(state) {
            case STARTING_STATE:
                stick_straight.visible = true;
                if(time > 1.f) startedState();
                if(time <= .25f) {
                    renderStickStraight(matrixStack, buffer, packedLight, packedOverlay,
                            MathHelper.lerp(time / .25f, 0.f, maxY));
                } else renderStickStraight(matrixStack, buffer, packedLight, packedOverlay, maxY);
                break;
            case STARTED_STATE:
                renderStickStraight(matrixStack, buffer, packedLight, packedOverlay, maxY);
                break;
            case STOPPING_STATE:
                break;
            case STOPPED_STATE:
                stick_straight.visible = false;
                break;
        }
    }

    private void renderStickStraight(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay, float y) {
        ModelRendererUtil.render(stick_straight, matrixStack, buffer, packedLight, packedOverlay, new TransformationMatrix(
                new Vector3f(0.f, y, 0.f),
                null,null,null
        ));
    }
}