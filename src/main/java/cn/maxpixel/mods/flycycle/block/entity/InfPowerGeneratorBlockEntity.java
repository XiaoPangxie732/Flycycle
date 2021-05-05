package cn.maxpixel.mods.flycycle.block.entity;

import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

public class InfPowerGeneratorBlockEntity extends TileEntity implements ITickableTileEntity {
    private final LazyOptional<IEnergyStorage> energyStorage = LazyOptional.of(() -> new IEnergyStorage() {
        @Override
        public int receiveEnergy(int maxReceive, boolean simulate) { return 0; }

        @Override
        public int extractEnergy(int maxExtract, boolean simulate) { return maxExtract; }

        @Override
        public int getEnergyStored() { return Integer.MAX_VALUE; }

        @Override
        public int getMaxEnergyStored() { return Integer.MAX_VALUE; }

        @Override
        public boolean canExtract() { return true; }

        @Override
        public boolean canReceive() { return false; }
    });

    public InfPowerGeneratorBlockEntity() {
        super(BlockEntityRegistry.INF_POWER_GENERATOR.get());
    }

    @Nonnull
    @Override
    public <T> LazyOptional<T> getCapability(@Nonnull Capability<T> cap, @Nullable Direction side) {
        return Objects.equals(cap, CapabilityEnergy.ENERGY) ? energyStorage.cast() : super.getCapability(cap, side);
    }

    @Override
    public void tick() {
    }
}