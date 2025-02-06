package cn.crtlprototypestudios.spos.network.packet;

import cn.crtlprototypestudios.spos.Spos;
import cn.crtlprototypestudios.spos.client.SposClient;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

// network/TpaRequestCancelPacket.java
public class TpaRequestCancelPacket {
    private final UUID requestId;

    public TpaRequestCancelPacket(UUID requestId) {
        this.requestId = requestId;
    }

    public static void encode(TpaRequestCancelPacket msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.requestId);
    }

    public static TpaRequestCancelPacket decode(FriendlyByteBuf buf) {
        return new TpaRequestCancelPacket(buf.readUUID());
    }

    public static void handle(TpaRequestCancelPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // We're on the client
            SposClient.TPA_NOTIF_HUD.removeNotification(msg.requestId);
        });
        ctx.get().setPacketHandled(true);
    }
}

