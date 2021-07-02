package cn.maxpixel.mods.flycycle;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.client.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import org.apache.commons.lang3.ArrayUtils;
import org.lwjgl.glfw.GLFW;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = Flycycle.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class KeyBindings {
    public static final String CATEGORY = "key." + Flycycle.MODID + ".categories";
    public static final String KEY_PREFIX = "key." + Flycycle.MODID + '.';

    private static final ObjectOpenHashSet<KeyBinding> KEY_BINDINGS = new ObjectOpenHashSet<>();
    private static KeyBinding makeKeyBinding(String name, int key) {
        KeyBinding keyBinding = new KeyBinding(KEY_PREFIX.concat(name), key, CATEGORY);
        KEY_BINDINGS.add(keyBinding);
        return keyBinding;
    }

    public static final KeyBinding KEY_FLY = makeKeyBinding("fly", GLFW.GLFW_KEY_SPACE);
    public static final KeyBinding KEY_TOGGLE_ENGINE = makeKeyBinding("toggle_engine", GLFW.GLFW_KEY_G);

    @SubscribeEvent
    public static void register(FMLClientSetupEvent event) {
        GameSettings options = event.getMinecraftSupplier().get().options;
        options.keyMappings = ArrayUtils.addAll(options.keyMappings, KEY_BINDINGS.toArray(new KeyBinding[0]));
    }
}