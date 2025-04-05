package gg.kite.core.command.impl;

import gg.kite.core.command.CommandHandler;
import gg.kite.core.service.TimeTrackerService;
import gg.kite.core.util.ChatUtil;
import org.bukkit.entity.Player;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class ResetCommand implements CommandHandler {
    private final TimeTrackerService timeTracker;

    public ResetCommand(TimeTrackerService timeTracker) {
        this.timeTracker = timeTracker;
    }

    @Override
    public void execute(Player player, String[] args) {
        if (args.length != 1) {
            ChatUtil.sendError(player, "usage-reset");
            return;
        }
        Player target = player.getServer().getPlayer(args[0]);
        if (target == null) {
            ChatUtil.sendError(player, "player-not-found");
            return;
        }
        timeTracker.resetPlayerTime(target.getUniqueId());
        ChatUtil.sendMessage(player, "reset-success", true, target.getName());
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
        return "timelimit.reset";
    }
}