package hu.montlikadani.ragemode.commands.list;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.Utils;
import hu.montlikadani.ragemode.API.event.GameLeaveAttemptEvent;
import hu.montlikadani.ragemode.gameLogic.Game;
import hu.montlikadani.ragemode.gameUtils.GameUtils;

import static hu.montlikadani.ragemode.utils.Message.hasPerm;
import static hu.montlikadani.ragemode.utils.Message.sendMessage;

public class leave {

	public boolean run(CommandSender sender) {
		if (!(sender instanceof Player)) {
			sendMessage(sender, RageMode.getLang().get("in-game-only"));
			return false;
		}

		Player p = (Player) sender;
		if (!hasPerm(p, "ragemode.leave")) {
			sendMessage(p, RageMode.getLang().get("no-permission"));
			return false;
		}

		p.removeMetadata("killedWith", RageMode.getInstance());
		Game game = GameUtils.getGameByPlayer(p) == null ? null : GameUtils.getGameByPlayer(p);
		if (game != null) {
			GameUtils.runCommands(p, game.getPlayersGame(p), "leave");
			GameUtils.sendActionBarMessages(p, game.getPlayersGame(p), "leave");
			GameLeaveAttemptEvent gameLeaveEvent = new GameLeaveAttemptEvent(game, p);
			Utils.callEvent(gameLeaveEvent);
			if (!gameLeaveEvent.isCancelled()) {
				game.removePlayer(p);
			}
			game.removeSpectatorPlayer(p);
		} else {
			p.sendMessage(RageMode.getLang().get("game.player-not-ingame"));
		}
		return false;
	}
}
