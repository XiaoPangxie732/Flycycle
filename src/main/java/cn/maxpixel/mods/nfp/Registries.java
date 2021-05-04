package cn.maxpixel.mods.nfp;

import cn.maxpixel.mods.nfp.block.BlockRegistry;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

public class Registries {
    public static void register(FMLJavaModLoadingContext modLoadingContext) {
        NuclearFusionPower.getLogger().info("Registering stuffs");
        BlockRegistry.register(modLoadingContext);
        NuclearFusionPower.getLogger().info("Registering stuffs - completed");
    }
}