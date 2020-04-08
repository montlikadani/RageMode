package hu.montlikadani.ragemode.commands.list;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.Utils;
import hu.montlikadani.ragemode.commands.ICommand;
import hu.montlikadani.ragemode.runtimePP.RuntimePPManager;
import hu.montlikadani.ragemode.scores.PlayerPoints;

import static hu.montlikadani.ragemode.utils.Misc.hasPerm;
import static hu.montlikadani.ragemode.utils.Misc.sendMessage;

public class stats implements ICommand {

	@Override
	public boolean run(RageMode plugin, CommandSender sender, String[] args) {
		if (!hasPerm(sender, "ragemode.stats")) {
			sendMessage(sender, RageMode.getLang().get("no-permission"));
			return false;
		}

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

			showStats(sender, target);
			return true;
		}

		Player p = (Player) sender;
		Player target = p;
		if (args.length == 2) {
			target = Bukkit.getPlayer(args[1]);
			if (target == null) {
				sendMessage(p, RageMode.getLang().get("commands.stats.player-not-found"));
				return false;
			}
		}

		showStats(p, target);
		return true;
	}

	private void showStats(CommandSender sender, Player t) {
		PlayerPoints rpp = RuntimePPManager.getPPForPlayer(t.getUniqueId());

		if (rpp == null) {
			sendMessage(sender, RageMode.getLang().get("not-played-yet", "%player%", t.getName()));
			return;
		}

		for (String list : RageMode.getLang().getList("statistic-list")) {
			list = list.replace("%player%", t.getName());
			list = Utils.setPlaceholders(list, t);

			sendMessage(sender, list);
		}
	}
}
