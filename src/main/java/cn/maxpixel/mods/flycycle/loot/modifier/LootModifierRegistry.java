package cn.maxpixel.mods.flycycle.loot.modifier;

import cn.maxpixel.mods.flycycle.Flycycle;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class LootModifierRegistry {
    public static final DeferredRegister<GlobalLootModifierSerializer<?>> LOOT_MODIFIER_SERIALIZERS = DeferredRegister.create(ForgeRegistries.LOOT_MODIFIER_SERIALIZERS, Flycycle.MODID);

    public static final RegistryObject<BuriedChestModifier.Serializer> BURIED_CHEST_MODIFIER_SERIALIZER = LOOT_MODIFIER_SERIALIZERS.register("buried_treasure", BuriedChestModifier.Serializer::new);

    public static void register(FMLJavaModLoadingContext modLoadingContext) {
        LOOT_MODIFIER_SERIALIZERS.register(modLoadingContext.getModEventBus());
    }
}