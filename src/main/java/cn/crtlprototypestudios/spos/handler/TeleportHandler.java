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
        final int taskId;

        TeleportTask(TeleportManager.Location destination, Vec3 originalPosition, int taskId) {
            this.destination = destination;
            this.originalPosition = originalPosition;
            this.taskId = taskId;
        }
    }

    public static void scheduleTeleport(ServerPlayer player, TeleportManager.Location destination) {
        cancelPendingTeleport(player);

        Vec3 originalPos = player.position();
        int taskId = player.getServer().getTickCount();

        pendingTeleports.put(player.getUUID(), new TeleportTask(destination, originalPos, taskId));

        player.sendSystemMessage(LocalizationHelper.getComponent("teleport.wait", Config.stayStillDuration));

        // Schedule the actual teleport
        player.getServer().tell(new TickTask(player.getServer().getTickCount() + (Config.stayStillDuration * 20), () -> {
            if (checkAndRemoveTeleportTask(player, taskId)) {
                TeleportManager.teleport(player, destination);
                player.sendSystemMessage(LocalizationHelper.getComponent("teleport.success"));
            }
        }));
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
            }
        }
    }

    private static boolean checkAndRemoveTeleportTask(ServerPlayer player, int taskId) {
        TeleportTask task = pendingTeleports.get(player.getUUID());
        if (task != null && task.taskId == taskId) {
            pendingTeleports.remove(player.getUUID());
            return true;
        }
        return false;
    }

    private static boolean hasPlayerMoved(Vec3 current, Vec3 original) {
        double threshold = 0.1; // Small threshold to account for floating-point inaccuracies
        return Math.abs(current.x - original.x) > threshold ||
                Math.abs(current.y - original.y) > threshold ||
                Math.abs(current.z - original.z) > threshold;
    }
}

