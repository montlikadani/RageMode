package hu.montlikadani.ragemode.commands.list;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.gameUtils.GameUtils;
import hu.montlikadani.ragemode.utils.ReJoinDelay;

import static hu.montlikadani.ragemode.utils.Message.hasPerm;
import static hu.montlikadani.ragemode.utils.Message.sendMessage;

public class join {

	public void run(CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			sendMessage(sender, RageMode.getLang().get("in-game-only"));
			return;
		}

		Player p = (Player) sender;
		if (!hasPerm(p, "ragemode.join")) {
			sendMessage(p, RageMode.getLang().get("no-permission"));
			return;
		}

		if (args.length < 2) {
			sendMessage(p, RageMode.getLang().get("missing-arguments", "%usage%", "/rm join <gameName>"));
			return;
		}

		String map = args[1];
		if (!GameUtils.isGameWithNameExists(map)) {
			sendMessage(p, RageMode.getLang().get("invalid-game", "%game%", map));
			return;
		}

		hu.montlikadani.ragemode.config.ConfigValues cv = RageMode.getInstance().getConfiguration().getCV();
		if (!GameUtils.getGameByName(map).getPlayers().containsKey(p) && cv.isRejoinDelayEnabled()) {
			int hour = cv.getRejoinDelayHour();
			int minute = cv.getRejoinDelayMinute();
			int second = cv.getRejoinDelaySecond();

			if (hour != 0 || minute != 0 || second != 0) {
				if (ReJoinDelay.isRunning(p)) {
					sendMessage(p, RageMode.getLang().get("commands.join.rejoin-delay", "%delay%",
							ReJoinDelay.format(ReJoinDelay.getTimeByPlayer(p))));
					return;
				}

				ReJoinDelay.resetTime(p);
				ReJoinDelay.setTime(p, map, hour, minute, second);
			} else {
				hu.montlikadani.ragemode.Debug.logConsole("The rejoin times can't be 0 hour, 0 minute and 0 second.");
			}
		}

		GameUtils.joinPlayer(p, GameUtils.getGameByName(map));
	}
}
