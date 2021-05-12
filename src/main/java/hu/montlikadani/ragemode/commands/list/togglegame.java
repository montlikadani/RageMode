package hu.montlikadani.ragemode.commands.list;

import org.bukkit.command.CommandSender;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.commands.ICommand;
import hu.montlikadani.ragemode.commands.annotations.CommandProcessor;
import hu.montlikadani.ragemode.config.Configuration;
import hu.montlikadani.ragemode.gameLogic.Game;
import hu.montlikadani.ragemode.gameLogic.GameStatus;
import hu.montlikadani.ragemode.gameUtils.GameUtils;
import hu.montlikadani.ragemode.signs.SignCreator;

import static hu.montlikadani.ragemode.utils.Misc.sendMessage;

@CommandProcessor(
		name = "togglegame",
		desc = "Toggles the specified game for players to not be able to join",
		params = "<gameName>",
		permission = "ragemode.admin.togglegame")
public final class togglegame implements ICommand {

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

		if (game.isRunning()) {
			sendMessage(sender, RageMode.getLang().get("commands.togglegame.game-is-running"));
			return false;
		}

		boolean toggle = true;
		if (game.getStatus() == GameStatus.NOTREADY) {
			game.setStatus(GameStatus.READY);
			toggle = false;
		} else {
			game.setStatus(GameStatus.NOTREADY);
			game.setRunning(false);
		}

		plugin.getConfiguration().getArenasCfg().set("arenas." + game.getName() + ".lock", toggle);
		Configuration.saveFile(plugin.getConfiguration().getArenasCfg(), plugin.getConfiguration().getArenasFile());

		SignCreator.updateAllSigns(game.getName());

		sendMessage(sender,
				RageMode.getLang().get("commands.togglegame.successfully-toggled", "%game%", game.getName(), "%status%",
						!toggle ? RageMode.getLang().get("commands.togglegame.status.ready")
								: RageMode.getLang().get("commands.togglegame.status.not-ready")));
		return true;
	}
}
