package hu.montlikadani.ragemode.commands.list;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.commands.ICommand;
import hu.montlikadani.ragemode.commands.annotations.CommandProcessor;
import hu.montlikadani.ragemode.gameLogic.base.BaseGame;
import hu.montlikadani.ragemode.gameUtils.GameUtils;

import static hu.montlikadani.ragemode.utils.Misc.sendMessage;

@CommandProcessor(
		name = "setlobby",
		permission = "ragemode.admin.setlobby",
		desc = "Sets the lobby spawn for the given game",
		params = "<gameName>",
		playerOnly = true)
public final class setlobby implements ICommand {

	@Override
	public boolean run(RageMode plugin, CommandSender sender, String[] args) {
		if (args.length < 2) {
			sendMessage(sender, RageMode.getLang().get("missing-arguments", "%usage%", "/rm setlobby <gameName>"));
			return false;
		}

		BaseGame game = GameUtils.getGame(args[1]);
		if (game == null) {
			sendMessage(sender, RageMode.getLang().get("invalid-game"));
			return false;
		}

		game.getGameLobby().location = ((Player) sender).getLocation();
		sendMessage(sender, RageMode.getLang().get("setup.lobby.set-success", "%game%", game.getName()));
		return true;
	}
}
