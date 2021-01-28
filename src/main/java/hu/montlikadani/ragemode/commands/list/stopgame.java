package hu.montlikadani.ragemode.commands.list;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.Utils;
import hu.montlikadani.ragemode.API.event.RMGameLeaveAttemptEvent;
import hu.montlikadani.ragemode.API.event.RMGameStopEvent;
import hu.montlikadani.ragemode.area.GameAreaManager;
import hu.montlikadani.ragemode.commands.CommandProcessor;
import hu.montlikadani.ragemode.commands.ICommand;
import hu.montlikadani.ragemode.gameLogic.Game;
import hu.montlikadani.ragemode.gameUtils.GameType;
import hu.montlikadani.ragemode.gameUtils.GameUtils;
import hu.montlikadani.ragemode.managers.PlayerManager;
import hu.montlikadani.ragemode.scores.RageScores;
import hu.montlikadani.ragemode.signs.SignCreator;

import static hu.montlikadani.ragemode.utils.Misc.sendMessage;

@CommandProcessor(name = "stopgame", permission = "ragemode.admin.stopgame")
public class stopgame implements ICommand {

	@Override
	public boolean run(RageMode plugin, CommandSender sender, String[] args) {
		if (args.length >= 2) {
			String game = args[1];
			if (!GameUtils.isGameExist(game)) {
				sendMessage(sender, RageMode.getLang().get("invalid-game", "%game%", game));
				return false;
			}

			Game g = GameUtils.getGame(game);
			if (!g.isGameRunning()) {
				sendMessage(sender, RageMode.getLang().get("game.not-running"));
				return false;
			}

			Utils.callEvent(new RMGameStopEvent(g));

			if (g.getGameType() != GameType.APOCALYPSE)
				RageScores.calculateWinner(g, g.getPlayers());
			else
				GameAreaManager.removeEntitiesFromGame(g);

			for (PlayerManager pm : g.getPlayers()) {
				Player player = pm.getPlayer();

				RMGameLeaveAttemptEvent gameLeaveEvent = new RMGameLeaveAttemptEvent(g, player);
				Utils.callEvent(gameLeaveEvent);
				if (!gameLeaveEvent.isCancelled()) {
					g.removePlayer(player);
				}
			}

			for (PlayerManager spec : g.getSpectatorPlayers()) {
				g.removeSpectatorPlayer(spec.getPlayer());
			}

			g.getActionMessengers().clear();
			g.setGameRunning(false);
			g.setStatus(null);
			SignCreator.updateAllSigns(game);
			sendMessage(sender, RageMode.getLang().get("game.stopped", "%game%", game));
		} else {
			sendMessage(sender,
					RageMode.getLang().get("missing-arguments", "%usage%", "/rm " + args[0] + " <gameName>"));
			return false;
		}

		return true;
	}
}
