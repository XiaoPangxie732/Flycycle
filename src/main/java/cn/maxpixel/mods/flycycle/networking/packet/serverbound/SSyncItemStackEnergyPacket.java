package cn.maxpixel.mods.flycycle.networking.packet.serverbound;

import cn.maxpixel.mods.flycycle.item.FlycycleItem;
import cn.maxpixel.mods.flycycle.networking.NetworkManager;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;

import java.util.function.Supplier;

public class SSyncItemStackEnergyPacket {
    static {
        NetworkManager.registerMessage(SSyncItemStackEnergyPacket.class,
                SSyncItemStackEnergyPacket::encode,
                SSyncItemStackEnergyPacket::decode,
                SSyncItemStackEnergyPacket::handle,
                NetworkDirection.PLAY_TO_CLIENT);
    }

    private final int id;
    private final int slot;
    private final int energy;

    public SSyncItemStackEnergyPacket(int id, int slot, int energy) {
        this.id = id;
        this.slot = slot;
        this.energy = energy;
    }

    public void encode(PacketBuffer packetBuffer) {
        packetBuffer.writeVarInt(id)
                .writeVarInt(slot)
                .writeVarInt(energy);
    }

    public static SSyncItemStackEnergyPacket decode(PacketBuffer packetBuffer) {
        return new SSyncItemStackEnergyPacket(packetBuffer.readVarInt(), packetBuffer.readVarInt(), packetBuffer.readVarInt());
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context ctx = contextSupplier.get();
        ctx.enqueueWork(() -> {
            if(ctx.getDirection() == NetworkDirection.PLAY_TO_CLIENT &&
                    Minecraft.getInstance().player != null &&
                    Minecraft.getInstance().player.getId() == id) {
                Minecraft.getInstance().player.inventory.getItem(slot).getCapability(CapabilityEnergy.ENERGY)
                        .filter(FlycycleItem.ChangeableEnergyStorage.class::isInstance)
                        .map(FlycycleItem.ChangeableEnergyStorage.class::cast)
                        .ifPresent(storage -> storage.setEnergy(energy));
            }
        });
        ctx.setPacketHandled(true);
    }
}