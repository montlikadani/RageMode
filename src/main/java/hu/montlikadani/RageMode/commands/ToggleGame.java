package hu.montlikadani.ragemode.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.gameLogic.GameStatus;
import hu.montlikadani.ragemode.gameLogic.PlayerList;
import hu.montlikadani.ragemode.gameUtils.GetGames;

public class ToggleGame extends RmCommand {

	public ToggleGame(CommandSender sender, Command cmd, String label, String[] args) {
		if (!sender.hasPermission("ragemode.admin.togglegame")) {
			sendMessage(sender, RageMode.getLang().get("no-permission"));
			return;
		}

		if (args.length < 2) {
			sendMessage(sender, RageMode.getLang().get("missing-arguments", "%usage%", "/rm " + args[0] + " <gameName>"));
			return;
		}

		if (!GetGames.isGameExistent(args[1])) {
			sendMessage(sender, RageMode.getLang().get("invalid-game"));
			return;
		}

		boolean toggle = false;
		if (!PlayerList.isGameRunning(args[1])) {
			if (PlayerList.getStatus() == GameStatus.NOTREADY) {
				PlayerList.setStatus(GameStatus.READY);
				toggle = true;
			} else
				PlayerList.setStatus(GameStatus.NOTREADY);

			sendMessage(sender, RageMode.getLang().get("commands.togglegame.successfully-toggled", "%game%", args[1], "%status%",
					toggle ? RageMode.getLang().get("commands.togglegame.status.on") : RageMode.getLang().get("commands.togglegame.status.off")));
		} else
			sendMessage(sender, RageMode.getLang().get("commands.togglegame.game-is-running"));

		return;
	}
}
