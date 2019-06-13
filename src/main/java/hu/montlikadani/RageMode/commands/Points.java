package hu.montlikadani.ragemode.commands;

import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.gameLogic.PlayerList;
import hu.montlikadani.ragemode.runtimeRPP.RuntimeRPPManager;
import hu.montlikadani.ragemode.scores.RetPlayerPoints;
import hu.montlikadani.ragemode.statistics.YAMLStats;

public class Points extends RmCommand {

	private enum Actions {
		Set, Add, Take
	}

	@Override
	public boolean run(CommandSender sender, Command cmd, String[] args) {
		if (!hasPerm(sender, "ragemode.admin.points")) {
			sendMessage(sender, RageMode.getLang().get("no-permission"));
			return false;
		}

		if (args.length < 4) {
			sendMessage(sender, RageMode.getLang().get("missing-arguments", "%usage%", "/rm points set/add/take <player> <amount>"));
			return false;
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
			return false;
		}

		int amount = 0;
		try {
			amount = Integer.parseInt(args[3]);
		} catch (NumberFormatException e) {
			sendMessage(sender, RageMode.getLang().get("not-a-number", "%number%", args[3]));
			return false;
		}

		if (amount < 0) {
			sendMessage(sender, RageMode.getLang().get("commands.points.amount-not-less"));
			return false;
		}

		if (PlayerList.isPlayerPlaying(target.getUniqueId().toString())) {
			sendMessage(sender, RageMode.getLang().get("commands.points.player-is-in-game", "%player%", args[2]));
			return false;
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
		return false;
	}
}
