package cn.crtlprototypestudios.spos.data;

import java.util.UUID;

public class TeleportRequest {
    private final UUID id;
    private final UUID from;
    private final UUID to;
    private final boolean isToRequest; // true for /tpa to, false for /tpa from
    private final long timestamp;
    private final long expirationTime;

    public TeleportRequest(UUID from, UUID to, boolean isToRequest, long expirationTime) {
        this.from = from;
        this.to = to;
        this.isToRequest = isToRequest;
        this.timestamp = System.currentTimeMillis();
        this.id = UUID.randomUUID();
        this.expirationTime = expirationTime;
    }

    public boolean isExpired() {
        return System.currentTimeMillis() - timestamp > expirationTime * 1000L;
    }

    // Getters
    public UUID getFrom() { return from; }
    public UUID getTo() { return to; }
    public boolean isToRequest() { return isToRequest; }

    public UUID getId() {return id;}

    public long getExpirationTime() {
        return expirationTime;
    }
}

