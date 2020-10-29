package hu.montlikadani.ragemode.commands.list;

import java.util.HashMap;
import java.util.List;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.Utils;
import hu.montlikadani.ragemode.API.event.RMGameLeaveAttemptEvent;
import hu.montlikadani.ragemode.API.event.RMGameStopEvent;
import hu.montlikadani.ragemode.area.GameAreaManager;
import hu.montlikadani.ragemode.commands.ICommand;
import hu.montlikadani.ragemode.gameLogic.Game;
import hu.montlikadani.ragemode.gameUtils.GameType;
import hu.montlikadani.ragemode.gameUtils.GameUtils;
import hu.montlikadani.ragemode.managers.PlayerManager;
import hu.montlikadani.ragemode.scores.RageScores;
import hu.montlikadani.ragemode.signs.SignCreator;

import static hu.montlikadani.ragemode.utils.Misc.hasPerm;
import static hu.montlikadani.ragemode.utils.Misc.sendMessage;

public class stopgame implements ICommand {

	@Override
	public boolean run(RageMode plugin, CommandSender sender, String[] args) {
		if (!hasPerm(sender, "ragemode.admin.stopgame")) {
			sendMessage(sender, RageMode.getLang().get("no-permission"));
			return false;
		}

		if (args.length >= 2) {
			String game = args[1];
			if (!GameUtils.isGameWithNameExists(game)) {
				sendMessage(sender, RageMode.getLang().get("invalid-game", "%game%", game));
				return false;
			}

			Game g = GameUtils.getGame(game);
			if (!g.isGameRunning()) {
				sendMessage(sender, RageMode.getLang().get("game.not-running"));
				return false;
			}

			List<PlayerManager> players = new java.util.ArrayList<>(g.getPlayersFromList());
			Utils.callEvent(new RMGameStopEvent(g, players));

			if (g.getGameType() != GameType.APOCALYPSE)
				RageScores.calculateWinner(g, players);
			else
				GameAreaManager.removeEntitiesFromGame(g);

			for (PlayerManager pm : players) {
				Player player = pm.getPlayer();

				RMGameLeaveAttemptEvent gameLeaveEvent = new RMGameLeaveAttemptEvent(g, player);
				Utils.callEvent(gameLeaveEvent);
				if (!gameLeaveEvent.isCancelled()) {
					g.removePlayer(player);
				}
			}

			for (PlayerManager spec : new HashMap<>(g.getSpectatorPlayers()).values()) {
				g.removeSpectatorPlayer(spec.getPlayer());
			}

			g.getActionMessengers().clear();
			g.setGameNotRunning();
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
