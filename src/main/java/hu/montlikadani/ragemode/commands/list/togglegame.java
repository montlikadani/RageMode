package hu.montlikadani.ragemode.commands.list;

import org.bukkit.command.CommandSender;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.commands.CommandProcessor;
import hu.montlikadani.ragemode.commands.ICommand;
import hu.montlikadani.ragemode.config.Configuration;
import hu.montlikadani.ragemode.gameLogic.Game;
import hu.montlikadani.ragemode.gameLogic.GameStatus;
import hu.montlikadani.ragemode.gameUtils.GameUtils;
import hu.montlikadani.ragemode.signs.SignCreator;

import static hu.montlikadani.ragemode.utils.Misc.sendMessage;

@CommandProcessor(name = "togglegame", permission = "ragemode.admin.togglegame")
public class togglegame implements ICommand {

	@Override
	public boolean run(RageMode plugin, CommandSender sender, String[] args) {
		if (args.length < 2) {
			sendMessage(sender,
					RageMode.getLang().get("missing-arguments", "%usage%", "/rm " + args[0] + " <gameName>"));
			return false;
		}

		String game = args[1];

		if (!GameUtils.isGameExist(game)) {
			sendMessage(sender, RageMode.getLang().get("invalid-game", "%game%", game));
			return false;
		}

		Game g = GameUtils.getGame(game);
		if (g.isGameRunning()) {
			sendMessage(sender, RageMode.getLang().get("commands.togglegame.game-is-running"));
			return false;
		}

		boolean toggle = true;
		if (g.getStatus() == GameStatus.NOTREADY) {
			g.setStatus(GameStatus.READY);
			toggle = false;
		} else {
			g.setStatus(GameStatus.NOTREADY);
			g.setGameRunning(false);
		}

		plugin.getConfiguration().getArenasCfg().set("arenas." + game + ".lock", toggle);
		Configuration.saveFile(plugin.getConfiguration().getArenasCfg(), plugin.getConfiguration().getArenasFile());

		SignCreator.updateAllSigns(game);

		sendMessage(sender,
				RageMode.getLang().get("commands.togglegame.successfully-toggled", "%game%", game, "%status%",
						!toggle ? RageMode.getLang().get("commands.togglegame.status.ready")
								: RageMode.getLang().get("commands.togglegame.status.not-ready")));
		return true;
	}
}
