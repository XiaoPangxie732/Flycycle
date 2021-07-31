package cn.maxpixel.mods.flycycle.event.player;

import net.minecraft.client.entity.player.AbstractClientPlayerEntity;
import net.minecraftforge.event.entity.player.PlayerEvent;

public class AnimationStateChangedEvent extends PlayerEvent {
    public final byte state;
    public final int slot;

    public AnimationStateChangedEvent(AbstractClientPlayerEntity player, int slot, byte state) {
        super(player);
        this.slot = slot;
        this.state = state;
    }
}