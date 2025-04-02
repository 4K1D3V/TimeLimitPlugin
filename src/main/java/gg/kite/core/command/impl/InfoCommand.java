package gg.kite.core.command.impl;

import gg.kite.core.service.TimeTrackerService;
import gg.kite.core.util.ChatUtil;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class InfoCommand implements CommandHandler {
    private final TimeTrackerService timeTracker;

    public InfoCommand(TimeTrackerService timeTracker) {
        this.timeTracker = timeTracker;
    }

    @Override
    public void execute(Player player, String[] args) {
        Player target = args.length == 0 ? player : player.getServer().getPlayer(args[0]);
        if (target == null) {
            ChatUtil.sendError(player, "player-not-found");
            return;
        }
        long remaining = timeTracker.getRemainingTime(target.getUniqueId());
        ChatUtil.sendMessage(player, "time-info", true, target.getName(), remaining / 60);
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
        return "timelimit.info";
    }
}