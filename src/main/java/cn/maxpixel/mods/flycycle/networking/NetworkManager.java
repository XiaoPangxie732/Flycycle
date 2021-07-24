package cn.maxpixel.mods.flycycle.networking;

import cn.maxpixel.mods.flycycle.Flycycle;
import cn.maxpixel.mods.flycycle.networking.packet.clientbound.CAnimationStateChangedPacket;
import cn.maxpixel.mods.flycycle.networking.packet.clientbound.CSyncCurioItemStackEnergyPacket;
import cn.maxpixel.mods.flycycle.networking.packet.clientbound.CSyncItemStackEnergyPacket;
import cn.maxpixel.mods.flycycle.networking.packet.serverbound.SBroadcastAnimationStateChangedPacket;
import cn.maxpixel.mods.flycycle.networking.packet.serverbound.SSyncCurioItemStackEnergyPacket;
import cn.maxpixel.mods.flycycle.networking.packet.serverbound.SSyncItemStackEnergyPacket;
import net.minecraft.network.PacketBuffer;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.fml.network.NetworkDirection;
import net.minecraftforge.fml.network.NetworkEvent;
import net.minecraftforge.fml.network.NetworkRegistry;
import net.minecraftforge.fml.network.PacketDistributor;
import net.minecraftforge.fml.network.simple.SimpleChannel;

import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

public class NetworkManager {
    public static void init() {
        SSyncItemStackEnergyPacket.register();
        SSyncCurioItemStackEnergyPacket.register();
        SBroadcastAnimationStateChangedPacket.register();

        CSyncItemStackEnergyPacket.register();
        CSyncCurioItemStackEnergyPacket.register();
        CAnimationStateChangedPacket.register();
    }
    private static final String PROTOCOL_VERSION = "1";
    private static int messageIndex = 0;
    private static final SimpleChannel CHANNEL = NetworkRegistry.ChannelBuilder
            .named(new ResourceLocation(Flycycle.MODID, "simple_network_channel"))
            .networkProtocolVersion(()->PROTOCOL_VERSION)
            .clientAcceptedVersions(PROTOCOL_VERSION::equals)
            .serverAcceptedVersions(PROTOCOL_VERSION::equals)
            .simpleChannel();

    public static <MSG> void registerMessage(Class<MSG> messageType,
                                             BiConsumer<MSG, PacketBuffer> encoder,
                                             Function<PacketBuffer, MSG> decoder,
                                             BiConsumer<MSG, Supplier<NetworkEvent.Context>> messageConsumer) {
        CHANNEL.registerMessage(messageIndex++, messageType, encoder, decoder, messageConsumer);
    }

    public static <MSG> void registerMessage(Class<MSG> messageType,
                                             BiConsumer<MSG, PacketBuffer> encoder,
                                             Function<PacketBuffer, MSG> decoder,
                                             BiConsumer<MSG, Supplier<NetworkEvent.Context>> messageConsumer,
                                             NetworkDirection networkDirection) {
        CHANNEL.registerMessage(messageIndex++, messageType, encoder, decoder, messageConsumer, Optional.ofNullable(networkDirection));
    }

    public static <MSG> void send(PacketDistributor.PacketTarget target, MSG message) {
        CHANNEL.send(target, message);
    }

    public static <MSG> void sendToServer(MSG message) {
        CHANNEL.sendToServer(message);
    }
}