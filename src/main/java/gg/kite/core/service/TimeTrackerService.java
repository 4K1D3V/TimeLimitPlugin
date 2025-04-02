package gg.kite.core.service;

import gg.kite.core.Main;
import gg.kite.core.api.TimeLimitService;
import gg.kite.core.config.ConfigManager;
import gg.kite.core.event.TimeLimitEvent;
import gg.kite.core.storage.JsonStorage;
import gg.kite.core.util.ChatUtil;
import gg.kite.core.util.Constants;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.boss.BarColor;
import org.bukkit.boss.BarStyle;
import org.bukkit.boss.BossBar;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;
import org.slf4j.Logger;

import java.time.*;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

public class TimeTrackerService implements TimeLimitService {
    private final Main plugin;
    private final ConfigManager config;
    private final JsonStorage storage;
    private final Logger logger;
    private final Map<UUID, Long> sessionStartNanos = new ConcurrentHashMap<>();
    private final Map<UUID, Integer> dailyPlayTimes = new ConcurrentHashMap<>();
    private LocalDate currentDay;
    private BukkitRunnable monitorTask;

    public TimeTrackerService(Main plugin, ConfigManager config, JsonStorage storage) {
        this.plugin = plugin;
        this.config = config;
        this.storage = storage;
        this.logger = plugin.getSLF4JLogger();
        this.currentDay = LocalDate.now(ZoneOffset.UTC);
        loadPlayTimes();
        startGlobalMonitor();
    }

    private void loadPlayTimes() {
        dailyPlayTimes.putAll(storage.loadPlayTimes(currentDay));
    }

    private void startGlobalMonitor() {
        monitorTask = new BukkitRunnable() {
            @Override
            public void run() {
                LocalDate now = LocalDate.now(ZoneOffset.UTC);
                if (!now.equals(currentDay)) {
                    dailyPlayTimes.clear();
                    sessionStartNanos.clear();
                    currentDay = now;
                    storage.clearOldData();
                    if (config.isDebugMode()) logger.info("Daily reset performed at {}", now);
                }
                Bukkit.getOnlinePlayers().forEach(TimeTrackerService.this::monitorPlayer);
            }
        };
        monitorTask.runTaskTimer(plugin, 0L, Constants.MONITOR_INTERVAL_TICKS);
    }

    @Override
    public void handlePlayerJoin(Player player) {
        UUID uuid = player.getUniqueId();
        if (isExempt(uuid)) return;

        int playedToday = dailyPlayTimes.getOrDefault(uuid, 0);
        int maxTime = config.getMaxPlayTimeMinutes(uuid);
        if (playedToday >= maxTime) {
            kickPlayer(player);
            return;
        }

        sessionStartNanos.put(uuid, System.nanoTime());
        ChatUtil.sendMessage(player, "welcome", true, maxTime - playedToday);
    }

    @Override
    public void handlePlayerQuit(Player player) {
        UUID uuid = player.getUniqueId();
        if (isExempt(uuid)) return;

        sessionStartNanos.computeIfPresent(uuid, (id, start) -> {
            updatePlayTime(uuid, start);
            return null;
        });
    }

    private void monitorPlayer(Player player) {
        UUID uuid = player.getUniqueId();
        if (isExempt(uuid) || !sessionStartNanos.containsKey(uuid)) return;

        long startNano = sessionStartNanos.get(uuid);
        int sessionMinutes = (int)TimeUnit.NANOSECONDS.toMinutes(System.nanoTime() - startNano);
        int totalPlayed = dailyPlayTimes.getOrDefault(uuid, 0) + sessionMinutes;
        int maxTime = config.getMaxPlayTimeMinutes(uuid);
        int remaining = maxTime - totalPlayed;

        if (totalPlayed >= maxTime) {
            kickPlayer(player);
        } else if (config.getWarningThresholds().contains(remaining)) {
            ChatUtil.sendMessage(player, "warning", true, remaining);
            showBossBar(player, remaining, maxTime);
        }
    }

    private void kickPlayer(Player player) {
        TimeLimitEvent event = new TimeLimitEvent(player);
        Bukkit.getPluginManager().callEvent(event);
        if (!event.isCancelled()) {
            ChatUtil.sendError(player, "time-exceeded");
            player.kickPlayer(ChatUtil.getPrefix() + "Daily time limit reached!");
            updatePlayTime(player.getUniqueId(), sessionStartNanos.remove(player.getUniqueId()));
        }
    }

    private void updatePlayTime(UUID uuid, long startNano) {
        int minutes = (int)TimeUnit.NANOSECONDS.toMinutes(System.nanoTime() - startNano);
        dailyPlayTimes.merge(uuid, minutes, Integer::sum);
        storage.savePlayTime(uuid, dailyPlayTimes.get(uuid), currentDay);
    }

    private void showBossBar(Player player, int remaining, int maxTime) {
        BossBar bar = Bukkit.createBossBar(
            ChatColor.YELLOW + "Time Left: " + remaining + "m",
            BarColor.BLUE,
            BarStyle.SEGMENTED_10
        );
        bar.setProgress(Math.min(1.0, (double)remaining / maxTime));
        bar.addPlayer(player);
        new BukkitRunnable() {
            @Override
            public void run() {
                bar.removePlayer(player);
            }
        }.runTaskLater(plugin, Constants.BOSS_BAR_DURATION_TICKS);
    }

    @Override
    public long getRemainingTime(UUID uuid) {
        if (isExempt(uuid)) return Long.MAX_VALUE;
        long startNano = sessionStartNanos.getOrDefault(uuid, System.nanoTime());
        long elapsedSeconds = TimeUnit.NANOSECONDS.toSeconds(System.nanoTime() - startNano);
        int playedToday = dailyPlayTimes.getOrDefault(uuid, 0);
        return Math.max(0, (config.getMaxPlayTimeMinutes(uuid) * 60L) - (playedToday * 60L + elapsedSeconds));
    }

    @Override
    public boolean isExempt(UUID uuid) {
        Player player = Bukkit.getPlayer(uuid);
        return (player != null && player.isOp()) || config.getExemptPlayers().contains(uuid);
    }

    @Override
    public void resetPlayerTime(UUID uuid) {
        dailyPlayTimes.remove(uuid);
        sessionStartNanos.remove(uuid);
        storage.removePlayTime(uuid);
    }

    @Override
    public void shutdown() {
        if (monitorTask != null) {
            monitorTask.cancel();
        }
    }
}