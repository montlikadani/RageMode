package hu.montlikadani.ragemode.commands.list;

import java.util.Iterator;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.gameLogic.Game;
import hu.montlikadani.ragemode.gameUtils.GameUtils;
import hu.montlikadani.ragemode.scores.RageScores;
import hu.montlikadani.ragemode.signs.SignCreator;
import hu.montlikadani.ragemode.utils.ICommand;

import static hu.montlikadani.ragemode.utils.Message.hasPerm;
import static hu.montlikadani.ragemode.utils.Message.sendMessage;

public class stopgame extends ICommand {

	@Override
	public boolean run(CommandSender sender, String[] args) {
		if (sender instanceof Player && !hasPerm(sender, "ragemode.admin.stopgame")) {
			sendMessage(sender, RageMode.getLang().get("no-permission"));
			return false;
		}

		if (args.length >= 2) {
			String game = args[1];

			if (Game.isGameRunning(game)) {
				RageScores.calculateWinner(game, Game.getPlayersFromList());

				for (Map.Entry<String, String> players : Game.getPlayers().entrySet()) {
					Player player = Bukkit.getPlayer(UUID.fromString(players.getValue()));

					player.removeMetadata("killedWith", RageMode.getInstance());
					Game.removePlayer(player);
				}

				for (Iterator<Entry<UUID, String>> it = Game.getSpectatorPlayers().entrySet().iterator(); it
						.hasNext();) {
					Player pl = Bukkit.getPlayer(it.next().getKey());
					Game.removeSpectatorPlayer(pl);
				}

				GameUtils.broadcastToGame(game, RageMode.getLang().get("game.stopped", "%game%", game));

				Game.setGameNotRunning(game);
				GameUtils.setStatus(game, null);
				SignCreator.updateAllSigns(game);
			} else
				sendMessage(sender, RageMode.getLang().get("game.not-running"));
		} else
			sendMessage(sender, RageMode.getLang().get("missing-arguments", "%usage%", "/rm " + args[0] + " <gameName>"));
		return false;
	}
}
