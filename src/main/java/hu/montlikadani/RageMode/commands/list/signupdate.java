package hu.montlikadani.ragemode.commands.list;

import org.bukkit.command.CommandSender;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.config.ConfigValues;
import hu.montlikadani.ragemode.gameUtils.GetGames;
import hu.montlikadani.ragemode.signs.SignCreator;

import static hu.montlikadani.ragemode.utils.Misc.hasPerm;
import static hu.montlikadani.ragemode.utils.Misc.sendMessage;

public class signupdate {

	public boolean run(CommandSender sender, String[] args) {
		if (!hasPerm(sender, "ragemode.admin.signupdate")) {
			sendMessage(sender, RageMode.getLang().get("no-permission"));
			return false;
		}

		if (!ConfigValues.isSignsEnable())
			return false;

		if (args.length != 2) {
			sendMessage(sender, RageMode.getLang().get("commands.signupdate.usage"));
			return false;
		}

		if (args[1].equalsIgnoreCase("all")) {
			for (String game : GetGames.getGameNames()) {
				if (game != null) {
					SignCreator.updateAllSigns(game);
				}
			}
		} else {
			String name = args[1];
			if (!GetGames.isGameExistent(name)) {
				sendMessage(sender, RageMode.getLang().get("invalid-game", "%game%", name));
				return false;
			}

			SignCreator.updateAllSigns(name);
		}

		return true;
	}
}
