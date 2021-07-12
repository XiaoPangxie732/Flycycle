package cn.maxpixel.mods.flycycle.item;

import cn.maxpixel.mods.flycycle.Flycycle;
import cn.maxpixel.mods.flycycle.KeyBindings;
import cn.maxpixel.mods.flycycle.networking.NetworkManager;
import cn.maxpixel.mods.flycycle.networking.packet.clientbound.CSyncItemStackEnergyPacket;
import cn.maxpixel.mods.flycycle.networking.packet.serverbound.SSyncItemStackEnergyPacket;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.DispenserBlock;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.item.ArmorItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.Direction;
import net.minecraft.util.math.MathHelper;
import net.minecraft.world.World;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.capabilities.ICapabilitySerializable;
import net.minecraftforge.common.util.Constants;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import net.minecraftforge.fml.network.PacketDistributor;
import top.theillusivec4.curios.api.SlotContext;
import top.theillusivec4.curios.api.type.capability.ICurioItem;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class FlycycleItem extends Item implements ICurioItem {
    public static final String NAME = "flycycle";
    private static final int ENERGY_CAPACITY = 200;
    private boolean engineStatus = false;

    public FlycycleItem() {
        super(new Properties()
                .stacksTo(1)
                .setNoRepair()
                .tab(Flycycle.ITEM_GROUP));
        DispenserBlock.registerBehavior(this, ArmorItem.DISPENSE_ITEM_BEHAVIOR);
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundNBT nbt) {
        return new ICapabilitySerializable<CompoundNBT>() {
            private final LazyOptional<ChangeableEnergyStorage> ENERGY = LazyOptional.of(ChangeableEnergyStorage::new);

            @Nonnull
            @Override
            public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
                return CapabilityEnergy.ENERGY == cap ? ENERGY.cast() : LazyOptional.empty();
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
    public double getDurabilityForDisplay(ItemStack stack) {
        IEnergyStorage storage = stack.getCapability(CapabilityEnergy.ENERGY).orElse(new EnergyStorage(0));
        return (storage.getMaxEnergyStored() - storage.getEnergyStored()) / (double) storage.getMaxEnergyStored();
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        return true;
    }

    @Override
    public void inventoryTick(ItemStack itemStack, World level, Entity player, int slot, boolean selected) {
        super.inventoryTick(itemStack, level, player, slot, selected);
        if(!level.isClientSide) {
            itemStack.getCapability(CapabilityEnergy.ENERGY)
                    .filter(ChangeableEnergyStorage.class::isInstance)
                    .map(ChangeableEnergyStorage.class::cast)
                    .filter(ChangeableEnergyStorage::needUpdate)
                    .ifPresent(storage -> {
                        NetworkManager.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player),
                                new SSyncItemStackEnergyPacket(player.getId(), slot, storage.getEnergyStored()));
                        storage.updated();
                    });
        } else {
            if(KeyBindings.KEY_TOGGLE_ENGINE.isDown()) engineStatus = !engineStatus;
            if(engineStatus) {
                itemStack.getCapability(CapabilityEnergy.ENERGY)
                        .filter(ChangeableEnergyStorage.class::isInstance)
                        .map(ChangeableEnergyStorage.class::cast)
                        .ifPresent(storage -> {
                            if(storage.use()) {
                                NetworkManager.send(PacketDistributor.PLAYER.with(() -> (ServerPlayerEntity) player),
                                        new CSyncItemStackEnergyPacket(player.getId(), slot, storage.getEnergyStored()));
                            } else engineStatus = false;
                        });
                engineStatus = false;
            }
        }
    }

    @Override
    public void curioAnimate(String identifier, int index, LivingEntity livingEntity, ItemStack stack) {
        ICurioItem.super.curioAnimate(identifier, index, livingEntity, stack);
    }

    @Override
    public boolean canEquipFromUse(SlotContext slotContext, ItemStack stack) {
        return true;
    }

    public static class ChangeableEnergyStorage extends EnergyStorage {
        private boolean needUpdate;

        public ChangeableEnergyStorage() {
            super(ENERGY_CAPACITY, ENERGY_CAPACITY, 0);
        }

        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) {
            this.needUpdate = true;
            return super.receiveEnergy(maxReceive, simulate);
        }

        private boolean use() {
            if(energy >= 200) {
                energy = 0;
                return true;
            }
            return false;
        }

        public boolean needUpdate() {
            return this.needUpdate;
        }

        public void updated() {
            this.needUpdate = false;
        }

        public void setEnergy(int energy) {
            this.energy = MathHelper.clamp(energy, 0, capacity);
        }
    }
}