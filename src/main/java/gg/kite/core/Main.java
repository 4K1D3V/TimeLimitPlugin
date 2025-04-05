package gg.kite.core;

import gg.kite.core.api.TimeLimitAPI;
import gg.kite.core.command.CommandManager;
import gg.kite.core.config.ConfigManager;
import gg.kite.core.listener.PlayerListener;
import gg.kite.core.service.TimeTrackerService;
import gg.kite.core.storage.JsonStorage;
import org.bukkit.plugin.java.JavaPlugin;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Main extends JavaPlugin {
    private static final Logger LOGGER = LoggerFactory.getLogger(Main.class);
    private ConfigManager configManager;
    private TimeTrackerService timeTrackerService;
    private CommandManager commandManager;
    final String pluginName = "TimeLimit";
    final String version = getDescription().getVersion();
    final String author = "KiteGG";

    @Override
    public void onEnable() {
        LOGGER.info("Starting {} with {} by {}", pluginName, version, author);
        saveResource("config.json", false);
        saveResource("messages.json", false);

        JsonStorage storage = new JsonStorage(this);
        configManager = new ConfigManager(this);
        timeTrackerService = new TimeTrackerService(this, configManager, storage);
        commandManager = new CommandManager(this, timeTrackerService, configManager);

        getServer().getPluginManager().registerEvents(new PlayerListener(timeTrackerService), this);
        getCommand("timelimit").setExecutor(commandManager);
        getCommand("timelimit").setTabCompleter(commandManager);

        TimeLimitAPI.initialize(timeTrackerService);
        LOGGER.info("{} enabled successfully", pluginName);
    }

    @Override
    public void onDisable() {
        timeTrackerService.shutdown();
        LOGGER.info("{} disabled", pluginName);
    }
}