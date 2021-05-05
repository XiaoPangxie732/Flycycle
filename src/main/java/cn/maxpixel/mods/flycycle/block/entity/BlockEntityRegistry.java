package cn.maxpixel.mods.flycycle.block.entity;

import cn.maxpixel.mods.flycycle.Flycycle;
import cn.maxpixel.mods.flycycle.block.BlockRegistry;
import cn.maxpixel.mods.flycycle.block.InfPowerGeneratorBlock;
import com.mojang.datafixers.DSL;
import net.minecraft.tileentity.TileEntityType;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class BlockEntityRegistry {
    private static final DeferredRegister<TileEntityType<?>> BLOCK_ENTITIES = DeferredRegister.create(ForgeRegistries.TILE_ENTITIES, Flycycle.MODID);

    public static final RegistryObject<TileEntityType<InfPowerGeneratorBlockEntity>> INF_POWER_GENERATOR = BLOCK_ENTITIES.register(InfPowerGeneratorBlock.NAME, () -> TileEntityType.Builder.of(InfPowerGeneratorBlockEntity::new, BlockRegistry.INF_POWER_GENERATOR.get()).build(DSL.remainderType()));

    public static void register(FMLJavaModLoadingContext modLoadingContext) {
        BLOCK_ENTITIES.register(modLoadingContext.getModEventBus());
    }
}