package hu.montlikadani.ragemode.commands;

import java.io.IOException;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.gameLogic.GameStatus;
import hu.montlikadani.ragemode.gameLogic.PlayerList;
import hu.montlikadani.ragemode.gameUtils.GameUtils;

public class ToggleGame extends RmCommand {

	@Override
	public boolean run(RageMode plugin, CommandSender sender, Command cmd, String[] args) {
		if (!hasPerm(sender, "ragemode.admin.togglegame")) {
			sendMessage(sender, RageMode.getLang().get("no-permission"));
			return false;
		}

		if (args.length < 2) {
			sendMessage(sender, RageMode.getLang().get("missing-arguments", "%usage%", "/rm " + args[0] + " <gameName>"));
			return false;
		}

		if (!GameUtils.isGameWithNameExists(args[1])) {
			sendMessage(sender, RageMode.getLang().get("invalid-game", "%game%", args[1]));
			return false;
		}

		boolean toggle = true;
		if (!PlayerList.isGameRunning(args[1])) {
			if (GameUtils.getStatus() == GameStatus.NOTREADY) {
				GameUtils.setStatus(GameStatus.READY);
				toggle = false;
			} else {
				GameUtils.setStatus(GameStatus.NOTREADY);
				PlayerList.setGameNotRunning(args[1]);
			}

			plugin.getConfiguration().getArenasCfg().set("arenas." + args[1] + ".lock", toggle);
			try {
				plugin.getConfiguration().getArenasCfg().save(plugin.getConfiguration().getArenasFile());
			} catch (IOException e) {
				e.printStackTrace();
			}

			sendMessage(sender, RageMode.getLang().get("commands.togglegame.successfully-toggled", "%game%", args[1], "%status%",
					!toggle ? RageMode.getLang().get("commands.togglegame.status.on") : RageMode.getLang().get("commands.togglegame.status.off")));
		} else
			sendMessage(sender, RageMode.getLang().get("commands.togglegame.game-is-running"));

		return false;
	}
}
