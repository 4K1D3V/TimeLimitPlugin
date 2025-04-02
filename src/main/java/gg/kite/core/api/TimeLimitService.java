package gg.kite.core.api;

import org.bukkit.entity.Player;

import java.util.UUID;

public interface TimeLimitService {
    void handlePlayerJoin(Player player);
    void handlePlayerQuit(Player player);
    long getRemainingTime(UUID uuid);
    boolean isExempt(UUID uuid);
    void resetPlayerTime(UUID uuid);
    void shutdown();
}