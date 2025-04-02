package gg.kite.core.command.impl;

import gg.kite.core.config.ConfigManager;
import gg.kite.core.service.TimeTrackerService;
import gg.kite.core.util.ChatUtil;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class SetCommand implements CommandHandler {
    private final TimeTrackerService timeTracker;
    private final ConfigManager config;

    public SetCommand(TimeTrackerService timeTracker, ConfigManager config) {
        this.timeTracker = timeTracker;
        this.config = config;
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length != 2) {
            ChatUtil.sendError(player, "usage-set");
            return;
        }
        Player target = player.getServer().getPlayer(args[0]);
        if (target == null) {
            ChatUtil.sendError(player, "player-not-found");
            return;
        }
        try {
            int minutes = Integer.parseInt(args[1]);
            if (minutes < 0) throw new NumberFormatException();
            config.setPlayerOverride(target.getUniqueId(), minutes);
            ChatUtil.sendMessage(player, "set-success", true, target.getName(), minutes);
        } catch (NumberFormatException e) {
            ChatUtil.sendError(player, "invalid-number");
        }
    }

    @Override
    public List<String> tabComplete(Player player, String[] args) {
        if (args.length == 1) {
            return player.getServer().getOnlinePlayers().stream()
                .map(Player::getName)
                .collect(Collectors.toList());
        }
        return Collections.emptyList();
    }

    @Override
    public String getPermission() {
        return "timelimit.set";
    }
}