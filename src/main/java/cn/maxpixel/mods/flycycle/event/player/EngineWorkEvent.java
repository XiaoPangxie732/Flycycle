package cn.maxpixel.mods.flycycle.event.player;

import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraftforge.event.entity.player.PlayerEvent;

public class EngineWorkEvent extends PlayerEvent {
    public final int slot;
    public final boolean work;

    public EngineWorkEvent(ServerPlayerEntity player, int slot, boolean work) {
        super(player);
        this.slot = slot;
        this.work = work;
    }
}