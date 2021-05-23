package hu.montlikadani.ragemode.commands.list;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.API.event.RMGameLeaveAttemptEvent;
import hu.montlikadani.ragemode.API.event.RMGameStopEvent;
import hu.montlikadani.ragemode.area.GameAreaManager;
import hu.montlikadani.ragemode.commands.ICommand;
import hu.montlikadani.ragemode.commands.annotations.CommandProcessor;
import hu.montlikadani.ragemode.gameLogic.Game;
import hu.montlikadani.ragemode.gameUtils.GameType;
import hu.montlikadani.ragemode.gameUtils.GameUtils;
import hu.montlikadani.ragemode.managers.PlayerManager;
import hu.montlikadani.ragemode.scores.RageScores;
import hu.montlikadani.ragemode.signs.SignCreator;
import hu.montlikadani.ragemode.utils.Utils;

import static hu.montlikadani.ragemode.utils.Misc.sendMessage;

@CommandProcessor(
		name = "stopgame",
		desc = "Stops the specified game",
		params = "<gameName>",
		permission = "ragemode.admin.stopgame")
public final class stopgame implements ICommand {

	@Override
	public boolean run(RageMode plugin, CommandSender sender, String[] args) {
		if (args.length < 2) {
			sendMessage(sender,
					RageMode.getLang().get("missing-arguments", "%usage%", "/rm " + args[0] + " <gameName>"));
			return false;
		}

		Game game = GameUtils.getGame(args[1]);
		if (game == null) {
			sendMessage(sender, RageMode.getLang().get("invalid-game", "%game%", args[1]));
			return false;
		}

		if (!game.isRunning()) {
			sendMessage(sender, RageMode.getLang().get("game.not-running"));
			return false;
		}

		Utils.callEvent(new RMGameStopEvent(game));

		if (game.getGameType() != GameType.APOCALYPSE)
			RageScores.calculateWinner(game, game.getPlayers());
		else
			GameAreaManager.removeEntitiesFromGame(game);

		for (PlayerManager pm : game.getPlayers()) {
			if (pm.isSpectator()) {
				game.removePlayer(pm.getPlayer());
				continue;
			}

			Player player = pm.getPlayer();

			RMGameLeaveAttemptEvent gameLeaveEvent = new RMGameLeaveAttemptEvent(game, pm);
			Utils.callEvent(gameLeaveEvent);

			if (!gameLeaveEvent.isCancelled()) {
				game.removePlayer(player);
			}
		}

		game.getActionMessengers().clear();
		game.setRunning(false);
		game.setStatus(null);
		SignCreator.updateAllSigns(game.getName());

		sendMessage(sender, RageMode.getLang().get("game.stopped", "%game%", game.getName()));
		return true;
	}
}
