package gg.kite.core.command;

import gg.kite.core.Main;
import gg.kite.core.command.impl.*;
import gg.kite.core.config.ConfigManager;
import gg.kite.core.service.TimeTrackerService;
import gg.kite.core.util.ChatUtil;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.stream.Collectors;

public class CommandManager implements CommandExecutor, TabCompleter {
    private final Main plugin;
    private final TimeTrackerService timeTracker;
    private final ConfigManager config;
    private final Map<String, CommandHandler> commands;

    public CommandManager(Main plugin, TimeTrackerService timeTracker, ConfigManager config) {
        this.plugin = plugin;
        this.timeTracker = timeTracker;
        this.config = config;
        this.commands = new HashMap<>();
        commands.put("info", new InfoCommand(timeTracker));
        commands.put("exempt", new ExemptCommand(timeTracker, config));
        commands.put("unexempt", new UnexemptCommand(timeTracker, config));
        commands.put("set", new SetCommand(timeTracker, config));
        commands.put("reset", new ResetCommand(timeTracker));
        commands.put("reload", new ReloadCommand(config));
        ChatUtil.initialize(config, new java.io.File(plugin.getDataFolder(), "messages.json"));
    }

    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command cmd, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Only players can use this command!");
            return true;
        }

        if (args.length == 0) {
            sendHelp(player);
            return true;
        }

        CommandHandler handler = commands.get(args[0].toLowerCase());
        if (handler == null || !player.hasPermission(handler.getPermission())) {
            ChatUtil.sendError(player, "no-permission");
            return true;
        }

        handler.execute(player, Arrays.copyOfRange(args, 1, args.length));
        return true;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, org.bukkit.command.Command cmd, String alias, String[] args) {
        if (!(sender instanceof Player player)) return Collections.emptyList();

        if (args.length == 1) {
            return commands.entrySet().stream()
                .filter(entry -> player.hasPermission(entry.getValue().getPermission()))
                .map(Map.Entry::getKey)
                .collect(Collectors.toList());
        }

        CommandHandler handler = commands.get(args[0].toLowerCase());
        if (handler != null && player.hasPermission(handler.getPermission())) {
            return handler.tabComplete(player, Arrays.copyOfRange(args, 1, args.length));
        }
        return Collections.emptyList();
    }

    private void sendHelp(Player player) {
        ChatUtil.sendMessage(player, "help-header", true);
        commands.forEach((name, handler) -> {
            if (player.hasPermission(handler.getPermission())) {
                ChatUtil.sendMessage(player, "help-" + name, false);
            }
        });
    }
}