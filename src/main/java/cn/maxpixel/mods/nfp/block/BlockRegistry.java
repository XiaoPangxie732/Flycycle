package cn.maxpixel.mods.nfp.block;

import cn.maxpixel.mods.nfp.NuclearFusionPower;
import net.minecraft.block.Block;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class BlockRegistry {
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, NuclearFusionPower.MODID);

    public static void register(FMLJavaModLoadingContext modLoadingContext) {
        BLOCKS.register(modLoadingContext.getModEventBus());
    }
}