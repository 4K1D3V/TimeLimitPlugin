package gg.kite.core.command;

import org.bukkit.entity.Player;

import java.util.List;

public interface CommandHandler {
    void execute(Player player, String[] args);
    List<String> tabComplete(Player player, String[] args);
    String getPermission();
}