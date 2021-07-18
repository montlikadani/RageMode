package hu.montlikadani.ragemode.commands.list;

import static hu.montlikadani.ragemode.utils.Misc.sendMessage;

import java.util.concurrent.ThreadLocalRandom;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.commands.ICommand;
import hu.montlikadani.ragemode.commands.annotations.CommandProcessor;
import hu.montlikadani.ragemode.config.configconstants.ConfigValues;
import hu.montlikadani.ragemode.gameLogic.base.BaseGame;
import hu.montlikadani.ragemode.gameUtils.GameUtils;
import hu.montlikadani.ragemode.utils.ReJoinDelay;

@CommandProcessor(
		name = "joinrandom",
		permission = "ragemode.joinrandom",
		desc = "Joins to a random choosed game",
		playerOnly = true)
public final class joinrandom implements ICommand {

	@Override
	public boolean run(RageMode plugin, CommandSender sender, String[] args) {
		Player player = (Player) sender;

		if (GameUtils.isPlayerPlaying(player)) {
			sendMessage(player, RageMode.getLang().get("game.player-already-in-game", "%usage%", "/rm leave"));
			return false;
		}

		java.util.List<BaseGame> games = plugin.getGames();
		if (games.isEmpty()) {
			sendMessage(player, RageMode.getLang().get("no-games"));
			return false;
		}

		if (!ReJoinDelay.checkRejoinDelay(player, "joinrandom")) {
			return false;
		}

		BaseGame game = games.get(ThreadLocalRandom.current().nextInt(games.size()));
		if (game != null) {
			if (!ConfigValues.isPlayersCanJoinRandomToRunningGames() && game.isRunning()) {
				sendMessage(player, RageMode.getLang().get("commands.joinrandom.cantjoin"));
				return false;
			}

			GameUtils.joinPlayer(player, game);
		}

		return true;
	}
}
