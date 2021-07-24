package cn.maxpixel.mods.flycycle.event.player;

import net.minecraft.entity.player.PlayerEntity;
import net.minecraftforge.event.entity.player.PlayerEvent;

public class AnimationStateChangedEvent extends PlayerEvent {
    public final byte state;
    public final int slot;

    public AnimationStateChangedEvent(PlayerEntity player, int slot, byte state) {
        super(player);
        this.slot = slot;
        this.state = state;
    }
}