package cn.maxpixel.mods.flycycle.item;

import cn.maxpixel.mods.flycycle.Flycycle;
import cn.maxpixel.mods.flycycle.KeyBindings;
import cn.maxpixel.mods.flycycle.event.player.AnimationStateChangedEvent;
import cn.maxpixel.mods.flycycle.model.item.FlycycleItemModel;
import cn.maxpixel.mods.flycycle.networking.NetworkManager;
import cn.maxpixel.mods.flycycle.networking.packet.clientbound.CAnimationStateChangedPacket;
import cn.maxpixel.mods.flycycle.networking.packet.clientbound.CSyncCurioItemStackEnergyPacket;
import cn.maxpixel.mods.flycycle.networking.packet.clientbound.CSyncItemStackEnergyPacket;
import cn.maxpixel.mods.flycycle.networking.packet.serverbound.SSyncCurioItemStackEnergyPacket;
import cn.maxpixel.mods.flycycle.networking.packet.serverbound.SSyncItemStackEnergyPacket;
import com.mojang.blaze3d.matrix.MatrixStack;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.network.PacketDistributor;
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
            private final LazyOptional<ICurio> CURIO = LazyOptional.of(() -> new FlycycleCurio(ENERGY, stack));

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
        return true;
    }

    @Override
    public double getDurabilityForDisplay(ItemStack stack) {
        IEnergyStorage storage = stack.getCapability(CapabilityEnergy.ENERGY).orElseGet(() -> new EnergyStorage(0));
        return (storage.getMaxEnergyStored() - storage.getEnergyStored()) / (double) storage.getMaxEnergyStored();
    }

    @Override
    public void inventoryTick(ItemStack itemStack, World level, Entity player, int slot, boolean selected) {
        if(!level.isClientSide) {
            itemStack.getCapability(CapabilityEnergy.ENERGY)
                    .filter(ChangeableEnergyStorage.class::isInstance)
                    .map(ChangeableEnergyStorage.class::cast)
                    .filter(storage -> storage.needUpdate && player instanceof ServerPlayerEntity && slot >= 0)
                    .ifPresent(storage -> {
                        NetworkManager.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player),
                                new SSyncItemStackEnergyPacket(player.getId(), slot, storage.getEnergyStored()));
                        storage.needUpdate = false;
                    });
        } else {
            itemStack.getCapability(CapabilityEnergy.ENERGY)
                    .filter(ChangeableEnergyStorage.class::isInstance)
                    .map(ChangeableEnergyStorage.class::cast)
                    .filter(storage -> storage.needUpdate && player instanceof ClientPlayerEntity && slot >= 0)
                    .ifPresent(storage -> {
                        NetworkManager.sendToServer(new CSyncItemStackEnergyPacket(player.getId(), slot, storage.getEnergyStored()));
                        storage.needUpdate = false;
                    });
        }
    }

    public static class ChangeableEnergyStorage extends EnergyStorage {
        private boolean needUpdate;

        public ChangeableEnergyStorage() {
            super(ENERGY_CAPACITY, ENERGY_CAPACITY, 0);
        }

        private boolean use() {
            if(energy >= 50) {
                energy -= 50;
                needUpdate = true;
                return true;
            }
            return false;
        }

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            int received = super.receiveEnergy(maxReceive, simulate);
            if(!simulate && received > 0) {
                needUpdate = true;
            }
            return received;
        }

        public void setEnergy(int energy) {
            this.energy = MathHelper.clamp(energy, 0, capacity);
        }
    }

    public static class FlycycleCurio implements ICurio {
        @OnlyIn(Dist.CLIENT)
        private FlycycleItemModel<AbstractClientPlayerEntity> model;
        private boolean engineStatus = false;
        private final LazyOptional<ChangeableEnergyStorage> ENERGY;
        private final ItemStack stack;
        private AnimationStateChangedEvent lastEvent;

        private FlycycleCurio(LazyOptional<ChangeableEnergyStorage> energy, ItemStack stack) {
            this.ENERGY = energy;
            this.stack = stack;
            if(FMLEnvironment.dist.isClient()) MinecraftForge.EVENT_BUS.register(this);
        }

        @SubscribeEvent
        public void onStateChanged(AnimationStateChangedEvent event) {
            if(event.getPlayer() instanceof AbstractClientPlayerEntity) lastEvent = event;
        }

        @Override
        public void curioTick(String identifier, int index, LivingEntity player) {
            ENERGY.filter(storage -> storage.needUpdate && player instanceof ServerPlayerEntity)
                    .ifPresent(storage -> {
                        NetworkManager.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player),
                                new SSyncCurioItemStackEnergyPacket(player.getId(), index, storage.getEnergyStored()));
                        storage.needUpdate = false;
                    });
            ENERGY.filter(storage -> storage.needUpdate && player instanceof ClientPlayerEntity)
                    .ifPresent(storage -> {
                        NetworkManager.sendToServer(new CSyncCurioItemStackEnergyPacket(player.getId(), index, storage.getEnergyStored()));
                        storage.needUpdate = false;
                    });
        }

        @Override
        public void curioAnimate(String identifier, int slot, LivingEntity player) {
            if(KeyBindings.KEY_TOGGLE_ENGINE.isDown()) engineStatus = !engineStatus;
            if(engineStatus && player instanceof ClientPlayerEntity) {
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
            if(!(livingEntity instanceof AbstractClientPlayerEntity)) return;
            if(model == null) {
                model = new FlycycleItemModel<>();
                new Thread(() -> {
                    try {
                        Thread.sleep(1000);
                    } catch (InterruptedException ignored) {
                    }
                    NetworkManager.sendToServer(new CAnimationStateChangedPacket(livingEntity.getId(), index, FlycycleItemModel.STARTING_STATE));
                }).start();// test only
            }
            model.partialTicks = partialTicks;
            if(lastEvent != null) {
                if(lastEvent.getPlayer().getId() != livingEntity.getId()) {
                    lastEvent = null;
                } else if(lastEvent.slot == index && lastEvent.state != model.getState()) {
                    switch(lastEvent.state) {
                        case FlycycleItemModel.STARTING_STATE:
                            model.startingState();
                            break;
                        case FlycycleItemModel.STOPPING_STATE:
                            model.stoppingState();
                            break;
                        default: throw new IllegalStateException();
                    }
                    lastEvent = null;
                }
            }
            model.setupAnim((AbstractClientPlayerEntity) livingEntity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
            model.renderToBuffer(matrixStack, ItemRenderer.getFoilBuffer(renderTypeBuffer, model.renderType(FlycycleItemModel.MODEL),
                    false, stack.hasFoil()), light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        }
    }
}