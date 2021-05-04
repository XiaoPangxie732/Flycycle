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

import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod("flycycle")
public class Flycycle {
    public static final String MODID = "flycycle";
    private static final Logger LOGGER = LogManager.getLogger("Flycycle");

    public Flycycle() {
        Registries.register(FMLJavaModLoadingContext.get());
    }

    public static Logger getLogger() {
        return LOGGER;
    }
}