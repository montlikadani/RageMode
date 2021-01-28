package hu.montlikadani.ragemode.commands.list;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.commands.CommandProcessor;
import hu.montlikadani.ragemode.commands.ICommand;
import hu.montlikadani.ragemode.gameUtils.GameUtils;

import static hu.montlikadani.ragemode.utils.Misc.sendMessage;

@CommandProcessor(name = "setlobby", permission = "ragemode.admin.setlobby", playerOnly = true)
public class setlobby implements ICommand {

	@Override
	public boolean run(RageMode plugin, CommandSender sender, String[] args) {
		Player p = (Player) sender;

		if (args.length < 2) {
			sendMessage(p, RageMode.getLang().get("missing-arguments", "%usage%", "/rm setlobby <gameName>"));
			return false;
		}

		String gameName = args[1];

		if (!GameUtils.isGameExist(gameName)) {
			sendMessage(p, RageMode.getLang().get("invalid-game"));
			return false;
		}

		GameUtils.getGame(gameName).getGameLobby().location = p.getLocation();
		sendMessage(p, RageMode.getLang().get("setup.lobby.set-success", "%game%", gameName));

		return true;
	}
}
