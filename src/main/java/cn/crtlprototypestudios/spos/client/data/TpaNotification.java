package cn.crtlprototypestudios.spos.client.data;

import java.util.UUID;

public record TpaNotification(UUID requestId, String requesterName, boolean isToRequest, long expirationTime) {

    public boolean isExpired() {
        return System.currentTimeMillis() >= expirationTime;
    }
}
