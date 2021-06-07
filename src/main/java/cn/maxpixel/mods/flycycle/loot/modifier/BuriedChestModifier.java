package cn.maxpixel.mods.flycycle.loot.modifier;

import cn.maxpixel.mods.flycycle.block.BlockRegistry;
import com.google.gson.JsonObject;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootContext;
import net.minecraft.loot.LootTables;
import net.minecraft.loot.conditions.ILootCondition;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.common.loot.GlobalLootModifierSerializer;
import net.minecraftforge.common.loot.LootModifier;

import javax.annotation.Nonnull;
import java.util.List;

public class BuriedChestModifier extends LootModifier {
    protected BuriedChestModifier(ILootCondition[] conditionsIn) {
        super(conditionsIn);
    }

    @Nonnull
    @Override
    protected List<ItemStack> doApply(List<ItemStack> generatedLoot, LootContext context) {
        if(context.getQueriedLootTableId().equals(LootTables.BURIED_TREASURE))
            generatedLoot.add(new ItemStack(BlockRegistry.INF_POWER_GENERATOR.get(), 1));
        return generatedLoot;
    }

    public static class Serializer extends GlobalLootModifierSerializer<BuriedChestModifier> {
        @Override
        public BuriedChestModifier read(ResourceLocation location, JsonObject object, ILootCondition[] ailootcondition) {
            return new BuriedChestModifier(ailootcondition);
        }

        @Override
        public JsonObject write(BuriedChestModifier instance) {
            return makeConditions(instance.conditions);
        }
    }
}