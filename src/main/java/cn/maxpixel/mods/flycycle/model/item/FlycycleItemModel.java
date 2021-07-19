package cn.maxpixel.mods.flycycle.model.item;

import cn.maxpixel.mods.flycycle.Flycycle;
import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.entity.Entity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class FlycycleItemModel extends EntityModel<Entity> {
    public static final ResourceLocation MODEL = new ResourceLocation(Flycycle.MODID, "textures/entity/flycycle.png");

    public final ModelRenderer engine;
    private final ModelRenderer stick_straight;

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

    @Override
    public void renderToBuffer(MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay,
                               float red, float green, float blue, float alpha) {
        stick_straight.copyFrom(engine);
        engine.x += -1.0F;
        engine.z += 5.0F;
        stick_straight.z += 1.0F;

        engine.render(matrixStack, buffer, packedLight, packedOverlay);
        stick_straight.render(matrixStack, buffer, packedLight, packedOverlay);
    }
}