package hu.montlikadani.ragemode.commands.list;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.commands.ICommand;
import hu.montlikadani.ragemode.gameLogic.Game;
import hu.montlikadani.ragemode.gameUtils.GameUtils;

import static hu.montlikadani.ragemode.utils.Misc.hasPerm;
import static hu.montlikadani.ragemode.utils.Misc.sendMessage;

public class forcestart implements ICommand {

	@Override
	public boolean run(RageMode plugin, CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			sendMessage(sender, RageMode.getLang().get("in-game-only"));
			return false;
		}

		Player p = (Player) sender;
		if (!hasPerm(p, "ragemode.admin.forcestart")) {
			sendMessage(p, RageMode.getLang().get("no-permission"));
			return false;
		}

		if (args.length < 2) {
			sendMessage(p, RageMode.getLang().get("missing-arguments", "%usage%", "/rm forcestart <gameName>"));
			return false;
		}

		String name = args[1];
		if (!GameUtils.isGameWithNameExists(name)) {
			sendMessage(p, RageMode.getLang().get("commands.forcestart.game-not-exist"));
			return false;
		}

		Game game = GameUtils.getGame(name);
		if (game.isGameRunning()) {
			sendMessage(p, RageMode.getLang().get("game.running"));
			return false;
		}

		if (!GameUtils.forceStart(game)) {
			sendMessage(p, RageMode.getLang().get("not-enough-players"));
			return false;
		}

		sendMessage(p, RageMode.getLang().get("commands.forcestart.game-start", "%game%", game.getName()));
		return true;
	}
}
