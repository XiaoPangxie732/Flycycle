package cn.maxpixel.mods.flycycle.item;

import cn.maxpixel.mods.flycycle.Flycycle;
import net.minecraft.item.Item;

public class FlycycleItem extends Item {
    public static final String NAME = "flycycle";
    public FlycycleItem() {
        super(new Properties()
                .defaultDurability(0)
                .durability(200)
                .setNoRepair()
                .tab(Flycycle.ITEM_GROUP));
    }
}