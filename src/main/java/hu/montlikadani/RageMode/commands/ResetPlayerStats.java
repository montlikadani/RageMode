package hu.montlikadani.ragemode.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.gameLogic.PlayerList;
import hu.montlikadani.ragemode.statistics.YAMLStats;

public class ResetPlayerStats extends RmCommand {

	@Override
	public boolean run(CommandSender sender, Command cmd, String[] args) {
		if (!hasPerm(sender, "ragemode.admin.stats.reset")) {
			sendMessage(sender, RageMode.getLang().get("no-permission"));
			return false;
		}
		if (!(sender instanceof Player)) {
			if (args.length < 2) {
				sendMessage(sender, RageMode.getLang().get("commands.stats.player-not-null"));
				return false;
			}
			Player target = Bukkit.getPlayer(args[0]);
			if (target == null) {
				sendMessage(sender, RageMode.getLang().get("commands.stats.player-not-found"));
				return false;
			}
			if (PlayerList.isPlayerPlaying(target.getUniqueId().toString())) {
				sendMessage(sender, RageMode.getLang().get("commands.stats.player-currently-in-game"));
				return false;
			}
			if (YAMLStats.resetPlayerStatistic(target.getUniqueId().toString())) {
				sendMessage(sender, RageMode.getLang().get("commands.stats.target-stats-reseted", "%player%", target.getName()));
				sendMessage(target, RageMode.getLang().get("commands.stats.reseted"));
			}
			return false;
		}
		if (args.length == 1) {
			Player target = Bukkit.getPlayer(args[0]);
			if (target == null) {
				sendMessage(sender, RageMode.getLang().get("commands.stats.player-not-found"));
				return false;
			}
			if (PlayerList.isPlayerPlaying(target.getUniqueId().toString())) {
				sendMessage(sender, RageMode.getLang().get("commands.stats.player-currently-in-game"));
				return false;
			}
			if (YAMLStats.resetPlayerStatistic(target.getUniqueId().toString())) {
				sendMessage(sender, RageMode.getLang().get("commands.stats.target-stats-reseted", "%player%", target.getName()));
				sendMessage(target, RageMode.getLang().get("commands.stats.reseted"));
			}
			return false;
		}
		Player p = (Player) sender;
		if (PlayerList.isPlayerPlaying(p.getUniqueId().toString())) {
			sendMessage(sender, RageMode.getLang().get("commands.stats.player-currently-in-game"));
			return false;
		}
		if (YAMLStats.resetPlayerStatistic(p.getUniqueId().toString()))
			sendMessage(p, RageMode.getLang().get("commands.stats.reseted"));
		return false;
	}
}
