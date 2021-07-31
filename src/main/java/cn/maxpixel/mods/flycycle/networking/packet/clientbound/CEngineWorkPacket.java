package cn.maxpixel.mods.flycycle.networking.packet.clientbound;

import cn.maxpixel.mods.flycycle.event.player.EngineWorkEvent;
import cn.maxpixel.mods.flycycle.networking.NetworkManager;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class CEngineWorkPacket {
    public static void register() {
        NetworkManager.registerMessage(CEngineWorkPacket.class,
                CEngineWorkPacket::encode,
                CEngineWorkPacket::decode,
                CEngineWorkPacket::handle,
                NetworkDirection.PLAY_TO_SERVER);
    }

    private final int id;
    private final int slot;
    private final boolean work;

    public CEngineWorkPacket(int id, int slot, boolean work) {
        this.id = id;
        this.slot = slot;
        this.work = work;
    }

    public void encode(PacketBuffer packetBuffer) {
        packetBuffer.writeVarInt(id)
                .writeVarInt(slot)
                .writeBoolean(work);
    }

    public static CEngineWorkPacket decode(PacketBuffer packetBuffer) {
        return new CEngineWorkPacket(packetBuffer.readVarInt(), packetBuffer.readVarInt(), packetBuffer.readBoolean());
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context ctx = contextSupplier.get();
        ctx.enqueueWork(() -> {
            if(ctx.getDirection() == NetworkDirection.PLAY_TO_SERVER &&
                    ctx.getSender() != null &&
                    ctx.getSender().getId() == id) {
                MinecraftForge.EVENT_BUS.post(new EngineWorkEvent(ctx.getSender(), slot, work));
            }
        }).whenComplete((ret, err) -> ctx.setPacketHandled(true));
    }
}