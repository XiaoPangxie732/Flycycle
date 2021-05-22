package cn.maxpixel.mods.flycycle.block.entity;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.profiler.IProfiler;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.Direction;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.IEnergyStorage;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Collections;
import java.util.Objects;
import java.util.stream.LongStream;

import static net.minecraft.util.math.MathHelper.floor;
import static net.minecraftforge.energy.CapabilityEnergy.ENERGY;

public class InfPowerGeneratorBlockEntity extends TileEntity implements ITickableTileEntity {
    private static final AxisAlignedBB RANGE_BOX = AxisAlignedBB.ofSize(1000, 1000, 1000)
            .move(.0d, 400.0d, .0d);
    private final AxisAlignedBB currentBox = RANGE_BOX.move(getBlockPos().getX(), .0d, getBlockPos().getZ());
    private final long[] rangeChunks = ChunkPos.rangeClosed(new ChunkPos(floor(currentBox.minX) >> 4, floor(currentBox.minZ) >> 4),
            new ChunkPos(floor(currentBox.maxX) >> 4, floor(currentBox.maxZ) >> 4)).mapToLong(ChunkPos::toLong).toArray();
    private static final LazyOptional<IEnergyStorage> energyStorage = LazyOptional.of(() -> new IEnergyStorage() {
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
        return Objects.equals(cap, ENERGY) ? energyStorage.cast() : super.getCapability(cap, side);
    }

    private void extractEnergy(ICapabilityProvider toExtract) {
        toExtract.getCapability(ENERGY).filter(IEnergyStorage::canReceive).ifPresent(energy -> energyStorage.ifPresent(storage ->
                energy.receiveEnergy(storage.extractEnergy(energy.getMaxEnergyStored() - energy.getEnergyStored(), false), false)));
    }

    private boolean waterAround() {
        BlockPos.Mutable mutable = getBlockPos().mutable();
        for(Direction direction : Direction.values()) if(level.isWaterAt(mutable.move(direction))) return true;
        return false;
    }

    @Override
    public void tick() {
        IProfiler profiler = level.getProfiler();
        profiler.push("check");
        if(hasLevel() && !level.isClientSide && level.isLoaded(getBlockPos()) && waterAround()) {
            profiler.popPush("extractEnergy");
            LongStream.of(rangeChunks).mapToObj(pos -> level.getChunkSource().getChunkNow(ChunkPos.getX(pos), ChunkPos.getZ(pos)))
                    .parallel().filter(Objects::nonNull).forEach(chunk -> {
                chunk.getEntities((Entity) null, currentBox, Collections.emptyList(), entity -> {
                    extractEnergy(entity);
                    if(entity instanceof PlayerEntity) {
                        ((PlayerEntity) entity).inventory.items.parallelStream().forEach(this::extractEnergy);
                        ((PlayerEntity) entity).inventory.armor.parallelStream().forEach(this::extractEnergy);
                        ((PlayerEntity) entity).inventory.offhand.parallelStream().forEach(this::extractEnergy);
                    } else if(entity instanceof IInventory) {
                        IInventory inv = (IInventory) entity;
                        for(int i = 0; i < inv.getContainerSize(); i++) extractEnergy(inv.getItem(i));
                    }
                    return false;
                });
                chunk.getBlockEntities().values().forEach(be -> {
                    if(level.isLoaded(be.getBlockPos())) {
                        extractEnergy(be);
                        if(be instanceof IInventory) {
                            IInventory inv = (IInventory) be;
                            for(int i = 0; i < inv.getContainerSize(); i++) extractEnergy(inv.getItem(i));
                        }
                    }
                });
            });
        }
        profiler.pop();
    }
}