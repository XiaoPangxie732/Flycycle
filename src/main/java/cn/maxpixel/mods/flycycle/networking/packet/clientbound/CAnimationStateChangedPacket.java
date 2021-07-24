package cn.maxpixel.mods.flycycle.networking.packet.clientbound;

import cn.maxpixel.mods.flycycle.event.player.AnimationStateChangedEvent;
import cn.maxpixel.mods.flycycle.networking.NetworkManager;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class CAnimationStateChangedPacket {
    public static void register() {
        NetworkManager.registerMessage(CAnimationStateChangedPacket.class,
                CAnimationStateChangedPacket::encode,
                CAnimationStateChangedPacket::decode,
                CAnimationStateChangedPacket::handle,
                NetworkDirection.PLAY_TO_SERVER);
    }

    private final int id;
    private final int slot;
    private final byte state;

    public CAnimationStateChangedPacket(int id, int slot, byte state) {
        this.id = id;
        this.slot = slot;
        this.state = state;
    }

    public void encode(PacketBuffer packetBuffer) {
        packetBuffer.writeVarInt(id)
                .writeVarInt(slot)
                .writeByte(state);
    }

    public static CAnimationStateChangedPacket decode(PacketBuffer packetBuffer) {
        return new CAnimationStateChangedPacket(packetBuffer.readVarInt(), packetBuffer.readVarInt(), packetBuffer.readByte());
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context ctx = contextSupplier.get();
        ctx.enqueueWork(() -> {
            if(ctx.getDirection() == NetworkDirection.PLAY_TO_SERVER &&
                    ctx.getSender() != null &&
                    ctx.getSender().getId() == id) {
                MinecraftForge.EVENT_BUS.post(new AnimationStateChangedEvent(ctx.getSender(), slot, state));
            }
        }).whenComplete((ret, err) -> ctx.setPacketHandled(true));
    }
}