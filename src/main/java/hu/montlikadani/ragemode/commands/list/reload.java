package hu.montlikadani.ragemode.commands.list;

import org.bukkit.command.CommandSender;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.commands.CommandProcessor;
import hu.montlikadani.ragemode.commands.ICommand;

import static hu.montlikadani.ragemode.utils.Misc.sendMessage;

@CommandProcessor(name = "reload", permission = "ragemode.admin.reload")
public class reload implements ICommand {

	@Override
	public boolean run(RageMode plugin, CommandSender sender, String[] args) {
		if (plugin.reload()) {
			sendMessage(sender, RageMode.getLang().get("commands.reload.success"));
			return true;
		}

		return false;
	}
}
