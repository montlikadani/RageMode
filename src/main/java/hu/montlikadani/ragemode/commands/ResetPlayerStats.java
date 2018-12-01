package hu.montlikadani.ragemode.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.scores.RageScores;
import hu.montlikadani.ragemode.statistics.YAMLStats;

public class ResetPlayerStats extends RmCommand {

	public ResetPlayerStats(CommandSender sender, Command cmd, String label, String[] args) {
		if (!sender.hasPermission("ragemode.admin.stats.reset")) {
			sender.sendMessage(RageMode.getLang().get("no-permission"));
			return;
		}
		Player target = null;
		if (!(sender instanceof Player)) {
			if (args.length == 1) {
				target = Bukkit.getPlayer(args[1]);
				if (target == null) {
					sender.sendMessage(RageMode.getLang().get("commands.stats.player-not-null"));
					return;
				}
				reset(target);
				sender.sendMessage(RageMode.getLang().get("commands.stats.target-stats-reseted", "%player%", target.getName()));
				target.sendMessage(RageMode.getLang().get("commands.stats.reseted"));
			}
			return;
		}
		if (args.length == 1) {
			target = Bukkit.getPlayer(args[1]);
			if (target == null) {
				sender.sendMessage(RageMode.getLang().get("commands.stats.player-not-null"));
				return;
			}
			reset(target);
			sender.sendMessage(RageMode.getLang().get("commands.stats.target-stats-reseted", "%player%", target.getName()));
			target.sendMessage(RageMode.getLang().get("commands.stats.reseted"));
			return;
		}
		Player p = (Player) sender;
		reset(p);
		p.sendMessage(RageMode.getLang().get("commands.stats.reseted"));
		return;
	}

	private void reset(Player p) {
		RageScores.resetPlayerStats(p.getUniqueId().toString());
		YAMLStats.resetPlayerStatistic(p.getUniqueId().toString());
	}
}
