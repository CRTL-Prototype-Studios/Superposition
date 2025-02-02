package cn.crtlprototypestudios.spos.handler;

import cn.crtlprototypestudios.spos.Spos;
import cn.crtlprototypestudios.spos.network.packet.TpaRequestCancelPacket;
import cn.crtlprototypestudios.spos.network.packet.TpaRequestPacket;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.network.NetworkRegistry;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.UUID;

public class PacketHandler {
    private static final String PROTOCOL_VERSION = "1";
    public static final SimpleChannel INSTANCE = NetworkRegistry.newSimpleChannel(
            new ResourceLocation(Spos.MODID, "main"),
            () -> PROTOCOL_VERSION,
            PROTOCOL_VERSION::equals,
            PROTOCOL_VERSION::equals
    );

    public static void register() {
        int id = 0;
        INSTANCE.registerMessage(
                id++,
                TpaRequestPacket.class,
                TpaRequestPacket::encode,
                TpaRequestPacket::decode,
                TpaRequestPacket::handle
        );
        INSTANCE.registerMessage(
                id++,
                TpaRequestCancelPacket.class,
                TpaRequestCancelPacket::encode,
                TpaRequestCancelPacket::decode,
                TpaRequestCancelPacket::handle
        );
    }

    public static void sendTpaRequest(ServerPlayer target, UUID requestId, String requesterName, boolean isToRequest, long expirationTime) {
        INSTANCE.send(
                PacketDistributor.PLAYER.with(() -> target),
                new TpaRequestPacket(requestId, requesterName, isToRequest, expirationTime)
        );
    }

    public static void sendTpaCancel(ServerPlayer target, UUID requestId) {
        INSTANCE.send(
                PacketDistributor.PLAYER.with(() -> target),
                new TpaRequestCancelPacket(requestId)
        );
    }
}

