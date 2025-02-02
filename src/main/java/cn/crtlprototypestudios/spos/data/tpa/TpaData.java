package cn.crtlprototypestudios.spos.data.tpa;

import java.util.ArrayList;
import java.util.List;

public class TpaData {
    private String playerUUID;
    private List<String> alwaysAllow;
    private List<String> alwaysDecline;

    public TpaData(String playerUUID) {
        this.playerUUID = playerUUID;
        this.alwaysAllow = new ArrayList<>();
        this.alwaysDecline = new ArrayList<>();
    }

    public String getPlayerUUID() {
        return playerUUID;
    }

    public List<String> getAlwaysAllow() {
        return alwaysAllow;
    }

    public List<String> getAlwaysDecline() {
        return alwaysDecline;
    }

    public void addAlwaysAllow(String uuid) {
        if (!alwaysAllow.contains(uuid)) {
            alwaysAllow.add(uuid);
        }
    }

    public void addAlwaysDecline(String uuid) {
        if (!alwaysDecline.contains(uuid)) {
            alwaysDecline.add(uuid);
        }
    }

    public void removeAlwaysAllow(String uuid) {
        alwaysAllow.remove(uuid);
    }

    public void removeAlwaysDecline(String uuid) {
        alwaysDecline.remove(uuid);
    }
}

