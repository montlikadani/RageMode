package hu.montlikadani.ragemode.commands.list;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.commands.ICommand;
import hu.montlikadani.ragemode.commands.annotations.CommandProcessor;
import hu.montlikadani.ragemode.gameLogic.Game;
import hu.montlikadani.ragemode.gameLogic.GameStatus;
import hu.montlikadani.ragemode.gameUtils.GameUtils;
import hu.montlikadani.ragemode.utils.Utils;

import static hu.montlikadani.ragemode.utils.Misc.sendMessage;

@CommandProcessor(
		name = "latestart",
		permission = "ragemode.admin.latestart",
		params = "<timeInSeconds>",
		desc = "Increases the current lobby waiting time",
		playerOnly = true)
public final class latestart implements ICommand {

	@Override
	public boolean run(RageMode plugin, CommandSender sender, String[] args) {
		if (args.length < 2) {
			sendMessage(sender,
					RageMode.getLang().get("missing-arguments", "%usage%", "/rm latestart <timeInSeconds>"));
			return false;
		}

		Game playerGame = GameUtils.getGameByPlayer((Player) sender);
		if (playerGame == null) {
			sendMessage(sender, RageMode.getLang().get("commands.latestart.player-not-in-lobby"));
			return false;
		}

		if (playerGame.getStatus() != GameStatus.WAITING) {
			sendMessage(sender, RageMode.getLang().get("commands.latestart.player-not-in-lobby"));
			return false;
		}

		java.util.Optional<Integer> opt = Utils.tryParseInt(args[1]);
		if (!opt.isPresent()) {
			sendMessage(sender, RageMode.getLang().get("not-a-number", "%number%", args[1]));
			return false;
		}

		int newTime = opt.get();
		if (newTime <= 0) {
			sendMessage(sender, RageMode.getLang().get("commands.latestart.time-can-not-less"));
			return false;
		}

		playerGame.getGameLobby().getLobbyTimer().increaseLobbyTime(newTime);
		sendMessage(sender, RageMode.getLang().get("commands.latestart.lobby-timer-increased", "%newtime%", newTime));
		return true;
	}
}
