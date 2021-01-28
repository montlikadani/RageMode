package hu.montlikadani.ragemode.commands.list;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.commands.CommandProcessor;
import hu.montlikadani.ragemode.commands.ICommand;
import hu.montlikadani.ragemode.gameLogic.Game;
import hu.montlikadani.ragemode.gameUtils.GameUtils;

import static hu.montlikadani.ragemode.utils.Misc.sendMessage;

@CommandProcessor(name = "forcestart", permission = "ragemode.admin.forcestart")
public class forcestart implements ICommand {

	@Override
	public boolean run(RageMode plugin, CommandSender sender, String[] args) {
		Player p = (Player) sender;
		if (args.length < 2) {
			sendMessage(p, RageMode.getLang().get("missing-arguments", "%usage%", "/rm forcestart <gameName>"));
			return false;
		}

		String name = args[1];
		if (!GameUtils.isGameExist(name)) {
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
