package cn.maxpixel.mods.flycycle.block;

import cn.maxpixel.mods.flycycle.block.entity.BlockEntityRegistry;
import mcp.MethodsReturnNonnullByDefault;
import net.minecraft.block.Block;
import net.minecraft.block.BlockState;
import net.minecraft.block.material.Material;
import net.minecraft.block.material.PushReaction;
import net.minecraft.fluid.FluidState;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.*;

import javax.annotation.Nullable;
import javax.annotation.ParametersAreNonnullByDefault;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@MethodsReturnNonnullByDefault
@ParametersAreNonnullByDefault
public class InfPowerGeneratorBlock extends Block {
    public static final String NAME = "inf_power_generator";
    private volatile boolean hasBlockEntity;

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

    @Override
    public PushReaction getPistonPushReaction(BlockState p_149656_1_) {
        return PushReaction.BLOCK;
    }

    @Override
    public List<ItemStack> getDrops(BlockState p_220076_1_, LootContext.Builder p_220076_2_) {
        return Collections.emptyList();
    }

    private void createExplosion(World level, BlockPos pos) {
        level.explode(null, null, new ExplosionContext() {
            @Override
            public Optional<Float> getBlockExplosionResistance(Explosion p_230312_1_, IBlockReader p_230312_2_, BlockPos p_230312_3_, BlockState p_230312_4_, FluidState p_230312_5_) {
                return Optional.of(-0.6f);
            }

            @Override
            public boolean shouldBlockExplode(Explosion p_230311_1_, IBlockReader p_230311_2_, BlockPos p_230311_3_, BlockState p_230311_4_, float p_230311_5_) {
                return true;
            }
        }, pos.getX() + 0.5d, pos.getY() + 0.5d, pos.getZ() + 0.5d, 100.f, true, Explosion.Mode.DESTROY);
    }

    @Override
    public boolean canDropFromExplosion(BlockState state, IBlockReader world, BlockPos pos, Explosion explosion) {
        return false;
    }

    @Override
    public void wasExploded(World level, BlockPos pos, Explosion explosion) {
        createExplosion(level, pos);
    }

    @Override
    public void destroy(IWorld level, BlockPos pos, BlockState p_176206_3_) {
        createExplosion((World) level, pos);
    }

    @Nullable
    @Override
    public TileEntity createTileEntity(BlockState state, IBlockReader world) {
        return BlockEntityRegistry.INF_POWER_GENERATOR.get().create();
    }
}