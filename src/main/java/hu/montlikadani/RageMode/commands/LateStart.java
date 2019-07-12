package hu.montlikadani.ragemode.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.Utils;
import hu.montlikadani.ragemode.gameLogic.GameStatus;
import hu.montlikadani.ragemode.gameLogic.PlayerList;
import hu.montlikadani.ragemode.gameUtils.GameUtils;

public class LateStart extends RmCommand {

	@Override
	public boolean run(CommandSender sender, Command cmd, String[] args) {
		if (!(sender instanceof Player)) {
			sendMessage(sender, RageMode.getLang().get("in-game-only"));
			return false;
		}

		Player p = (Player) sender;

		if (!hasPerm(p, "ragemode.admin.latestart")) {
			sendMessage(p, RageMode.getLang().get("no-permission"));
			return false;
		}

		if (args.length < 2) {
			sendMessage(sender, RageMode.getLang().get("missing-arguments", "%usage%", "/rm latestart <timeInSeconds>"));
			return false;
		}

		if (!PlayerList.isPlayerPlaying(p.getUniqueId().toString())) {
			sendMessage(sender, RageMode.getLang().get("commands.latestart.player-not-in-lobby"));
			return false;
		}

		if (GameUtils.getStatus() != GameStatus.WAITING) {
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

		PlayerList.getLobbyTimer().addLobbyTime(newTime);
		sendMessage(sender, RageMode.getLang().get("commands.latestart.lobby-timer-increased", "%newtime%", newTime));
		return false;
	}
}