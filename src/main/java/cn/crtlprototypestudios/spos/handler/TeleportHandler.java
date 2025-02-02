package cn.crtlprototypestudios.spos.handler;

import cn.crtlprototypestudios.spos.Config;
import cn.crtlprototypestudios.spos.manager.TeleportManager;
import cn.crtlprototypestudios.spos.utility.LocalizationHelper;
import net.minecraft.server.TickTask;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TeleportHandler {
    private static final Map<UUID, TeleportTask> pendingTeleports = new HashMap<>();

    private static class TeleportTask {
        final TeleportManager.Location destination;
        final Vec3 originalPosition;
        final boolean saveLastLocation;
        final long scheduledTime;

        TeleportTask(TeleportManager.Location destination, Vec3 originalPosition, boolean saveLastLocation) {
            this.destination = destination;
            this.originalPosition = originalPosition;
            this.saveLastLocation = saveLastLocation;
            this.scheduledTime = System.currentTimeMillis() + (Config.stayStillDuration * 1000L);
        }

        boolean isTimeToTeleport() {
            return System.currentTimeMillis() >= scheduledTime;
        }
    }

    public static void scheduleTeleport(ServerPlayer player, TeleportManager.Location destination, boolean saveLastLocation) {
        if (!Config.enableTeleportDelay) {
            // Instant teleport if delay is disabled
            TeleportManager.teleportInstant(player, destination);
            player.sendSystemMessage(LocalizationHelper.getComponent("teleport.success"));
            return;
        }

        cancelPendingTeleport(player);

        Vec3 originalPos = player.position();
        pendingTeleports.put(player.getUUID(), new TeleportTask(destination, originalPos, saveLastLocation));
        player.sendSystemMessage(LocalizationHelper.getComponent("teleport.wait", Config.stayStillDuration));
    }

    public static void cancelPendingTeleport(ServerPlayer player) {
        pendingTeleports.remove(player.getUUID());
    }

    public static void checkPlayerMovement(ServerPlayer player) {
        TeleportTask task = pendingTeleports.get(player.getUUID());
        if (task != null) {
            Vec3 currentPos = player.position();
            if (hasPlayerMoved(currentPos, task.originalPosition)) {
                cancelPendingTeleport(player);
                player.sendSystemMessage(LocalizationHelper.getComponent("teleport.cancelled"));
            } else if (task.isTimeToTeleport()) {
                // Time to teleport!
                pendingTeleports.remove(player.getUUID());
                TeleportManager.teleportInstant(player, task.destination, task.saveLastLocation);
                player.sendSystemMessage(LocalizationHelper.getComponent("teleport.success"));
            }
        }
    }

    private static boolean hasPlayerMoved(Vec3 current, Vec3 original) {
        double threshold = 0.1; // Small threshold to account for floating-point inaccuracies
        return Math.abs(current.x - original.x) > threshold ||
                Math.abs(current.y - original.y) > threshold ||
                Math.abs(current.z - original.z) > threshold;
    }
}

