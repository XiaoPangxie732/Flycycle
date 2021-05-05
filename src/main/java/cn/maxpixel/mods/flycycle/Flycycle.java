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

package cn.maxpixel.mods.flycycle;

import cn.maxpixel.mods.flycycle.block.BlockRegistry;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod("flycycle")
public class Flycycle {
    private static final Logger LOGGER = LogManager.getLogger("Flycycle");
    public static final String MODID = "flycycle";
    public static final ItemGroup ITEM_GROUP = new ItemGroup(MODID) {
        @Override
        @OnlyIn(Dist.CLIENT)
        public ItemStack makeIcon() {
            return new ItemStack(BlockRegistry.INF_POWER_GENERATOR.get());
        }
    };

    public Flycycle() {
        Registries.register(FMLJavaModLoadingContext.get());
    }

    public static Logger getLogger() {
        return LOGGER;
    }
}