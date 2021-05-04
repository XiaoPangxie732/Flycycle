package cn.maxpixel.mods.nfp;

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod("nfp")
public class NuclearFusionPower {
    public static final String MODID = "nfp";
    private static final Logger LOGGER = LogManager.getLogger("NuclearFusionPower");

    public NuclearFusionPower() {
        Registries.register(FMLJavaModLoadingContext.get());
    }

    public static Logger getLogger() {
        return LOGGER;
    }
}