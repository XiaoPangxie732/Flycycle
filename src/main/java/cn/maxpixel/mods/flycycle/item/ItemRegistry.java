package cn.maxpixel.mods.flycycle.item;

import cn.maxpixel.mods.flycycle.Flycycle;
import it.unimi.dsi.fastutil.objects.Object2ObjectOpenHashMap;
import net.minecraft.block.Block;
import net.minecraft.item.BlockItem;
import net.minecraft.item.Item;
import net.minecraft.item.Rarity;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

public class ItemRegistry {
    private static final DeferredRegister<Item> ITEMS = DeferredRegister.create(ForgeRegistries.ITEMS, Flycycle.MODID);
    public static final Object2ObjectOpenHashMap<RegistryObject<? extends Block>, RegistryObject<BlockItem>> BLOCK_ITEMS = new Object2ObjectOpenHashMap<>();

    public static final RegistryObject<Item> FLYCYCLE = ITEMS.register(FlycycleItem.NAME, FlycycleItem::new);

    public static void register(FMLJavaModLoadingContext modLoadingContext) {
        ITEMS.register(modLoadingContext.getModEventBus());
    }

    public static <T extends Block> RegistryObject<T> registerBlock(RegistryObject<T> block) {
        return registerBlock(block, Rarity.COMMON);
    }

    public static <T extends Block> RegistryObject<T> registerBlock(RegistryObject<T> block, Rarity rarity) {
        BLOCK_ITEMS.put(block, ITEMS.register(block.getId().getPath(), () -> new BlockItem(block.get(),
                new Item.Properties().rarity(rarity).setNoRepair().tab(Flycycle.ITEM_GROUP))));
        return block;
    }
}