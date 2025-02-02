package cn.crtlprototypestudios.spos.manager;

import cn.crtlprototypestudios.spos.data.TeleportRequest;
import cn.crtlprototypestudios.spos.handler.PacketHandler;
import cn.crtlprototypestudios.spos.handler.TeleportHandler;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

import java.util.*;

public class TeleportManager {
    private static final Map<UUID, TeleportRequest> activeRequests = new HashMap<>();
    private static final Map<UUID, Location> lastLocations = new HashMap<>();
    private static final Map<UUID, Location> deathLocations = new HashMap<>();

    public static class Location {
        public final ResourceKey<Level> dimension;
        public final double x, y, z;
        public final float yRot, xRot;

        public Location(ResourceKey<Level> dimension, double x, double y, double z, float yRot, float xRot) {
            this.dimension = dimension;
            this.x = x;
            this.y = y;
            this.z = z;
            this.yRot = yRot;
            this.xRot = xRot;
        }

        public static Location fromEntity(ServerPlayer player) {
            return new Location(
                    player.level().dimension(),
                    player.getX(),
                    player.getY(),
                    player.getZ(),
                    player.getYRot(),
                    player.getXRot()
            );
        }
    }

    public static void createRequest(UUID from, UUID to, boolean isToRequest, ServerLevel level) {
        MinecraftServer server = level.getServer();
        ServerPlayer targetPlayer = server.getPlayerList().getPlayer(to);
        if (targetPlayer != null) {
            TeleportRequest request = new TeleportRequest(from, to, isToRequest);
            activeRequests.put(to, request);
            PacketHandler.sendTpaRequest(
                    targetPlayer,
                    request.getId(), // Add an ID field to TeleportRequest
                    Objects.requireNonNull(server.getPlayerList().getPlayer(from)).getGameProfile().getName(),
                    isToRequest,
                    System.currentTimeMillis() + 30000 // 30 seconds expiration
            );
        }
    }

    public static void removeRequest(UUID to, ServerLevel level) {
        TeleportRequest request = activeRequests.remove(to);
        if (request != null) {
            MinecraftServer server = level.getServer();
            ServerPlayer targetPlayer = server.getPlayerList().getPlayer(to);
            if (targetPlayer != null) {
                PacketHandler.sendTpaCancel(targetPlayer, request.getId());
            }
        }
    }

    public static Optional<TeleportRequest> getRequest(UUID to) {
        TeleportRequest request = activeRequests.get(to);
        if (request != null && request.isExpired()) {
            activeRequests.remove(to);
            return Optional.empty();
        }
        return Optional.ofNullable(request);
    }

    public static void saveLastLocation(ServerPlayer player) {
        lastLocations.put(player.getUUID(), Location.fromEntity(player));
    }

    public static void saveDeathLocation(ServerPlayer player) {
        deathLocations.put(player.getUUID(), Location.fromEntity(player));
    }

    public static Optional<Location> getLastLocation(UUID player) {
        return Optional.ofNullable(lastLocations.get(player));
    }

    public static Optional<Location> getDeathLocation(UUID player) {
        return Optional.ofNullable(deathLocations.get(player));
    }

    public static Location clearLastLocation(UUID player) {
        return lastLocations.remove(player);
    }

    public static Location clearDeathLocation(UUID player) {
        return deathLocations.remove(player);
    }

    public static void teleportInstant(ServerPlayer player, Location location) {
        teleportInstant(player, location, true);
    }

    public static void teleportInstant(ServerPlayer player, Location location, boolean saveLastLocation) {
        if (saveLastLocation) saveLastLocation(player);
        ServerLevel targetLevel = Objects.requireNonNull(player.getServer()).getLevel(location.dimension);
        if (targetLevel != null) {
            player.teleportTo(targetLevel, location.x, location.y, location.z, location.yRot, location.xRot);
        }
    }

    public static void teleport(ServerPlayer player, Location location) {
        teleport(player, location, true);
    }

    public static void teleport(ServerPlayer player, Location location, boolean saveLastLocation) {
        TeleportHandler.scheduleTeleport(player, location, saveLastLocation);
    }

    public static Collection<TeleportRequest> getAllRequests() {
        return activeRequests.values();
    }
}

