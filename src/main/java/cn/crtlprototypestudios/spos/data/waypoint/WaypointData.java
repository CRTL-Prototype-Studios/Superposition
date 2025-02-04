package cn.crtlprototypestudios.spos.data.waypoint;

import java.util.ArrayList;
import java.util.List;

public class WaypointData {
    private final String playerUUID;
    private List<Waypoint> waypoints;

    public WaypointData(String playerUUID) {
        this.playerUUID = playerUUID;
        this.waypoints = new ArrayList<>();
    }

    public String getPlayerUUID() {
        return playerUUID;
    }

    public List<Waypoint> getWaypoints() {
        return waypoints;
    }

    public void setWaypoints(List<Waypoint> waypoints) {
        this.waypoints = waypoints;
    }
}
