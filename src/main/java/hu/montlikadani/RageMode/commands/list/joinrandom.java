package hu.montlikadani.ragemode.commands.list;

import static hu.montlikadani.ragemode.utils.Misc.hasPerm;
import static hu.montlikadani.ragemode.utils.Misc.sendMessage;

import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.config.ConfigValues;
import hu.montlikadani.ragemode.gameLogic.Game;
import hu.montlikadani.ragemode.gameUtils.GameUtils;
import hu.montlikadani.ragemode.utils.ReJoinDelay;

public class joinrandom {

	public void run(CommandSender sender) {
		if (!(sender instanceof Player)) {
			sendMessage(sender, RageMode.getLang().get("in-game-only"));
			return;
		}

		Player p = (Player) sender;
		if (!hasPerm(p, "ragemode.joinrandom")) {
			sendMessage(p, RageMode.getLang().get("no-permission"));
			return;
		}

		if (GameUtils.isPlayerPlaying(p)) {
			sendMessage(p, RageMode.getLang().get("game.player-already-in-game", "%usage%", "/rm leave"));
			return;
		}

		java.util.List<Game> games = RageMode.getInstance().getGames();
		if (games.isEmpty()) {
			sendMessage(p, RageMode.getLang().get("no-games"));
			return;
		}

		if (ConfigValues.isRejoinDelayEnabled() && !p.hasPermission("ragemode.bypass.rejoindelay")) {
			if (ReJoinDelay.isValid(p)) {
				sendMessage(p, RageMode.getLang().get("commands.joinrandom.rejoin-delay", "%delay%",
						ReJoinDelay.format(ReJoinDelay.getTimeByPlayer(p) - System.currentTimeMillis())));
				return;
			}

			int hour = ConfigValues.getRejoinDelayHour();
			int minute = ConfigValues.getRejoinDelayMinute();
			int second = ConfigValues.getRejoinDelaySecond();

			if (hour != 0 || minute != 0 || second != 0) {
				ReJoinDelay.resetTime(p);
				ReJoinDelay.setTime(p, hour, minute, second);
			} else {
				hu.montlikadani.ragemode.Debug.logConsole("The rejoin times can't be 0 hour, 0 minute and 0 second.");
			}
		}

		int r = ThreadLocalRandom.current().nextInt(games.size());
		Game game = games.get(r);
		if (game != null) {
			if (!ConfigValues.isPlayersCanJoinRandomToRunningGames() && game.isGameRunning()) {
				sendMessage(p, RageMode.getLang().get("commands.joinrandom.cantjoin"));
				return;
			}

			GameUtils.joinPlayer(p, game);
		}
	}
}
