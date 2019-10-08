package hu.montlikadani.ragemode.commands.list;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.gameUtils.GameUtils;
import hu.montlikadani.ragemode.statistics.MySQLStats;
import hu.montlikadani.ragemode.statistics.SQLStats;
import hu.montlikadani.ragemode.statistics.YAMLStats;

import static hu.montlikadani.ragemode.utils.Message.hasPerm;
import static hu.montlikadani.ragemode.utils.Message.sendMessage;

public class resetplayerstats {

	public boolean run(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			if (args.length < 2) {
				sendMessage(sender, RageMode.getLang().get("commands.stats.player-not-null"));
				return false;
			}

			Player target = Bukkit.getPlayer(args[1]);
			if (target == null) {
				sendMessage(sender, RageMode.getLang().get("commands.stats.player-not-found"));
				return false;
			}

			if (GameUtils.isPlayerPlaying(target)) {
				sendMessage(sender, RageMode.getLang().get("commands.stats.player-currently-in-game"));
				return false;
			}

			if (reset(target.getUniqueId().toString())) {
				sendMessage(sender, RageMode.getLang().get("commands.stats.target-stats-reseted", "%player%", target.getName()));
				sendMessage(target, RageMode.getLang().get("commands.stats.reseted"));
			}
			return false;
		}

		Player p = (Player) sender;
		if (!hasPerm(p, "ragemode.admin.stats.reset")) {
			sendMessage(p, RageMode.getLang().get("no-permission"));
			return false;
		}

		if (args.length == 2) {
			Player target = Bukkit.getPlayer(args[1]);
			if (target == null) {
				sendMessage(p, RageMode.getLang().get("commands.stats.player-not-found"));
				return false;
			}

			if (GameUtils.isPlayerPlaying(target)) {
				sendMessage(p, RageMode.getLang().get("commands.stats.player-currently-in-game"));
				return false;
			}

			if (reset(target.getUniqueId().toString())) {
				sendMessage(p, RageMode.getLang().get("commands.stats.target-stats-reseted", "%player%", target.getName()));
				sendMessage(target, RageMode.getLang().get("commands.stats.reseted"));
			}

			return false;
		}

		if (GameUtils.isPlayerPlaying(p)) {
			sendMessage(sender, RageMode.getLang().get("commands.stats.player-currently-in-game"));
			return false;
		}

		if (reset(p.getUniqueId().toString()))
			sendMessage(p, RageMode.getLang().get("commands.stats.reseted"));

		return false;
	}

	private boolean reset(String uuid) {
		String type = RageMode.getInstance().getConfiguration().getCV().getStatistics();
		switch (type) {
		case "mysql":
			MySQLStats.resetPlayerStatistic(uuid);
			return true;
		case "sql":
		case "sqlite":
			SQLStats.resetPlayerStatistic(uuid);
			return true;
		default:
			YAMLStats.resetPlayerStatistic(uuid);
			return true;
		}
	}
}
