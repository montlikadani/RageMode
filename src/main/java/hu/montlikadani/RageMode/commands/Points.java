package hu.montlikadani.ragemode.commands;

import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.runtimeRPP.RuntimeRPPManager;
import hu.montlikadani.ragemode.scores.RetPlayerPoints;
import hu.montlikadani.ragemode.statistics.YAMLStats;

public class Points extends RmCommand {

	private enum Actions {
		Set, Add, Take
	}

	public Points(CommandSender sender, Command cmd, String label, String[] args) {
		if (!sender.hasPermission("ragemode.admin.points")) {
			sendMessage(sender, RageMode.getLang().get("no-permission"));
			return;
		}

		if (args.length < 4) {
			sendMessage(sender, RageMode.getLang().get("missing-arguments", "%usage%", "/rm points set/add/take <player> <amount>"));
			return;
		}

		Actions action = Actions.Add;

		switch (args[1].toLowerCase()) {
		case "set":
			action = Actions.Set;
			break;
		case "add":
			action = Actions.Add;
			break;
		case "take":
			action = Actions.Take;
			break;
		}

		Player target = Bukkit.getPlayer(args[2]);
		if (target == null) {
			sendMessage(sender, RageMode.getLang().get("commands.points.player-not-found"));
			return;
		}

		int amount = 0;
		try {
			amount = Integer.parseInt(args[3]);
		} catch (NumberFormatException e) {
			sendMessage(sender, RageMode.getLang().get("not-a-number", "%number%", args[3]));
			return;
		}

		if (amount < 0) {
			sendMessage(sender, RageMode.getLang().get("commands.points.amount-not-less"));
			return;
		}

		RetPlayerPoints rpp = RuntimeRPPManager.getRPPForPlayer(target.getUniqueId().toString());
		switch (action) {
		case Set:
			rpp.setPoints(amount);
			break;
		case Add:
			rpp.addPoints(amount);
			break;
		case Take:
			rpp.takePoints(amount);
			break;
		}
		YAMLStats.getConf().set("data." + target.getUniqueId() + ".score", rpp.getPoints());
		try {
			YAMLStats.getConf().save(YAMLStats.getFile());
		} catch (IOException e) {
			e.printStackTrace();
		}

		sendMessage(sender, RageMode.getLang().get("commands.points.changed", "%amount%", amount, "%new%", rpp.getPoints()));
		return;
	}
}
