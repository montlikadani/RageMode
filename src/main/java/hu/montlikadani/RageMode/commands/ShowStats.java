package hu.montlikadani.ragemode.commands;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.Utils;
import hu.montlikadani.ragemode.runtimeRPP.RuntimeRPPManager;
import hu.montlikadani.ragemode.scores.RetPlayerPoints;

public class ShowStats extends RmCommand {

	public ShowStats(CommandSender sender, Command cmd, String label, String[] args) {
		if (!sender.hasPermission("ragemode.stats")) {
			sendMessage(sender, RageMode.getLang().get("no-permission"));
			return;
		}
		if (!(sender instanceof Player)) {
			if (args.length < 2) {
				sendMessage(sender, RageMode.getLang().get("commands.stats.player-not-null"));
				return;
			}
			Player target = Bukkit.getPlayer(args[1]);
			if (target == null) {
				sendMessage(sender, RageMode.getLang().get("commands.stats.player-not-found"));
				return;
			}
			showStats(sender, target);
			return;
		}
		Player p = (Player) sender;
		if (args.length == 2) {
			Player target = Bukkit.getPlayer(args[1]);
			if (target == null) {
				sendMessage(p, RageMode.getLang().get("commands.stats.player-not-found"));
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

				list = Utils.setPlaceholders(list, t);
				list = list.replace("%rank%", Integer.toString(rpp.getRank()));
				sendMessage(sender, list);
			}
		} else
			sendMessage(sender, RageMode.getLang().get("not-played-yet"));
	}
}
