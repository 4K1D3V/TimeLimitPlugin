package gg.kite.core.listener;

import gg.kite.core.api.TimeLimitService;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerListener implements Listener {
    private final TimeLimitService service;

    public PlayerListener(TimeLimitService service) {
        this.service = service;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        service.handlePlayerJoin(event.getPlayer());
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        service.handlePlayerQuit(event.getPlayer());
    }
}