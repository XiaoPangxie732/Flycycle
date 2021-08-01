package cn.maxpixel.mods.flycycle.item;

import cn.maxpixel.mods.flycycle.Flycycle;
import cn.maxpixel.mods.flycycle.KeyBindings;
import cn.maxpixel.mods.flycycle.event.player.AnimationStateChangedEvent;
import cn.maxpixel.mods.flycycle.event.player.EngineWorkEvent;
import cn.maxpixel.mods.flycycle.model.item.FlycycleItemModel;
import cn.maxpixel.mods.flycycle.networking.NetworkManager;
import cn.maxpixel.mods.flycycle.networking.packet.clientbound.SSyncCurioItemStackEnergyPacket;
import cn.maxpixel.mods.flycycle.networking.packet.clientbound.SSyncItemStackEnergyPacket;
import cn.maxpixel.mods.flycycle.networking.packet.serverbound.CAnimationStateChangedPacket;
import cn.maxpixel.mods.flycycle.networking.packet.serverbound.CEngineWorkPacket;
import cn.maxpixel.mods.flycycle.networking.packet.serverbound.CSyncCurioItemStackEnergyPacket;
import cn.maxpixel.mods.flycycle.networking.packet.serverbound.CSyncItemStackEnergyPacket;
import com.mojang.blaze3d.matrix.MatrixStack;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.ItemRenderer;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Pose;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.ForgeMod;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.network.PacketDistributor;
import top.theillusivec4.curios.api.CuriosCapability;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurio;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Objects;
import java.util.UUID;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class FlycycleItem extends Item {
    public static final String NAME = "flycycle";
    private static final int ENERGY_CAPACITY = 200;
    private static final AttributeModifier SPEED_MODIFIER = new AttributeModifier(
            UUID.fromString("A09C5FFC-3230-46B3-8F86-0BE99C5215FE"),
            "effect." + Flycycle.MODID + ".swimming_speed",
            2., AttributeModifier.Operation.MULTIPLY_BASE
    );

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
        private EngineWorkEvent engineWork;
        @OnlyIn(Dist.CLIENT)
        private FlycycleItemModel<AbstractClientPlayerEntity> model;
        private final LazyOptional<ChangeableEnergyStorage> ENERGY;
        private final ItemStack stack;
        private AnimationStateChangedEvent lastEvent;

        private FlycycleCurio(LazyOptional<ChangeableEnergyStorage> energy, ItemStack stack) {
            this.ENERGY = energy;
            this.stack = stack;
            MinecraftForge.EVENT_BUS.addListener(this::onEngineWork);
            if(FMLEnvironment.dist.isClient())
                MinecraftForge.EVENT_BUS.addListener(this::onStateChanged);
        }

        public void onStateChanged(AnimationStateChangedEvent event) {
            lastEvent = event;
        }

        public void onEngineWork(EngineWorkEvent event) {
            engineWork = event;
        }

        @Override
        public void onEquip(SlotContext slotContext, ItemStack prevStack) {
            if(slotContext.getWearer() instanceof ServerPlayerEntity) {
                ENERGY.ifPresent(storage -> storage.needUpdate = true);
            }
        }

        @Override
        public void curioTick(String identifier, int index, LivingEntity player) {
            if(engineWork != null && player instanceof ServerPlayerEntity && engineWork.getPlayer().getId() == player.getId() &&
                    engineWork.slot == index) {
                if(engineWork.work) {
                    player.fallDistance = 0.f;
                    if(player.isSwimming()) {
                        if(!player.getAttribute(ForgeMod.SWIM_SPEED.get()).hasModifier(SPEED_MODIFIER))
                            player.getAttribute(ForgeMod.SWIM_SPEED.get()).addTransientModifier(SPEED_MODIFIER);
                    } else if(player.getPose() == Pose.STANDING && !(player.isUnderWater() && player.isSprinting())) {
                        ((ServerPlayerEntity) player).abilities.mayfly = true;
                    }
                } else {
                    player.getAttribute(ForgeMod.SWIM_SPEED.get()).removeModifier(SPEED_MODIFIER);
                    if(!((ServerPlayerEntity) player).isCreative() && !player.isSpectator()) {
                        ((ServerPlayerEntity) player).abilities.mayfly = false;
                    }
                }
            }
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

        private boolean engineWorkSent;

        @Override
        public void curioAnimate(String identifier, int slot, LivingEntity player) {
            if(model != null) {
                if(lastEvent != null && player instanceof AbstractClientPlayerEntity) {
                    if(lastEvent.getPlayer().getId() == player.getId() && lastEvent.slot == slot && lastEvent.state != model.getState()) {
                        switch(lastEvent.state) {
                            case FlycycleItemModel.STARTING_STATE:
                                model.startingState();
                                break;
                            case FlycycleItemModel.STOPPING_STATE:
                                model.stoppingState();
                                NetworkManager.sendToServer(new CEngineWorkPacket(player.getId(), slot, false));
                                engineWorkSent = false;
                                break;
                            default: throw new IllegalStateException();
                        }
                    }
                    lastEvent = null;
                }
                if(player instanceof ClientPlayerEntity) {
                    if((model.getState() & (FlycycleItemModel.STARTING_STATE | FlycycleItemModel.STARTED_STATE)) != 0) {
                        if((model.getState() & FlycycleItemModel.STARTED_STATE) != 0) {
                            if(!engineWorkSent) {
                                NetworkManager.sendToServer(new CEngineWorkPacket(player.getId(), slot, true));
                                engineWorkSent = true;
                            }
                            if(player.isSwimming()) {
                                if(!player.getAttribute(ForgeMod.SWIM_SPEED.get()).hasModifier(SPEED_MODIFIER))
                                    player.getAttribute(ForgeMod.SWIM_SPEED.get()).addTransientModifier(SPEED_MODIFIER);
                            } else if(player.getPose() == Pose.STANDING && !(player.isUnderWater() && player.isSprinting())) {
                                ((ClientPlayerEntity) player).abilities.flying = true;
                                ((ClientPlayerEntity) player).abilities.setFlyingSpeed(.15f);
                            }
                        }
                        ENERGY.ifPresent(storage -> {
                            if(!storage.use()) {
                                NetworkManager.sendToServer(new CAnimationStateChangedPacket(player.getId(), slot, FlycycleItemModel.STOPPING_STATE));
                            }
                        });
                    } else {
                        player.getAttribute(ForgeMod.SWIM_SPEED.get()).removeModifier(SPEED_MODIFIER);
                        ((ClientPlayerEntity) player).abilities.setFlyingSpeed(.05f);
                        if(!((ClientPlayerEntity) player).isCreative() && !player.isSpectator()) {
                            ((ClientPlayerEntity) player).abilities.flying = false;
                        }
                    }
                }
                if(KeyBindings.KEY_TOGGLE_ENGINE.isDown()) {
                    if(model.getState() == FlycycleItemModel.STOPPED_STATE) {
                        ENERGY.ifPresent(storage -> {
                            if(storage.use()) {
                                NetworkManager.sendToServer(new CAnimationStateChangedPacket(player.getId(), slot, FlycycleItemModel.STARTING_STATE));
                            }
                        });
                    } else if(model.getState() == FlycycleItemModel.STARTED_STATE) {
                        NetworkManager.sendToServer(new CAnimationStateChangedPacket(player.getId(), slot, FlycycleItemModel.STOPPING_STATE));
                    }
                }
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
            if(model == null) model = new FlycycleItemModel<>();
            model.partialTicks = partialTicks;
            model.setupAnim((AbstractClientPlayerEntity) livingEntity, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
            model.renderToBuffer(matrixStack, ItemRenderer.getFoilBuffer(renderTypeBuffer, model.renderType(FlycycleItemModel.MODEL),
                    false, stack.hasFoil()), light, OverlayTexture.NO_OVERLAY, 1.0F, 1.0F, 1.0F, 1.0F);
        }
    }
}