package cn.maxpixel.mods.flycycle.networking.packet.clientbound;

import cn.maxpixel.mods.flycycle.item.FlycycleItem;
import cn.maxpixel.mods.flycycle.networking.NetworkManager;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotTypePreset;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

import java.util.function.Supplier;

public class CSyncCurioItemStackEnergyPacket {
    static {
        NetworkManager.registerMessage(CSyncCurioItemStackEnergyPacket.class,
                CSyncCurioItemStackEnergyPacket::encode,
                CSyncCurioItemStackEnergyPacket::decode,
                CSyncCurioItemStackEnergyPacket::handle,
                NetworkDirection.PLAY_TO_SERVER);
    }

    private final int id;
    private final int slot;
    private final int energy;

    public CSyncCurioItemStackEnergyPacket(int id, int slot, int energy) {
        this.id = id;
        this.slot = slot;
        this.energy = energy;
    }

    public void encode(PacketBuffer packetBuffer) {
        packetBuffer.writeVarInt(id)
                .writeVarInt(slot)
                .writeVarInt(energy);
    }

    public static CSyncCurioItemStackEnergyPacket decode(PacketBuffer packetBuffer) {
        return new CSyncCurioItemStackEnergyPacket(packetBuffer.readVarInt(), packetBuffer.readVarInt(), packetBuffer.readVarInt());
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        NetworkEvent.Context ctx = contextSupplier.get();
        ctx.enqueueWork(() -> {
            if(ctx.getDirection() == NetworkDirection.PLAY_TO_SERVER &&
                    ctx.getSender() != null &&
                    ctx.getSender().getId() == id) {
                CuriosApi.getCuriosHelper()
                        .getCuriosHandler(ctx.getSender())
                        .map(handler -> handler.getCurios().get(SlotTypePreset.BACK.getIdentifier()))
                        .map(ICurioStacksHandler::getStacks)
                        .map(handler -> handler.getStackInSlot(slot))
                        .flatMap(stack -> stack
                                .getCapability(CapabilityEnergy.ENERGY)
                                .filter(FlycycleItem.ChangeableEnergyStorage.class::isInstance)
                                .map(FlycycleItem.ChangeableEnergyStorage.class::cast)
                        ).ifPresent(storage -> storage.setEnergy(energy));
            }
        }).whenComplete((ret, err) -> ctx.setPacketHandled(true));
    }
}