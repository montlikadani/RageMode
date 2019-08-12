package hu.montlikadani.ragemode.commands;

import org.bukkit.command.CommandSender;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.utils.ICommand;

import static hu.montlikadani.ragemode.utils.Message.hasPerm;
import static hu.montlikadani.ragemode.utils.Message.sendMessage;

public class Reload extends ICommand {

	@Override
	public boolean run(RageMode plugin, CommandSender sender) {
		if (sender instanceof org.bukkit.entity.Player && !hasPerm(sender, "ragemode.admin.reload")) {
			sendMessage(sender, RageMode.getLang().get("no-permission"));
			return false;
		}

		plugin.reload();

		sendMessage(sender, RageMode.getLang().get("commands.reload.success"));
		return false;
	}
}
