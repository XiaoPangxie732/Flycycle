package cn.maxpixel.mods.flycycle;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

@Mod.EventBusSubscriber(modid = Flycycle.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config {
    public static final ForgeConfigSpec SERVER_SPEC;
    public static final ForgeConfigSpec.IntValue INFPOWER_RANGE;
    public static int range;

    static {
        ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();
        INFPOWER_RANGE = builder.defineInRange("infpower.range", 100, 10, 10000);
        SERVER_SPEC = builder.build();
    }

    public static void register(ModLoadingContext ctx) {
        ctx.registerConfig(ModConfig.Type.SERVER, SERVER_SPEC);
    }

    @SubscribeEvent
    public static void onModConfig(ModConfig.ModConfigEvent event) {
        range = INFPOWER_RANGE.get();
    }
}