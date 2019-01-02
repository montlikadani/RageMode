package hu.montlikadani.ragemode.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.gameLogic.PlayerList;
import hu.montlikadani.ragemode.statistics.YAMLStats;

public class ResetPlayerStats extends RmCommand {

	public ResetPlayerStats(CommandSender sender, Command cmd, String label, String[] args) {
		if (!sender.hasPermission("ragemode.admin.stats.reset")) {
			sender.sendMessage(RageMode.getLang().get("no-permission"));
			return;
		}
		if (!(sender instanceof Player)) {
			if (args.length < 2) {
				sender.sendMessage(RageMode.getLang().get("commands.stats.player-not-null"));
				return;
			}
			Player target = Bukkit.getPlayer(args[0]);
			if (target == null) {
				sender.sendMessage(RageMode.getLang().get("commands.stats.player-not-found"));
				return;
			}
			if (PlayerList.isPlayerPlaying(target.getName())) {
				sender.sendMessage(RageMode.getLang().get("commands.stats.player-currently-in-game"));
				return;
			}
			reset(target);
			sender.sendMessage(RageMode.getLang().get("commands.stats.target-stats-reseted", "%player%", target.getName()));
			target.sendMessage(RageMode.getLang().get("commands.stats.reseted"));
			return;
		}
		if (args.length == 1) {
			Player target = Bukkit.getPlayer(args[0]);
			if (target == null) {
				sender.sendMessage(RageMode.getLang().get("commands.stats.player-not-found"));
				return;
			}
			if (PlayerList.isPlayerPlaying(target.getName())) {
				sender.sendMessage(RageMode.getLang().get("commands.stats.player-currently-in-game"));
				return;
			}
			reset(target);
			sender.sendMessage(RageMode.getLang().get("commands.stats.target-stats-reseted", "%player%", target.getName()));
			target.sendMessage(RageMode.getLang().get("commands.stats.reseted"));
			return;
		}
		Player p = (Player) sender;
		if (PlayerList.isPlayerPlaying(p.getName())) {
			sender.sendMessage(RageMode.getLang().get("commands.stats.player-currently-in-game"));
			return;
		}
		reset(p);
		p.sendMessage(RageMode.getLang().get("commands.stats.reseted"));
		return;
	}

	private void reset(Player p) {
		YAMLStats.resetPlayerStatistic(p.getUniqueId().toString());
	}
}
