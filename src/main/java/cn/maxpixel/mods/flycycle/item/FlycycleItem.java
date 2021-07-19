package cn.maxpixel.mods.flycycle.item;

import cn.maxpixel.mods.flycycle.Flycycle;
import cn.maxpixel.mods.flycycle.KeyBindings;
import cn.maxpixel.mods.flycycle.model.item.FlycycleItemModel;
import com.mojang.blaze3d.matrix.MatrixStack;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.model.BipedModel;
import net.minecraft.client.renderer.entity.model.EntityModel;
import net.minecraft.client.renderer.model.ModelRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.EnergyStorage;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class FlycycleItem extends Item {
    public static final String NAME = "flycycle";
    private static final int ENERGY_CAPACITY = 200;
    private boolean engineStatus = false;

    public FlycycleItem() {
        super(new Properties()
                .stacksTo(1)
                .setNoRepair()
                .tab(Flycycle.ITEM_GROUP));
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundNBT nbt) {
        return new ICapabilitySerializable<CompoundNBT>() {
            private final LazyOptional<ChangeableEnergyStorage> ENERGY = LazyOptional.of(ChangeableEnergyStorage::new);
            private final LazyOptional<ICurio> CURIO = LazyOptional.of(() -> new ICurio() {
                private final FlycycleItemModel model = new FlycycleItemModel();

                @Override
                public void curioAnimate(String identifier, int slot, LivingEntity player) {
                    if(KeyBindings.KEY_TOGGLE_ENGINE.isDown()) engineStatus = !engineStatus;
                    if(engineStatus && player instanceof PlayerEntity) {
                        ENERGY.ifPresent(storage -> {
                            if(!storage.use()) engineStatus = false;
                        });
                    }
                }

                @Override
                public boolean canEquipFromUse(SlotContext slotContext) {
                    return true;
                }

                @Override
                public boolean canRender(String identifier, int index, LivingEntity livingEntity) {
                    return true;
                }

                @Override
                public void render(String identifier, int index, MatrixStack matrixStack,
                                   IRenderTypeBuffer renderTypeBuffer, int light, LivingEntity livingEntity,
                                   float limbSwing, float limbSwingAmount, float partialTicks, float ageInTicks,
                                   float netHeadYaw, float headPitch) {
                    followBodyRotations(livingEntity, model.engine);
                    model.renderToBuffer(matrixStack, ItemRenderer.getFoilBuffer(renderTypeBuffer, model.renderType(FlycycleItemModel.MODEL),
                            false, stack.hasFoil()), light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
                }
            });

            @Nonnull
            @Override
            public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
                return Objects.equals(CapabilityEnergy.ENERGY, cap) ? ENERGY.cast() :
                        Objects.equals(CuriosCapability.ITEM, cap) ? CURIO.cast() : LazyOptional.empty();
            }

            @Override
            public CompoundNBT serializeNBT() {
                CompoundNBT compound = new CompoundNBT();
                ENERGY.ifPresent(storage -> compound.putInt("Energy", storage.getEnergyStored()));
                return compound;
            }

            @Override
            public void deserializeNBT(CompoundNBT nbt) {
                if(nbt.contains("Energy") && nbt.getTagType("Energy") == Constants.NBT.TAG_INT)
                    ENERGY.ifPresent(storage -> storage.setEnergy(nbt.getInt("Energy")));
            }
        };
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        return false;
    }

    private static void followBodyRotations(LivingEntity livingEntity, ModelRenderer renderer) {
        EntityRenderer<? super LivingEntity> entityRenderer = Minecraft.getInstance().getEntityRenderDispatcher().getRenderer(livingEntity);
        if(entityRenderer instanceof LivingRenderer) {
            LivingRenderer<LivingEntity, EntityModel<LivingEntity>> livingRenderer =
                    (LivingRenderer<LivingEntity, EntityModel<LivingEntity>>) entityRenderer;
            EntityModel<LivingEntity> entityModel = livingRenderer.getModel();
            if(entityModel instanceof BipedModel) {
                renderer.copyFrom(((BipedModel<LivingEntity>) entityModel).body);
            }
        }
    }

    public static class ChangeableEnergyStorage extends EnergyStorage {
        public ChangeableEnergyStorage() {
            super(ENERGY_CAPACITY, ENERGY_CAPACITY, 0);
        }

        private boolean use() {
            if(energy - 50 > 0) {
                energy -= 50;
                return true;
            }
            return false;
        }

        public void setEnergy(int energy) {
            this.energy = MathHelper.clamp(energy, 0, capacity);
        }
    }
}