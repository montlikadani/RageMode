package hu.montlikadani.ragemode.commands.list;

import org.bukkit.command.CommandSender;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.commands.CommandProcessor;
import hu.montlikadani.ragemode.commands.ICommand;
import hu.montlikadani.ragemode.config.ConfigValues;
import hu.montlikadani.ragemode.gameLogic.Game;
import hu.montlikadani.ragemode.gameUtils.GameUtils;
import hu.montlikadani.ragemode.signs.SignCreator;

import static hu.montlikadani.ragemode.utils.Misc.sendMessage;

@CommandProcessor(name = "signupdate", permission = "ragemode.admin.signupdate")
public class signupdate implements ICommand {

	@Override
	public boolean run(RageMode plugin, CommandSender sender, String[] args) {
		if (!ConfigValues.isSignsEnable())
			return false;

		if (args.length != 2) {
			sendMessage(sender, RageMode.getLang().get("commands.signupdate.usage"));
			return false;
		}

		if (args[1].equalsIgnoreCase("all")) {
			for (Game game : plugin.getGames()) {
				SignCreator.updateAllSigns(game.getName());
			}
		} else {
			String name = args[1];
			if (GameUtils.getGame(name) == null) {
				sendMessage(sender, RageMode.getLang().get("invalid-game", "%game%", name));
				return false;
			}

			SignCreator.updateAllSigns(name);
		}

		return true;
	}
}
