package cn.crtlprototypestudios.spos.network.packet;

import cn.crtlprototypestudios.spos.Spos;
import cn.crtlprototypestudios.spos.client.SposClient;
import cn.crtlprototypestudios.spos.client.data.TpaNotification;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
import java.util.function.Supplier;

public class TpaRequestPacket {
    private final UUID requestId;
    private final String requesterName;
    private final boolean isToRequest;
    private final long expirationTime;

    public TpaRequestPacket(UUID requestId, String requesterName, boolean isToRequest, long expirationTime) {
        this.requestId = requestId;
        this.requesterName = requesterName;
        this.isToRequest = isToRequest;
        this.expirationTime = expirationTime;
    }

    public static void encode(TpaRequestPacket msg, FriendlyByteBuf buf) {
        buf.writeUUID(msg.requestId);
        buf.writeUtf(msg.requesterName);
        buf.writeBoolean(msg.isToRequest);
        buf.writeLong(msg.expirationTime);
    }

    public static TpaRequestPacket decode(FriendlyByteBuf buf) {
        return new TpaRequestPacket(
                buf.readUUID(),
                buf.readUtf(),
                buf.readBoolean(),
                buf.readLong()
        );
    }

    public static void handle(TpaRequestPacket msg, Supplier<NetworkEvent.Context> ctx) {
        ctx.get().enqueueWork(() -> {
            // We're on the client
            SposClient.TPA_NOTIF_HUD.addNotification(
                    new TpaNotification(
                    msg.requestId,
                    msg.requesterName,
                    msg.isToRequest,
                    msg.expirationTime
            ));
        });
        ctx.get().setPacketHandled(true);
    }

    public UUID getId(){
        return requestId;
    }
}
