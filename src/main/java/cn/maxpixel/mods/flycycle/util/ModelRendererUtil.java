package cn.maxpixel.mods.flycycle.util;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.math.vector.TransformationMatrix;

public class ModelRendererUtil {
    public static void render(ModelRenderer renderer, MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay, TransformationMatrix matrix) {
        if (renderer.visible) {
            matrix.push(matrixStack);
            renderer.translateAndRotate(matrixStack);
            renderer.compile(matrixStack.last(), buffer, packedLight, packedOverlay, 1.0F, 1.0F, 1.0F, 1.0F);

            matrixStack.popPose();
        }
    }
}