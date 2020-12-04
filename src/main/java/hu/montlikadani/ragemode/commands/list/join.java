package hu.montlikadani.ragemode.commands.list;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.commands.CommandProcessor;
import hu.montlikadani.ragemode.commands.ICommand;
import hu.montlikadani.ragemode.config.ConfigValues;
import hu.montlikadani.ragemode.gameUtils.GameUtils;
import hu.montlikadani.ragemode.utils.ReJoinDelay;

import static hu.montlikadani.ragemode.utils.Misc.hasPerm;
import static hu.montlikadani.ragemode.utils.Misc.sendMessage;

@CommandProcessor(name = "join", permission = "ragemode.join", playerOnly = true)
public class join implements ICommand {

	@Override
	public boolean run(RageMode plugin, CommandSender sender, String[] args) {
		Player p = (Player) sender;
		if (args.length < 2) {
			sendMessage(p, RageMode.getLang().get("missing-arguments", "%usage%", "/rm join <gameName>"));
			return false;
		}

		String map = args[1];
		if (!GameUtils.isGameWithNameExists(map)) {
			sendMessage(p, RageMode.getLang().get("invalid-game", "%game%", map));
			return false;
		}

		if (GameUtils.isPlayerPlaying(p)) {
			sendMessage(p, RageMode.getLang().get("game.player-already-in-game", "%usage%", "/rm leave"));
			return false;
		}

		if (ConfigValues.isPerJoinPermissions() && !hasPerm(p, "ragemode.join." + map)) {
			sendMessage(p, RageMode.getLang().get("no-permission"));
			return false;
		}

		if (!ReJoinDelay.checkRejoinDelay(p, "")) {
			return false;
		}

		GameUtils.joinPlayer(p, GameUtils.getGame(map));
		return true;
	}
}
