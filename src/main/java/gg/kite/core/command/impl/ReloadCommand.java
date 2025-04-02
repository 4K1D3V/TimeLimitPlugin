package gg.kite.core.command.impl;

import gg.kite.core.config.ConfigManager;
import gg.kite.core.util.ChatUtil;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;

public class ReloadCommand implements CommandHandler {
    private final ConfigManager config;

    public ReloadCommand(ConfigManager config) {
        this.config = config;
    }

    @Override
    public void execute(Player player, String[] args) {
        config.loadConfig();
        ChatUtil.sendMessage(player, "reload-success", true);
    }

    @Override
    public List<String> tabComplete(Player player, String[] args) {
        return Collections.emptyList();
    }

    @Override
    public String getPermission() {
        return "timelimit.reload";
    }
}