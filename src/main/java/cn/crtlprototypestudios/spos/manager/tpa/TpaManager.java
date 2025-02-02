package cn.crtlprototypestudios.spos.manager.tpa;

import cn.crtlprototypestudios.spos.Spos;
import cn.crtlprototypestudios.spos.data.TeleportRequest;
import cn.crtlprototypestudios.spos.data.tpa.TpaData;
import cn.crtlprototypestudios.spos.manager.BaseJsonManager;
import cn.crtlprototypestudios.spos.manager.TeleportManager;
import com.google.gson.reflect.TypeToken;

import java.util.*;
import java.util.stream.Collectors;

public class TpaManager extends BaseJsonManager<TpaData> {
    private static final TpaManager INSTANCE = new TpaManager();

    private TpaManager() {
        super(Spos.MODID, "tpa.json", new TypeToken<List<TpaData>>(){}.getType(), Spos.LOGGER);
    }

    public static TpaManager getInstance() {
        return INSTANCE;
    }

    public TpaData getOrCreateData(UUID uuid) {
        String uuidStr = uuid.toString();
        return getData().stream()
                .filter(data -> data.getPlayerUUID().equals(uuidStr))
                .findFirst()
                .orElseGet(() -> {
                    TpaData newData = new TpaData(uuidStr);
                    getData().add(newData);
                    save();
                    return newData;
                });
    }

    public Optional<UUID> getLatestRequest(UUID playerUUID) {
        Optional<TeleportRequest> request = TeleportManager.getRequest(playerUUID);
        return request.map(TeleportRequest::getFrom);
    }

    public boolean isAlwaysAllowed(UUID from, UUID to) {
        TpaData data = getOrCreateData(to);
        return data.getAlwaysAllow().contains(from.toString());
    }

    public boolean isAlwaysDenied(UUID from, UUID to) {
        TpaData data = getOrCreateData(to);
        return data.getAlwaysDeny().contains(from.toString());
    }

    public void addAlwaysDenyPlayer(UUID owner, UUID target) {
        TpaData data = getOrCreateData(owner);
        data.addAlwaysDeny(target.toString());
        // Remove from allow list if present
        data.removeAlwaysAllow(target.toString());
        save();
    }

    public void removeAlwaysDenyPlayer(UUID owner, UUID target) {
        TpaData data = getOrCreateData(owner);
        data.removeAlwaysDeny(target.toString());
        save();
    }

    public void setAlwaysDenyPlayers(UUID owner, Collection<UUID> targets) {
        TpaData data = getOrCreateData(owner);
        List<String> targetStrings = targets.stream()
                .map(UUID::toString)
                .collect(Collectors.toSet()).stream().toList();
        data.setAlwaysDeny(targetStrings);
        // Remove any of these players from allow list
        data.getAlwaysAllow().removeAll(targetStrings);
        save();
    }

    public void addAlwaysAllowPlayer(UUID owner, UUID target) {
        TpaData data = getOrCreateData(owner);
        data.addAlwaysAllow(target.toString());
        // Remove from deny list if present
        data.removeAlwaysDeny(target.toString());
        save();
    }

    public void removeAlwaysAllowPlayer(UUID owner, UUID target) {
        TpaData data = getOrCreateData(owner);
        data.removeAlwaysAllow(target.toString());
        save();
    }

    public void setAlwaysAllowPlayers(UUID owner, Collection<UUID> targets) {
        TpaData data = getOrCreateData(owner);
        List<String> targetStrings = targets.stream()
                .map(UUID::toString)
                .collect(Collectors.toSet()).stream().toList();
        data.setAlwaysAllow(targetStrings);
        // Remove any of these players from deny list
        data.getAlwaysDeny().removeAll(targetStrings);
        save();
    }

    public Set<UUID> getAlwaysAllowPlayers(UUID owner) {
        TpaData data = getOrCreateData(owner);
        return data.getAlwaysAllow().stream()
                .map(UUID::fromString)
                .collect(Collectors.toSet());
    }

    public Set<UUID> getAlwaysDenyPlayers(UUID owner) {
        TpaData data = getOrCreateData(owner);
        return data.getAlwaysDeny().stream()
                .map(UUID::fromString)
                .collect(Collectors.toSet());
    }

    public Optional<UUID> getLatestSentRequest(UUID playerUUID) {
        return TeleportManager.getAllRequests().stream()
                .filter(request -> request.getFrom().equals(playerUUID))
                .map(TeleportRequest::getTo)
                .findFirst();
    }
}


