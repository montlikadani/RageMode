package hu.montlikadani.ragemode.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.gameUtils.GetGames;
import hu.montlikadani.ragemode.signs.SignCreator;

public class SignUpdate extends RmCommand {

	public SignUpdate(CommandSender sender, Command cmd, String label, String[] args) {
		if (!sender.hasPermission("ragemode.admin.signupdate")) {
			sendMessage(sender, RageMode.getLang().get("no-permission"));
			return;
		}
		if (!RageMode.getInstance().getConfiguration().getCfg().getBoolean("signs.enable"))
			return;

		if (args.length != 2) {
			sendMessage(sender, RageMode.getLang().get("commands.signupdate.usage"));
			return;
		}

		String name = null;
		if (args[1].equalsIgnoreCase("all"))
			name = GetGames.getGameNames()[GetGames.getConfigGamesCount() - 1];
		else {
			name = args[1];
			if (!GetGames.isGameExistent(name)) {
				sendMessage(sender, RageMode.getLang().get("invalid-game"));
				return;
			}
		}

		SignCreator.updateAllSigns(name);
		return;
	}
}
