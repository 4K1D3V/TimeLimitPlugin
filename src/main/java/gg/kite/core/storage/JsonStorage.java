package gg.kite.core.storage;

import com.google.gson.*;
import gg.kite.core.Main;
import org.slf4j.Logger;

import java.io.*;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class JsonStorage {
    private final Main plugin;
    private final Logger logger;
    private final File storageFile;
    private final Gson gson;
    private final Object lock = new Object();

    public JsonStorage(Main plugin) {
        this.plugin = plugin;
        this.logger = plugin.getSLF4JLogger();
        this.storageFile = new File(plugin.getDataFolder(), "playtime.json");
        this.gson = new GsonBuilder().setPrettyPrinting().create();
        if (!storageFile.exists()) {
            try {
                storageFile.createNewFile();
                try (FileWriter writer = new FileWriter(storageFile)) {
                    gson.toJson(new JsonObject(), writer);
                }
            } catch (IOException e) {
                logger.error("Failed to initialize playtime.json", e);
            }
        }
    }

    public Map<UUID, Integer> loadPlayTimes(LocalDate currentDay) {
        synchronized (lock) {
            Map<UUID, Integer> playTimes = new HashMap<>();
            try (FileReader reader = new FileReader(storageFile)) {
                JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
                for (Map.Entry<String, JsonElement> entry : json.entrySet()) {
                    JsonObject data = entry.getValue().getAsJsonObject();
                    String date = data.get("date").getAsString();
                    if (currentDay.toString().equals(date)) {
                        playTimes.put(UUID.fromString(entry.getKey()), data.get("minutes").getAsInt());
                    }
                }
            } catch (IOException e) {
                logger.error("Failed to load playtime.json", e);
            }
            return playTimes;
        }
    }

    public void savePlayTime(UUID uuid, int minutes, LocalDate date) {
        synchronized (lock) {
            JsonObject json = readJson();
            JsonObject data = new JsonObject();
            data.addProperty("minutes", minutes);
            data.addProperty("date", date.toString());
            json.add(uuid.toString(), data);
            writeJson(json);
        }
    }

    public void removePlayTime(UUID uuid) {
        synchronized (lock) {
            JsonObject json = readJson();
            json.remove(uuid.toString());
            writeJson(json);
        }
    }

    public void clearOldData() {
        synchronized (lock) {
            writeJson(new JsonObject());
        }
    }

    private JsonObject readJson() {
        try (FileReader reader = new FileReader(storageFile)) {
            return JsonParser.parseReader(reader).getAsJsonObject();
        } catch (IOException e) {
            logger.error("Failed to read playtime.json", e);
            return new JsonObject();
        }
    }

    private void writeJson(JsonObject json) {
        try (FileWriter writer = new FileWriter(storageFile)) {
            gson.toJson(json, writer);
        } catch (IOException e) {
            logger.error("Failed to write playtime.json", e);
        }
    }
}