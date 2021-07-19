package cn.maxpixel.mods.flycycle.model.item;

import cn.maxpixel.mods.flycycle.Flycycle;
import com.google.common.collect.ImmutableMap;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.model.animation.ModelBlockAnimation;
import net.minecraftforge.common.animation.TimeValues;
import net.minecraftforge.common.model.animation.AnimationStateMachine;
import net.minecraftforge.common.model.animation.IAnimationStateMachine;

@OnlyIn(Dist.CLIENT)
public class FlycycleItemModel extends EntityModel<Entity> {
    public static final ResourceLocation MODEL = new ResourceLocation(Flycycle.MODID, "textures/entity/flycycle.png");
    private static final ResourceLocation ASM = new ResourceLocation(Flycycle.MODID, "asms/entity/flycycle.json");
    private static final ResourceLocation ARMATURE = new ResourceLocation(Flycycle.MODID, "armatures/entity/flycycle.json");

    public final ModelRenderer engine;
    private final ModelRenderer stick_straight;

    private final TimeValues.VariableValue cycleLength = new TimeValues.VariableValue(4.0f);
    private final IAnimationStateMachine asm = AnimationStateMachine.load(Minecraft.getInstance().getResourceManager(), ASM, ImmutableMap.of(
            "cycle_length", cycleLength
    ));
    private final ModelBlockAnimation armature = ModelBlockAnimation.loadVanillaAnimation(Minecraft.getInstance().getResourceManager(), ARMATURE);

    public FlycycleItemModel() {
        texWidth = 64;
        texHeight = 64;

        engine = new ModelRenderer(this);
        engine.texOffs(0, 0).addBox(-1.5F, -1.0F, -1.0F, 5.0F, 1.0F, 5.0F, 0.0F, false);
        engine.texOffs(20, 6).addBox(-0.5F, -14.0F, -1.0F, 4.0F, 13.0F, 1.0F, 0.0F, false);
        engine.texOffs(10, 6).addBox(2.5F, -14.0F, 0.0F, 1.0F, 13.0F, 4.0F, 0.0F, false);
        engine.texOffs(30, 6).addBox(-1.5F, -14.0F, -1.0F, 1.0F, 13.0F, 4.0F, 0.0F, false);
        engine.texOffs(0, 6).addBox(-1.5F, -14.0F, 3.0F, 4.0F, 13.0F, 1.0F, 0.0F, false);

        stick_straight = new ModelRenderer(this);
        stick_straight.texOffs(0, 0).addBox(-0.5F, -15.0F, 5.0F, 1.0F, 14.0F, 1.0F, 0.0F, false);
    }

    @Override
    public void setupAnim(Entity entity, float v, float v1, float v2, float v3, float v4) {
    }

    public float partialTicks;

    @Override
    public void renderToBuffer(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
        stick_straight.copyFrom(engine);
        engine.x += -1.0F;
        engine.y += 12.0F;
        engine.z += 5.0F;

        stick_straight.x += 0.0F;
        stick_straight.y += 12.0F;
        stick_straight.z += 1.0F;

//        asm.apply(Animation.getWorldTime(Minecraft.getInstance().level, partialTicks));

        engine.render(matrixStack, buffer, packedLight, packedOverlay);
        stick_straight.render(matrixStack, buffer, packedLight, packedOverlay);
    }
}