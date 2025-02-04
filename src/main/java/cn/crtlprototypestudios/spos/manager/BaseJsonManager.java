package cn.crtlprototypestudios.spos.manager;

import cn.crtlprototypestudios.spos.Spos;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import org.slf4j.Logger;

import java.io.*;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public abstract class BaseJsonManager<T> {
    private Gson gson;
    private final File saveFile;
    private final Type dataType;
    protected List<T> data;
    private final Logger LOGGER;

    protected BaseJsonManager(String modid, String fileName, Type dataType, Logger logger) {
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        this.saveFile = new File("config/" + modid + "/" + fileName);
        this.dataType = dataType;
        this.data = new ArrayList<>();
        this.LOGGER = logger;
    }

    protected BaseJsonManager(String modid, String fileName, Type dataType) {
        this(modid, fileName, dataType, null);
    }

    public void load() {
        if (!saveFile.exists()) {
            saveFile.getParentFile().mkdirs();
            // Don't save immediately if file doesn't exist
            data = new ArrayList<>();
            return;
        }

        try (Reader reader = new FileReader(saveFile)) {
            List<T> loadedData = gson.fromJson(reader, dataType);
            if (loadedData != null) {
                data = loadedData;
            } else {
                data = new ArrayList<>();
                Spos.LOGGER.warn("Loaded null data from " + saveFile.getName() + ", initializing empty list");
            }
        } catch (IOException e) {
            Spos.LOGGER.error("Failed to load data from " + saveFile.getName(), e);
            data = new ArrayList<>();
        }
    }

    public void save() {
        try (Writer writer = new FileWriter(saveFile)) {
            gson.toJson(data, writer);
        } catch (IOException e) {
            Spos.LOGGER.error("Failed to save data to " + saveFile.getName(), e);
        }
    }

    protected List<T> getData() {
        return data;
    }

    // Optional: Add custom serialization/deserialization
    protected void setCustomGson(Gson customGson) {
        if (customGson != null) {
            this.gson = customGson;
        }
    }
}