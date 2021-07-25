package cn.maxpixel.mods.flycycle.util;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.util.math.vector.TransformationMatrix;
import net.minecraft.util.math.vector.Vector3f;

public class ModelRendererUtil {
    public static void render(ModelRenderer renderer, MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay,
                              TransformationMatrix matrix) {
        render(renderer, matrixStack, buffer, packedLight, packedOverlay, matrix, null);
    }

    public static void render(ModelRenderer renderer, MatrixStack matrixStack, IVertexBuilder buffer, int packedLight, int packedOverlay,
                              TransformationMatrix matrix, Runnable children) {
        if (renderer.visible) {
            matrixStack.pushPose();

            renderer.translateAndRotate(matrixStack);

            if(matrix != null) {
                matrixStack.mulPose(matrix.getLeftRotation());

                Vector3f translation = matrix.getTranslation();
                matrixStack.translate(translation.x(), translation.y(), translation.z());

                Vector3f scale = matrix.getScale();
                matrixStack.scale(scale.x(), scale.y(), scale.z());

                matrixStack.mulPose(matrix.getRightRot());
            }

            renderer.compile(matrixStack.last(), buffer, packedLight, packedOverlay, 1.0F, 1.0F, 1.0F, 1.0F);

            if(children != null) children.run();

            matrixStack.popPose();
        }
    }
}