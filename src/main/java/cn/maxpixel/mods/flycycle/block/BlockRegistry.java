/*
 * Flycycle. A Minecraft mod.
 * Copyright (C) 2021  MaxPixel Studios Development
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */

package cn.maxpixel.mods.flycycle.block;

import cn.maxpixel.mods.flycycle.Flycycle;
import cn.maxpixel.mods.flycycle.block.entity.BlockEntityRegistry;
import cn.maxpixel.mods.flycycle.item.ItemRegistry;
import net.minecraft.block.Block;
import net.minecraft.item.Rarity;
import net.minecraftforge.fml.RegistryObject;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;

import java.util.function.Supplier;

public class BlockRegistry {
    private static final DeferredRegister<Block> BLOCKS = DeferredRegister.create(ForgeRegistries.BLOCKS, Flycycle.MODID);

    public static final RegistryObject<InfPowerGeneratorBlock> INF_POWER_GENERATOR = registerWithItem(InfPowerGeneratorBlock.NAME, InfPowerGeneratorBlock::new, Rarity.UNCOMMON);

    private static <T extends Block> RegistryObject<T> registerWithItem(String name, Supplier<T> sup) {
        return ItemRegistry.registerBlock(BLOCKS.register(name, sup));
    }

    private static <T extends Block> RegistryObject<T> registerWithItem(String name, Supplier<T> sup, Rarity rarity) {
        return ItemRegistry.registerBlock(BLOCKS.register(name, sup), rarity);
    }

    public static void register(FMLJavaModLoadingContext modLoadingContext) {
        BLOCKS.register(modLoadingContext.getModEventBus());
        BlockEntityRegistry.register(modLoadingContext);
    }
}