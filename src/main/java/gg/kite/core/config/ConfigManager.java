package gg.kite.core.config;

import com.google.gson.*;
import gg.kite.core.Main;
import org.slf4j.Logger;

import java.io.*;
import java.util.*;

public class ConfigManager {
    private final Main plugin;
    private final Logger logger;
    private final File configFile;
    private int defaultMaxPlayTimeMinutes;
    private List<Integer> warningThresholds;
    private String chatPrefix;
    private boolean useSounds;
    private boolean debugMode;
    private final Map<UUID, Integer> playerOverrides;
    private final Set<UUID> exemptPlayers;

    public ConfigManager(Main plugin) {
        this.plugin = plugin;
        this.logger = plugin.getSLF4JLogger();
        this.configFile = new File(plugin.getDataFolder(), "config.json");
        this.playerOverrides = new HashMap<>();
        this.exemptPlayers = new HashSet<>();
        loadConfig();
    }

    public void loadConfig() {
        try (FileReader reader = new FileReader(configFile)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            defaultMaxPlayTimeMinutes = json.get("max-playtime-minutes").getAsInt();
            warningThresholds = new ArrayList<>();
            json.get("warning-thresholds").getAsJsonArray().forEach(e -> warningThresholds.add(e.getAsInt()));
            JsonObject chat = json.get("chat").getAsJsonObject();
            chatPrefix = chat.get("prefix").getAsString();
            useSounds = chat.get("use-sounds").getAsBoolean();
            debugMode = json.get("debug-mode").getAsBoolean();

            playerOverrides.clear();
            json.getAsJsonObject("player-overrides").entrySet()
                .forEach(e -> playerOverrides.put(UUID.fromString(e.getKey()), e.getValue().getAsInt()));

            exemptPlayers.clear();
            json.getAsJsonArray("exempt-players")
                .forEach(e -> exemptPlayers.add(UUID.fromString(e.getAsString())));
        } catch (IOException | JsonParseException e) {
            logger.error("Failed to load config.json", e);
        }
    }

    public int getMaxPlayTimeMinutes(UUID uuid) {
        return playerOverrides.getOrDefault(uuid, defaultMaxPlayTimeMinutes);
    }

    public List<Integer> getWarningThresholds() { return Collections.unmodifiableList(warningThresholds); }
    public String getChatPrefix() { return chatPrefix; }
    public boolean isUseSounds() { return useSounds; }
    public boolean isDebugMode() { return debugMode; }
    public Set<UUID> getExemptPlayers() { return Collections.unmodifiableSet(exemptPlayers); }
    public void addExemptPlayer(UUID uuid) { exemptPlayers.add(uuid); }
    public void removeExemptPlayer(UUID uuid) { exemptPlayers.remove(uuid); }
    public void setPlayerOverride(UUID uuid, int minutes) { playerOverrides.put(uuid, minutes); }
}