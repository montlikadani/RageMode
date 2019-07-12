package hu.montlikadani.ragemode.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.gameUtils.GetGames;
import hu.montlikadani.ragemode.signs.SignCreator;

public class SignUpdate extends RmCommand {

	@Override
	public boolean run(CommandSender sender, Command cmd, String[] args) {
		if (sender instanceof org.bukkit.entity.Player && !hasPerm(sender, "ragemode.admin.signupdate")) {
			sendMessage(sender, RageMode.getLang().get("no-permission"));
			return false;
		}
		if (!RageMode.getInstance().getConfiguration().getCfg().getBoolean("signs.enable"))
			return false;

		if (args.length != 2) {
			sendMessage(sender, RageMode.getLang().get("commands.signupdate.usage"));
			return false;
		}

		String name = null;
		if (args[1].equalsIgnoreCase("all")) {
			for (String game : GetGames.getGameNames()) {
				if (game != null)
					name = game;
			}
		} else {
			name = args[1];

			if (!GetGames.isGameExistent(name)) {
				sendMessage(sender, RageMode.getLang().get("invalid-game", "%game%", name));
				return false;
			}
		}

		SignCreator.updateAllSigns(name);
		return false;
	}
}
