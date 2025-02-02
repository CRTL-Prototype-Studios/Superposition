package cn.crtlprototypestudios.spos.data.tpa;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

public class TpaData {
    private String playerUUID;
    private List<String> alwaysAllow;
    private List<String> alwaysDeny;

    public TpaData(String playerUUID) {
        this.playerUUID = playerUUID;
        this.alwaysAllow = new ArrayList<>();
        this.alwaysDeny = new ArrayList<>();
    }

    public String getPlayerUUID() {
        return playerUUID;
    }

    public List<String> getAlwaysAllow() {
        return alwaysAllow;
    }

    public void setAlwaysAllow(List<String> alwaysAllow) {
        this.alwaysAllow = alwaysAllow;
    }

    public List<String> getAlwaysDeny() {
        return alwaysDeny;
    }

    public void setAlwaysDeny(List<String> alwaysDeny) {
        this.alwaysDeny = alwaysDeny;
    }

    public void addAlwaysAllow(String uuid) {
        alwaysAllow.add(uuid);
    }

    public void removeAlwaysAllow(String uuid) {
        alwaysAllow.remove(uuid);
    }

    public void addAlwaysDeny(String uuid) {
        alwaysDeny.add(uuid);
    }

    public void removeAlwaysDeny(String uuid) {
        alwaysDeny.remove(uuid);
    }
}

