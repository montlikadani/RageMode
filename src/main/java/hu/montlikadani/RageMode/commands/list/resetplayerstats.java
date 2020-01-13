package hu.montlikadani.ragemode.commands.list;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.gameUtils.GameUtils;
import hu.montlikadani.ragemode.storage.MySQLDB;
import hu.montlikadani.ragemode.storage.SQLDB;
import hu.montlikadani.ragemode.storage.YAMLDB;

import static hu.montlikadani.ragemode.utils.Misc.hasPerm;
import static hu.montlikadani.ragemode.utils.Misc.sendMessage;

import java.util.UUID;

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

			if (reset(target.getUniqueId())) {
				sendMessage(sender, RageMode.getLang().get("commands.stats.target-stats-reseted", "%player%", target.getName()));
				sendMessage(target, RageMode.getLang().get("commands.stats.reseted"));
				return true;
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

			if (reset(target.getUniqueId())) {
				sendMessage(p, RageMode.getLang().get("commands.stats.target-stats-reseted", "%player%", target.getName()));
				sendMessage(target, RageMode.getLang().get("commands.stats.reseted"));
				return true;
			}

			return false;
		}

		if (GameUtils.isPlayerPlaying(p)) {
			sendMessage(sender, RageMode.getLang().get("commands.stats.player-currently-in-game"));
			return false;
		}

		if (reset(p.getUniqueId())) {
			sendMessage(p, RageMode.getLang().get("commands.stats.reseted"));
			return true;
		}

		return false;
	}

	private boolean reset(UUID uuid) {
		String type = hu.montlikadani.ragemode.config.ConfigValues.getDatabaseType();
		switch (type) {
		case "mysql":
			return MySQLDB.resetPlayerStatistic(uuid);
		case "sql":
		case "sqlite":
			return SQLDB.resetPlayerStatistic(uuid);
		default:
			return YAMLDB.resetPlayerStatistic(uuid);
		}
	}
}
