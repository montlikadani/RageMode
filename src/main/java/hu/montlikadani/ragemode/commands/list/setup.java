package hu.montlikadani.ragemode.commands.list;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.commands.CommandProcessor;
import hu.montlikadani.ragemode.commands.ICommand;
import hu.montlikadani.ragemode.gameLogic.Game;
import hu.montlikadani.ragemode.gameUtils.GameUtils;

import static hu.montlikadani.ragemode.utils.Misc.sendMessage;

@CommandProcessor(name = "setup", permission = "ragemode.admin.setup", playerOnly = true)
public class setup implements ICommand {

	@Override
	public boolean run(RageMode plugin, CommandSender sender, String[] args) {
		Player player = (Player) sender;

		if (args.length < 2) {
			sendMessage(player, RageMode.getLang().get("missing-arguments", "%usage%", "/rm setup <gameName>"));
			return false;
		}

		if (!GameUtils.isGameExist(args[1])) {
			sendMessage(player, RageMode.getLang().get("invalid-game", "%game%", args[1]));
			return false;
		}

		Game game = GameUtils.getGame(args[1]);
		if (game.isGameRunning()) {
			sendMessage(player, RageMode.getLang().get("game.running"));
			return false;
		}

		plugin.getSetupGui().openGui(player, game);
		return true;
	}
}
