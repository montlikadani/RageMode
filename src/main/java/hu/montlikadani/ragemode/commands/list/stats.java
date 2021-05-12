package hu.montlikadani.ragemode.commands.list;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.commands.ICommand;
import hu.montlikadani.ragemode.commands.annotations.CommandProcessor;
import hu.montlikadani.ragemode.runtimePP.RuntimePPManager;
import hu.montlikadani.ragemode.scores.PlayerPoints;
import hu.montlikadani.ragemode.utils.Utils;

import static hu.montlikadani.ragemode.utils.Misc.sendMessage;

@CommandProcessor(
		name = "stats",
		desc = "Showing statistic of a target player",
		params = "[player]",
		permission = { "ragemode.stats", "ragemode.help.playercommands" })
public final class stats implements ICommand {

	@Override
	public boolean run(RageMode plugin, CommandSender sender, String[] args) {
		boolean isPlayer = sender instanceof Player;

		if (!isPlayer && args.length < 2) {
			sendMessage(sender, RageMode.getLang().get("commands.stats.player-not-null"));
			return false;
		}

		Player target = isPlayer ? (Player) sender : null;

		if ((!isPlayer || args.length >= 2) && (target = Bukkit.getPlayer(args[1])) == null) {
			sendMessage(sender, RageMode.getLang().get("commands.stats.player-not-found"));
			return false;
		}

		if (target == null) {
			return true;
		}

		PlayerPoints rpp = RuntimePPManager.getPPForPlayer(target.getUniqueId());
		if (rpp == null) {
			sendMessage(sender, RageMode.getLang().get("not-played-yet", "%player%", target.getName()));
			return true;
		}

		for (String list : RageMode.getLang().getList("statistic-list")) {
			list = list.replace("%player%", target.getName());
			list = Utils.setPlaceholders(list, target);

			sendMessage(sender, list);
		}

		return true;
	}
}
