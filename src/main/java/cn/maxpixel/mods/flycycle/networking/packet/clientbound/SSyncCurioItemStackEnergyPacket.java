package cn.maxpixel.mods.flycycle.networking.packet.clientbound;

import cn.maxpixel.mods.flycycle.item.FlycycleItem;
import cn.maxpixel.mods.flycycle.networking.NetworkManager;
import net.minecraft.client.Minecraft;
import net.minecraft.network.PacketBuffer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.energy.CapabilityEnergy;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotTypePreset;
import top.theillusivec4.curios.api.type.inventory.ICurioStacksHandler;

import java.util.function.Supplier;

public class SSyncCurioItemStackEnergyPacket {
    public static void register() {
        NetworkManager.registerMessage(SSyncCurioItemStackEnergyPacket.class,
                SSyncCurioItemStackEnergyPacket::encode,
                SSyncCurioItemStackEnergyPacket::decode,
                SSyncCurioItemStackEnergyPacket::handle,
                NetworkDirection.PLAY_TO_CLIENT);
    }

    private final int id;
    private final int slot;
    private final int energy;

    public SSyncCurioItemStackEnergyPacket(int id, int slot, int energy) {
        this.id = id;
        this.slot = slot;
        this.energy = energy;
    }

    public void encode(PacketBuffer packetBuffer) {
        packetBuffer.writeVarInt(id)
                .writeVarInt(slot)
                .writeVarInt(energy);
    }

    public static SSyncCurioItemStackEnergyPacket decode(PacketBuffer packetBuffer) {
        return new SSyncCurioItemStackEnergyPacket(packetBuffer.readVarInt(), packetBuffer.readVarInt(), packetBuffer.readVarInt());
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
        if(Minecraft.getInstance().player != null && Minecraft.getInstance().player.getId() == id) {
            CuriosApi.getCuriosHelper()
                    .getCuriosHandler(Minecraft.getInstance().player)
                    .map(handler -> handler.getCurios().get(SlotTypePreset.BACK.getIdentifier()))
                    .map(ICurioStacksHandler::getStacks)
                    .map(handler -> handler.getStackInSlot(slot))
                    .flatMap(stack -> stack
                            .getCapability(CapabilityEnergy.ENERGY)
                            .filter(FlycycleItem.ChangeableEnergyStorage.class::isInstance)
                            .map(FlycycleItem.ChangeableEnergyStorage.class::cast)
                    ).ifPresent(storage -> storage.setEnergy(energy));
        }
    }
}