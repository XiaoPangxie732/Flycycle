package cn.maxpixel.mods.flycycle.networking.packet.serverbound;

import cn.maxpixel.mods.flycycle.event.player.AnimationStateChangedEvent;
import cn.maxpixel.mods.flycycle.networking.NetworkManager;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class SBroadcastAnimationStateChangedPacket {
    public static void register() {
        NetworkManager.registerMessage(SBroadcastAnimationStateChangedPacket.class,
                SBroadcastAnimationStateChangedPacket::encode,
                SBroadcastAnimationStateChangedPacket::decode,
                SBroadcastAnimationStateChangedPacket::handle,
                NetworkDirection.PLAY_TO_CLIENT);
    }

    private final int id;
    private final int slot;
    private final byte state;

    public SBroadcastAnimationStateChangedPacket(int id, int slot, byte state) {
        this.id = id;
        this.slot = slot;
        this.state = state;
    }

    public void encode(PacketBuffer packetBuffer) {
        packetBuffer.writeVarInt(id)
                .writeVarInt(slot)
                .writeByte(state);
    }

    public static SBroadcastAnimationStateChangedPacket decode(PacketBuffer packetBuffer) {
        return new SBroadcastAnimationStateChangedPacket(packetBuffer.readVarInt(), packetBuffer.readVarInt(), packetBuffer.readByte());
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context ctx = contextSupplier.get();
        ctx.enqueueWork(() -> {
            if(FMLEnvironment.dist.isClient() && ctx.getDirection() == NetworkDirection.PLAY_TO_CLIENT) {
                clientHandle();
            }
        }).whenComplete((ret, err) -> ctx.setPacketHandled(true));
    }

    @OnlyIn(Dist.CLIENT)
    private void clientHandle() {
        Minecraft.getInstance().getConnection().getLevel().players().forEach(player -> {
            if(player.getId() == id) {
                MinecraftForge.EVENT_BUS.post(new AnimationStateChangedEvent(player, slot, state));
            }
        });
    }
}