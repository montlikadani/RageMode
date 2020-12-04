package hu.montlikadani.ragemode.commands.list;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.Utils;
import hu.montlikadani.ragemode.commands.CommandProcessor;
import hu.montlikadani.ragemode.commands.ICommand;
import hu.montlikadani.ragemode.gameLogic.Game;
import hu.montlikadani.ragemode.gameLogic.GameStatus;
import hu.montlikadani.ragemode.gameUtils.GameUtils;

import static hu.montlikadani.ragemode.utils.Misc.sendMessage;

@CommandProcessor(name = "latestart", permission = "ragemode.admin.latestart", playerOnly = true)
public class latestart implements ICommand {

	@Override
	public boolean run(RageMode plugin, CommandSender sender, String[] args) {
		Player p = (Player) sender;
		if (args.length < 2) {
			sendMessage(sender,
					RageMode.getLang().get("missing-arguments", "%usage%", "/rm latestart <timeInSeconds>"));
			return false;
		}

		if (!GameUtils.isPlayerPlaying(p)) {
			sendMessage(sender, RageMode.getLang().get("commands.latestart.player-not-in-lobby"));
			return false;
		}

		Game playerGame = GameUtils.getGameByPlayer(p);
		if (playerGame.getStatus() != GameStatus.WAITING) {
			sendMessage(sender, RageMode.getLang().get("commands.latestart.player-not-in-lobby"));
			return false;
		}

		if (!Utils.isInt(args[1])) {
			sendMessage(sender, RageMode.getLang().get("not-a-number", "%number%", args[1]));
			return false;
		}

		int newTime = Integer.parseInt(args[1]);
		if (newTime <= 0) {
			sendMessage(sender, RageMode.getLang().get("commands.latestart.time-can-not-less"));
			return false;
		}

		playerGame.getLobbyTimer().addLobbyTime(newTime);
		sendMessage(sender, RageMode.getLang().get("commands.latestart.lobby-timer-increased", "%newtime%", newTime));
		return true;
	}
}
