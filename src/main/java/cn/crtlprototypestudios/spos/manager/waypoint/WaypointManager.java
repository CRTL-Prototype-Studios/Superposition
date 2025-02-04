package cn.crtlprototypestudios.spos.manager.waypoint;

import cn.crtlprototypestudios.spos.Spos;
import cn.crtlprototypestudios.spos.data.waypoint.Waypoint;
import cn.crtlprototypestudios.spos.data.waypoint.WaypointData;
import cn.crtlprototypestudios.spos.manager.BaseJsonManager;
import com.google.gson.reflect.TypeToken;
import net.minecraft.server.level.ServerPlayer;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class WaypointManager extends BaseJsonManager<WaypointData> {
    private static final WaypointManager INSTANCE = new WaypointManager();

    private WaypointManager() {
        super(Spos.MODID, "waypoints.json", new TypeToken<List<WaypointData>>(){}.getType(), Spos.LOGGER);
    }

    public static WaypointManager getInstance() {
        return INSTANCE;
    }

    public WaypointData getOrCreateData(UUID uuid) {
        String uuidStr = uuid.toString();
        return getData().stream()
                .filter(data -> data.getPlayerUUID().equals(uuidStr))
                .findFirst()
                .orElseGet(() -> {
                    WaypointData newData = new WaypointData(uuidStr);
                    getData().add(newData);
                    save();
                    return newData;
                });
    }

    public boolean addWaypoint(ServerPlayer player, String name) {
        WaypointData data = getOrCreateData(player.getUUID());

        // Check if waypoint with this name already exists
        if (data.getWaypoints().stream().anyMatch(wp -> wp.getName().equals(name))) {
            return false;
        }

        Waypoint waypoint = new Waypoint(
                player.level().toString(),
                name,
                player.getX(),
                player.getY(),
                player.getZ()
        );

        data.getWaypoints().add(waypoint);
        save();
        return true;
    }

    public boolean removeWaypoint(UUID playerUUID, String name) {
        WaypointData data = getOrCreateData(playerUUID);
        boolean removed = data.getWaypoints().removeIf(wp -> wp.getName().equals(name));
        if (removed) {
            save();
        }
        return removed;
    }

    public boolean overrideWaypoint(ServerPlayer player, String name) {
        WaypointData data = getOrCreateData(player.getUUID());
        Optional<Waypoint> existing = data.getWaypoints().stream()
                .filter(wp -> wp.getName().equals(name))
                .findFirst();

        if (existing.isEmpty()) {
            return false;
        }

        removeWaypoint(player.getUUID(), name);
        addWaypoint(player, name);
        return true;
    }

    public Optional<Waypoint> getWaypoint(UUID playerUUID, String name) {
        WaypointData data = getOrCreateData(playerUUID);
        return data.getWaypoints().stream()
                .filter(wp -> wp.getName().equals(name))
                .findFirst();
    }

    public List<Waypoint> getAllWaypoints(UUID playerUUID) {
        return new ArrayList<>(getOrCreateData(playerUUID).getWaypoints());
    }
}
