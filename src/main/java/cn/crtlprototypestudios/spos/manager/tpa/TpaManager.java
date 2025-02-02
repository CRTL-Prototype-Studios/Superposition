package cn.crtlprototypestudios.spos.manager.tpa;

import cn.crtlprototypestudios.spos.Spos;
import cn.crtlprototypestudios.spos.data.tpa.TpaData;
import cn.crtlprototypestudios.spos.manager.BaseJsonManager;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

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

    public boolean isAlwaysAllowed(UUID from, UUID to) {
        TpaData data = getOrCreateData(to);
        return data.getAlwaysAllow().contains(from.toString());
    }

    public boolean isAlwaysDeclined(UUID from, UUID to) {
        TpaData data = getOrCreateData(to);
        return data.getAlwaysDecline().contains(from.toString());
    }

    public void setAlwaysAllow(UUID player, UUID target) {
        TpaData data = getOrCreateData(player);
        data.addAlwaysAllow(target.toString());
        data.removeAlwaysDecline(target.toString());
        save();
    }

    public void setAlwaysDecline(UUID player, UUID target) {
        TpaData data = getOrCreateData(player);
        data.addAlwaysDecline(target.toString());
        data.removeAlwaysAllow(target.toString());
        save();
    }
}


