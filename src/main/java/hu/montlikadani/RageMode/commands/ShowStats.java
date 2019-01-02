package hu.montlikadani.ragemode.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.runtimeRPP.RuntimeRPPManager;
import hu.montlikadani.ragemode.scores.RetPlayerPoints;

public class ShowStats extends RmCommand {

	public ShowStats(CommandSender sender, Command cmd, String label, String[] args) {
		if (!sender.hasPermission("ragemode.stats")) {
			sender.sendMessage(RageMode.getLang().get("no-permission"));
			return;
		}
		if (!(sender instanceof Player)) {
			if (args.length < 2) {
				sender.sendMessage(RageMode.getLang().get("commands.stats.player-not-null"));
				return;
			}
			Player target = Bukkit.getPlayer(args[1]);
			if (target == null) {
				sender.sendMessage(RageMode.getLang().get("commands.stats.player-not-found"));
				return;
			}
			showStats(sender, target);
			return;
		}
		Player p = (Player) sender;
		if (args.length == 2) {
			Player target = Bukkit.getPlayer(args[1]);
			if (target == null) {
				p.sendMessage(RageMode.getLang().get("commands.stats.player-not-found"));
				return;
			}
			showStats(p, target);
			return;
		}

		showStats(p, p);
	}

	private void showStats(CommandSender sender, Player t) {
		RetPlayerPoints rpp = RuntimeRPPManager.getRPPForPlayer(t.getUniqueId().toString());

		if (rpp != null) {
			for (String list : RageMode.getLang().getList("statistic-list")) {
				list = list.replace("%player%", t.getName());

				list = list.replace("%knife-kills%", rpp.getKnifeKills() + "");
				list = list.replace("%knife-deaths%", rpp.getKnifeDeaths() + "");

				list = list.replace("%explosion-kills%", rpp.getExplosionKills() + "");
				list = list.replace("%explosion-deaths%", rpp.getExplosionDeaths() + "");

				list = list.replace("%axe-kills%", rpp.getAxeKills() + "");
				list = list.replace("%axe-deaths%", rpp.getAxeDeaths() + "");

				list = list.replace("%direct-arrow-kills%", rpp.getDirectArrowKills() + "");
				list = list.replace("%direct-arrow-deaths%", rpp.getDirectArrowDeaths() + "");

				list = list.replace("%kills%", rpp.getKills() + "");
				list = list.replace("%deaths%", rpp.getDeaths() + "");
				list = list.replace("%kd%", rpp.getKD() + "");
				list = list.replace("%games%", rpp.getGames() + "");
				list = list.replace("%wins%", rpp.getWins() + "");
				list = list.replace("%points%", rpp.getPoints() + "");
				list = list.replace("%rank%", Integer.toString(rpp.getRank()) + "");
				sender.sendMessage(list);
			}
		} else
			sender.sendMessage(RageMode.getLang().get("not-played-yet"));
	}
}
