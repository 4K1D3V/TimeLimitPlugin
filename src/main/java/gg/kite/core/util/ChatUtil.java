package gg.kite.core.util;

import gg.kite.core.config.ConfigManager;
import org.bukkit.ChatColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

import java.io.File;
import java.io.FileReader;
import java.util.HashMap;
import java.util.Map;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

public class ChatUtil {
    private static String PREFIX;
    private static boolean USE_SOUNDS;
    private static final Map<String, String> MESSAGES = new HashMap<>();

    public static void initialize(ConfigManager config, File messagesFile) {
        PREFIX = ChatColor.translateAlternateColorCodes('&', config.getChatPrefix());
        USE_SOUNDS = config.isUseSounds();
        try (FileReader reader = new FileReader(messagesFile)) {
            JsonObject json = JsonParser.parseReader(reader).getAsJsonObject();
            json.entrySet().forEach(e -> MESSAGES.put(e.getKey(), e.getValue().getAsString()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void sendMessage(Player player, String key, boolean sound, Object... args) {
        String message = ChatColor.translateAlternateColorCodes('&', 
            String.format(MESSAGES.getOrDefault(key, key), args));
        player.sendMessage(PREFIX + message);
        if (sound && USE_SOUNDS) {
            player.playSound(player.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1.0f, 1.0f);
        }
    }

    public static void sendError(Player player, String key, Object... args) {
        String message = ChatColor.translateAlternateColorCodes('&', 
            String.format(MESSAGES.getOrDefault(key, key), args));
        player.sendMessage(PREFIX + message);
        player.playSound(player.getLocation(), Sound.ENTITY_VILLAGER_NO, 1.0f, 1.0f);
    }

    public static String getPrefix() {
        return PREFIX;
    }
}