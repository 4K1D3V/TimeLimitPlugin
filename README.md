# TimeLimitPlugin
A professional Minecraft plugin to limit player playtime.

## Features
- Configurable daily time limits
- Per-player overrides and exemptions
- Warning system with boss bar
- JSON-based persistence
- Full command suite with tab completion
- API for integration
- Localization support

## Commands
- `/timelimit info [player]` - Check remaining time (`timelimit.info`)
- `/timelimit exempt <player>` - Exempt a player (`timelimit.exempt`)
- `/timelimit unexempt <player>` - Remove exemption (`timelimit.unexempt`)
- `/timelimit set <player> <minutes>` - Set custom limit (`timelimit.set`)
- `/timelimit reset <player>` - Reset playtime (`timelimit.reset`)
- `/timelimit reload` - Reload config (`timelimit.reload`)

## Permissions
- `timelimit.info` - View time info (default: true)
- `timelimit.exempt` - Exempt players (default: op)
- `timelimit.unexempt` - Remove exemptions (default: op)
- `timelimit.set` - Set custom time limits (default: op)
- `timelimit.reset` - Reset playtime (default: op)
- `timelimit.reload` - Reload config (default: op)
- `timelimit.admin` - Access all commands (default: op)

## Installation
1. Place the JAR in your `plugins` folder
2. Start the server to generate config files
3. Edit `config.json` and `messages.json` as needed
4. Reload with `/timelimit reload`

## API
```java
long remaining = TimeLimitAPI.getRemainingTime(player.getUniqueId());
boolean isExempt = TimeLimitAPI.isExempt(player.getUniqueId());---
```

# Credit
- Made by KiteGG: [Website](https://ks.akii.pro)
- Concept by gReach: [Discord](https://discord.com/users/800747697890918440)

## License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.