package cn.maxpixel.mods.flycycle;

import it.unimi.dsi.fastutil.objects.ObjectOpenHashSet;
import net.minecraft.client.GameSettings;
import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.commons.lang3.ArrayUtils;
import org.lwjgl.glfw.GLFW;

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

    public static void register(FMLJavaModLoadingContext modLoadingContext) {
        modLoadingContext.getModEventBus().addListener((FMLClientSetupEvent event) -> {
            GameSettings options = event.getMinecraftSupplier().get().options;
            options.keyMappings = ArrayUtils.addAll(options.keyMappings, KEY_BINDINGS.toArray(new KeyBinding[0]));
        });
    }
}