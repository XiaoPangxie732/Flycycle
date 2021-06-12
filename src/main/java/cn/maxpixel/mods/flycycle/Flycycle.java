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

import cn.maxpixel.mods.flycycle.item.ItemRegistry;
import net.minecraft.item.ItemGroup;
import net.minecraft.item.ItemStack;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.InterModComms;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.InterModEnqueueEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotTypeMessage;
import top.theillusivec4.curios.api.SlotTypePreset;

@Mod(Flycycle.MODID)
@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)
public class Flycycle {
    private static final Logger LOGGER = LogManager.getLogger("Flycycle");
    public static final String MODID = "flycycle";
    public static final ItemGroup ITEM_GROUP = new ItemGroup(MODID) {
        @Override
        @OnlyIn(Dist.CLIENT)
        public ItemStack makeIcon() {
            return new ItemStack(ItemRegistry.FLYCYCLE.get());
        }
    };

    public Flycycle() {
        Registries.register(FMLJavaModLoadingContext.get());
    }

    @SubscribeEvent
    public static void enqueue(InterModEnqueueEvent enqueueEvent) {
        try {
            InterModComms.sendTo(CuriosApi.MODID, SlotTypeMessage.REGISTER_TYPE, SlotTypePreset.BACK.getMessageBuilder()::build);
            LOGGER.info("Found Curios API.");
        } catch(NoClassDefFoundError ignored) {}
    }

    public static Logger getLogger() {
        return LOGGER;
    }
}