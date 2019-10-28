package hu.montlikadani.ragemode.commands.list;

import org.bukkit.command.CommandSender;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.config.Configuration;
import hu.montlikadani.ragemode.gameLogic.GameStatus;
import hu.montlikadani.ragemode.gameUtils.GameUtils;

import static hu.montlikadani.ragemode.utils.Message.hasPerm;
import static hu.montlikadani.ragemode.utils.Message.sendMessage;

public class togglegame {

	public boolean run(RageMode plugin, CommandSender sender, String[] args) {
		if (!hasPerm(sender, "ragemode.admin.togglegame")) {
			sendMessage(sender, RageMode.getLang().get("no-permission"));
			return false;
		}

		if (args.length < 2) {
			sendMessage(sender, RageMode.getLang().get("missing-arguments", "%usage%", "/rm " + args[0] + " <gameName>"));
			return false;
		}

		String game = args[1];

		if (!GameUtils.isGameWithNameExists(game)) {
			sendMessage(sender, RageMode.getLang().get("invalid-game", "%game%", game));
			return false;
		}

		boolean toggle = true;
		if (!GameUtils.getGame(game).isGameRunning(game)) {
			if (GameUtils.getStatus(game) == GameStatus.NOTREADY) {
				GameUtils.setStatus(game, GameStatus.READY);
				toggle = false;
			} else {
				GameUtils.setStatus(game, GameStatus.NOTREADY);
				GameUtils.getGame(game).setGameNotRunning(game);
			}

			plugin.getConfiguration().getArenasCfg().set("arenas." + game + ".lock", toggle);
			Configuration.saveFile(plugin.getConfiguration().getArenasCfg(), plugin.getConfiguration().getArenasFile());

			// Something wrong with this... But what??
			// Seems the Java is broken for me, because if a string has 3 or less length then it will show as null
			// or probably FileConfiguration is broken?
			sendMessage(sender,
					RageMode.getLang().get("commands.togglegame.successfully-toggled", "%game%", game, "%status%",
							!toggle ? RageMode.getLang().get("commands.togglegame.status.on")
									: RageMode.getLang().get("commands.togglegame.status.off")));
		} else
			sendMessage(sender, RageMode.getLang().get("commands.togglegame.game-is-running"));

		return false;
	}
}
