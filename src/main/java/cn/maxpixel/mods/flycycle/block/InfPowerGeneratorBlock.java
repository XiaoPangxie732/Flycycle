package cn.maxpixel.mods.flycycle.block;

import cn.maxpixel.mods.flycycle.block.entity.BlockEntityRegistry;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.IBlockReader;

import javax.annotation.Nullable;

public class InfPowerGeneratorBlock extends Block {
    public static final String NAME = "inf_power_generator";

    public InfPowerGeneratorBlock() {
        super(Properties.of(Material.METAL)
                .strength(14.f, .0f)
                .noDrops()
                .harvestLevel(3)
                .noOcclusion());
    }

    @Override
    public boolean hasTileEntity(BlockState state) {
        return true;
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return BlockEntityRegistry.INF_POWER_GENERATOR.get().create();
    }
}