package hu.montlikadani.ragemode.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.gameUtils.GetGames;
import hu.montlikadani.ragemode.signs.SignCreator;

public class SignUpdate extends RmCommand {

	public SignUpdate(CommandSender sender, Command cmd, String label, String[] args) {
		if (!sender.hasPermission("ragemode.admin.signupdate")) {
			sender.sendMessage(RageMode.getLang().get("no-permission"));
			return;
		}
		if (!RageMode.getInstance().getConfiguration().getCfg().getBoolean("signs.enable"))
			return;

		if (args.length != 2) {
			sender.sendMessage(RageMode.getLang().get("commands.signupdate.usage"));
			return;
		}

		String gameName = args[1];
		if (!GetGames.isGameExistent(gameName)) {
			sender.sendMessage(RageMode.getLang().get("commands.signupdate.game-not-exist"));
			return;
		}

		SignCreator.updateAllSigns(gameName);
		return;
	}
}
