package gg.kite.core.api;

import java.util.UUID;

public class TimeLimitAPI {
    private static TimeLimitService service;

    public static void initialize(TimeLimitService service) {
        TimeLimitAPI.service = service;
    }

    public static long getRemainingTime(UUID uuid) {
        if (service == null) throw new IllegalStateException("TimeLimitAPI not initialized");
        return service.getRemainingTime(uuid);
    }

    public static boolean isExempt(UUID uuid) {
        if (service == null) throw new IllegalStateException("TimeLimitAPI not initialized");
        return service.isExempt(uuid);
    }
}