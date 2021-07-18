package hu.montlikadani.ragemode.commands.list;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.commands.ICommand;
import hu.montlikadani.ragemode.commands.annotations.CommandProcessor;
import hu.montlikadani.ragemode.gameUtils.GameUtils;

import static hu.montlikadani.ragemode.utils.Misc.sendMessage;

@CommandProcessor(
		name = "resetplayerstats",
		params = "[player]",
		desc = "Resets a specific player's statistics",
		permission = "ragemode.admin.stats.reset")
public final class resetplayerstats implements ICommand {

	@Override
	public boolean run(RageMode plugin, CommandSender sender, String[] args) {
		boolean isPlayer = sender instanceof Player;

		if (!isPlayer || args.length == 2) {
			if (!isPlayer && args.length < 2) {
				sendMessage(sender, RageMode.getLang().get("commands.stats.player-not-null"));
				return false;
			}

			Player target = plugin.getServer().getPlayer(args[1]);
			if (target == null) {
				sendMessage(sender, RageMode.getLang().get("commands.stats.player-not-found"));
				return false;
			}

			if (GameUtils.isPlayerPlaying(target)) {
				sendMessage(sender, RageMode.getLang().get("commands.stats.player-currently-in-game"));
				return false;
			}

			if (plugin.getDatabase().resetPlayerStatistic(target.getUniqueId())) {
				sendMessage(sender,
						RageMode.getLang().get("commands.stats.target-stats-reseted", "%player%", target.getName()));
				sendMessage(target, RageMode.getLang().get("commands.stats.reseted"));
				return true;
			}

			sendMessage(sender, RageMode.getLang().get("commands.stats.could-not-reset-player-stats"));
			return false;
		}

		Player player = (Player) sender;

		if (GameUtils.isPlayerPlaying(player)) {
			sendMessage(player, RageMode.getLang().get("commands.stats.player-currently-in-game"));
			return false;
		}

		if (plugin.getDatabase().resetPlayerStatistic(player.getUniqueId())) {
			sendMessage(player, RageMode.getLang().get("commands.stats.reseted"));
			return true;
		}

		sendMessage(player, RageMode.getLang().get("commands.stats.could-not-reset-player-stats"));
		return false;
	}
}
