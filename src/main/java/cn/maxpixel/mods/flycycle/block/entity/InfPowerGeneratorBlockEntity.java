package cn.maxpixel.mods.flycycle.block.entity;

import cn.maxpixel.mods.flycycle.util.ChunkPosUtil;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.IInventory;
import net.minecraft.profiler.IProfiler;
import net.minecraft.tileentity.ITickableTileEntity;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.ClassInheritanceMultiMap;
import net.minecraft.util.Direction;
import net.minecraft.world.biome.Biome;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ICapabilityProvider;
import net.minecraftforge.common.util.LazyOptional;
import net.minecraftforge.energy.EnergyStorage;
import net.minecraftforge.energy.IEnergyStorage;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;
import top.theillusivec4.curios.api.type.inventory.IDynamicStackHandler;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Map;
import java.util.Objects;

import static net.minecraftforge.energy.CapabilityEnergy.ENERGY;

public class InfPowerGeneratorBlockEntity extends TileEntity implements ITickableTileEntity {
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
        toExtract.getCapability(ENERGY)
                .filter(IEnergyStorage::canReceive)
                .ifPresent(energy -> energy.receiveEnergy(
                        energyStorage.orElseGet(() -> new EnergyStorage(-1))
                                .extractEnergy(energy.getMaxEnergyStored() - energy.getEnergyStored(),false),
                        false));
    }

    private boolean waterAround() {
        for(Direction direction : Direction.values()) if(level.isWaterAt(getBlockPos().relative(direction))) return true;
        return false;
    }

    @Override
    public void tick() {
        IProfiler profiler = level.getProfiler();
        profiler.push("prerequisitesCheck");
        if(hasLevel() && !level.isClientSide && level.isLoaded(getBlockPos()) &&
                level.getBiome(getBlockPos()).getBiomeCategory() == Biome.Category.OCEAN && waterAround()) {
            profiler.popPush("extractEnergy");
            int minX = -20 + (getBlockPos().getX() >> 4);
            int minZ = -20 + (getBlockPos().getZ() >> 4);
            int maxX = 20 + (getBlockPos().getX() >> 4);
            int maxZ = 20 + (getBlockPos().getZ() >> 4);
            ChunkPosUtil.rangeClosed(minX, minZ, maxX, maxZ, level)
                    .filter(Objects::nonNull)
                    .forEach(chunk -> {
                        for(ClassInheritanceMultiMap<Entity> entitySection : chunk.getEntitySections()) {
                            entitySection.forEach(entity -> {
                                extractEnergy(entity);
                                if(entity instanceof PlayerEntity) {
                                    ((PlayerEntity) entity).inventory.items.parallelStream().forEach(this::extractEnergy);
                                    ((PlayerEntity) entity).inventory.armor.parallelStream().forEach(this::extractEnergy);
                                    extractEnergy(((PlayerEntity) entity).inventory.offhand.get(0));
                                    CuriosApi.getCuriosHelper()
                                            .getCuriosHandler((LivingEntity) entity)
                                            .map(ICuriosItemHandler::getCurios)
                                            .map(Map::values)
                                            .ifPresent(handlers -> handlers.forEach(handler -> {
                                                IDynamicStackHandler stacks = handler.getStacks();
                                                for(int i = 0; i < stacks.getSlots(); i++) extractEnergy(stacks.getStackInSlot(i));
                                            }));
                                } else if(entity instanceof IInventory) {
                                    IInventory inv = (IInventory) entity;
                                    for(int i = 0; i < inv.getContainerSize(); i++) extractEnergy(inv.getItem(i));
                                }
                            });
                        }
                        chunk.getBlockEntities().forEach((bp, be) -> {
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