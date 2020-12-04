package hu.montlikadani.ragemode.commands.list;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.commands.CommandProcessor;
import hu.montlikadani.ragemode.commands.ICommand;
import hu.montlikadani.ragemode.gameUtils.GameUtils;
import hu.montlikadani.ragemode.runtimePP.RuntimePPManager;
import hu.montlikadani.ragemode.scores.PlayerPoints;

import static hu.montlikadani.ragemode.utils.Misc.sendMessage;

@CommandProcessor(name = "points", permission = "ragemode.admin.points")
public class points implements ICommand {

	private enum Actions {
		Set, Add, Take;
	}

	@Override
	public boolean run(RageMode plugin, CommandSender sender, String[] args) {
		if (args.length < 4) {
			sendMessage(sender, RageMode.getLang().get("missing-arguments", "%usage%",
					"/rm points set/add/take <player> <amount>"));
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
		default:
			break;
		}

		Player target = Bukkit.getPlayer(args[2]);
		if (target == null) {
			sendMessage(sender, RageMode.getLang().get("commands.points.player-not-found"));
			return false;
		}

		if (GameUtils.isPlayerPlaying(target)) {
			sendMessage(sender, RageMode.getLang().get("commands.points.player-is-in-game", "%player%", args[2]));
			return false;
		}

		PlayerPoints rpp = RuntimePPManager.getPPForPlayer(target.getUniqueId());
		if (rpp == null) {
			sendMessage(sender, RageMode.getLang().get("not-played-yet", "%player%", args[2]));
			return false;
		}

		int amount = 0;
		try {
			amount = Integer.parseInt(args[3]);
		} catch (NumberFormatException e) {
			sendMessage(sender, RageMode.getLang().get("not-a-number", "%number%", args[3]));
			return false;
		}

		if (amount <= 0) {
			sendMessage(sender, RageMode.getLang().get("commands.points.amount-not-less"));
			return false;
		}

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
		default:
			break;
		}

		RuntimePPManager.updatePlayerEntry(rpp);

		sendMessage(sender,
				RageMode.getLang().get("commands.points.changed", "%amount%", amount, "%new%", rpp.getPoints()));
		return true;
	}
}
