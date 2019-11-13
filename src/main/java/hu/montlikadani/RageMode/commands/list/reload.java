package hu.montlikadani.ragemode.commands.list;

import org.bukkit.command.CommandSender;

import hu.montlikadani.ragemode.RageMode;

import static hu.montlikadani.ragemode.utils.Misc.hasPerm;
import static hu.montlikadani.ragemode.utils.Misc.sendMessage;

public class reload {

	public boolean run(RageMode plugin, CommandSender sender) {
		if (!hasPerm(sender, "ragemode.admin.reload")) {
			sendMessage(sender, RageMode.getLang().get("no-permission"));
			return false;
		}

		if (plugin.reload()) {
			sendMessage(sender, RageMode.getLang().get("commands.reload.success"));
			return true;
		}

		return false;
	}
}
