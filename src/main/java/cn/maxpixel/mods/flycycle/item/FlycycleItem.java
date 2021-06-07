package cn.maxpixel.mods.flycycle.item;

import cn.maxpixel.mods.flycycle.Flycycle;
import cn.maxpixel.mods.flycycle.KeyBindings;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.EquipmentSlotType;
import net.minecraft.item.Item;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.util.ActionResult;
import net.minecraft.util.Direction;
import net.minecraft.util.Hand;
import net.minecraft.util.NonNullList;
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

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class FlycycleItem extends Item {
    public static final String NAME = "flycycle";
    private static final int ENERGY_CAPACITY = 200;

    public FlycycleItem() {
        super(new Properties()
                .durability(ENERGY_CAPACITY)
                .setNoRepair()
                .tab(Flycycle.ITEM_GROUP));
    }

    @Nullable
    @Override
    public ICapabilityProvider initCapabilities(ItemStack stack, @Nullable CompoundNBT nbt) {
        return new ICapabilitySerializable<CompoundNBT>() {
            private LazyOptional<IEnergyStorage> ENERGY = LazyOptional.of(() -> new EnergyStorage(ENERGY_CAPACITY, ENERGY_CAPACITY, 0));

            @Nonnull
            @Override
            public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
                return CapabilityEnergy.ENERGY.orEmpty(cap, ENERGY);
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
                    ENERGY = LazyOptional.of(() -> new EnergyStorage(ENERGY_CAPACITY, ENERGY_CAPACITY, 0, nbt.getInt("Energy")));
            }
        };
    }

    @Override
    public void fillItemCategory(ItemGroup group, NonNullList<ItemStack> itemStacks) {
        if(this.allowdedIn(group)) {
            ItemStack is = new ItemStack(this);
            is.setDamageValue(ENERGY_CAPACITY);
            itemStacks.add(is);
        }
    }

    @Override
    public int getDamage(ItemStack stack) {
        IEnergyStorage storage = stack.getCapability(CapabilityEnergy.ENERGY).orElse(new EnergyStorage(-1));
        return MathHelper.clamp(storage.getMaxEnergyStored() - storage.getEnergyStored(), 0, ENERGY_CAPACITY);
    }

    @Override
    public boolean showDurabilityBar(ItemStack stack) {
        return true;
    }

    @Override
    public void inventoryTick(ItemStack itemStack, World level, Entity player, int slot, boolean selected) {
        super.inventoryTick(itemStack, level, player, slot, selected);
        if(level.isClientSide && KeyBindings.KEY_FLY.isDown()) {
            itemStack.getCapability(CapabilityEnergy.ENERGY, Direction.DOWN);
        }
    }

    @Nullable
    @Override
    public EquipmentSlotType getEquipmentSlot(ItemStack stack) {
        return EquipmentSlotType.CHEST;
    }

    @Override
    public ActionResult<ItemStack> use(World level, PlayerEntity player, Hand hand) {
        ItemStack itemStack = player.getItemInHand(hand);
        if(player.getItemBySlot(EquipmentSlotType.CHEST).isEmpty()) {
            player.setItemSlot(EquipmentSlotType.CHEST, itemStack.copy());
            itemStack.setCount(0);
            return ActionResult.sidedSuccess(itemStack, level.isClientSide);
        }
        return ActionResult.fail(itemStack);
    }
}