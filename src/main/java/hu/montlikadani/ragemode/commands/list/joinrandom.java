package hu.montlikadani.ragemode.commands.list;

import static hu.montlikadani.ragemode.utils.Misc.sendMessage;

import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.commands.CommandProcessor;
import hu.montlikadani.ragemode.commands.ICommand;
import hu.montlikadani.ragemode.config.ConfigValues;
import hu.montlikadani.ragemode.gameLogic.Game;
import hu.montlikadani.ragemode.gameUtils.GameUtils;
import hu.montlikadani.ragemode.utils.ReJoinDelay;

@CommandProcessor(name = "joinrandom", permission = "ragemode.joinrandom", playerOnly = true)
public class joinrandom implements ICommand {

	@Override
	public boolean run(RageMode plugin, CommandSender sender, String[] args) {
		Player p = (Player) sender;
		if (GameUtils.isPlayerPlaying(p)) {
			sendMessage(p, RageMode.getLang().get("game.player-already-in-game", "%usage%", "/rm leave"));
			return false;
		}

		java.util.List<Game> games = RageMode.getInstance().getGames();
		if (games.isEmpty()) {
			sendMessage(p, RageMode.getLang().get("no-games"));
			return false;
		}

		if (!ReJoinDelay.checkRejoinDelay(p, "joinrandom")) {
			return false;
		}

		Game game = games.get(ThreadLocalRandom.current().nextInt(games.size()));
		if (game != null) {
			if (!ConfigValues.isPlayersCanJoinRandomToRunningGames() && game.isGameRunning()) {
				sendMessage(p, RageMode.getLang().get("commands.joinrandom.cantjoin"));
				return false;
			}

			GameUtils.joinPlayer(p, game);
		}

		return true;
	}
}
