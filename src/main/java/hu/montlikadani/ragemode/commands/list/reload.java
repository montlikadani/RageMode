package hu.montlikadani.ragemode.commands.list;

import org.bukkit.command.CommandSender;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.commands.ICommand;
import hu.montlikadani.ragemode.commands.annotations.CommandProcessor;

import static hu.montlikadani.ragemode.utils.Misc.sendMessage;

@CommandProcessor(
		name = "reload",
		desc = "Reloads the entire plugin",
		permission = "ragemode.admin.reload")
public final class reload implements ICommand {

	@Override
	public boolean run(RageMode plugin, CommandSender sender, String[] args) {
		if (plugin.reload()) {
			sendMessage(sender, RageMode.getLang().get("commands.reload.success"));
			return true;
		}

		return false;
	}
}
