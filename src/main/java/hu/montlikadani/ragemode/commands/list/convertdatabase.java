package hu.montlikadani.ragemode.commands.list;

import static hu.montlikadani.ragemode.utils.Misc.sendMessage;

import org.bukkit.Bukkit;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.commands.ICommand;
import hu.montlikadani.ragemode.database.DBType;

public class convertdatabase implements ICommand {

	@Override
	public boolean run(RageMode plugin, CommandSender sender, String[] args) {
		if (sender instanceof Player && !sender.hasPermission("ragemode.admin.convertdatabase")) {
			sendMessage(sender, RageMode.getLang().get("no-permission"));
			return false;
		}

		if (args.length < 2) {
			sendMessage(sender,
					RageMode.getLang().get("missing-arguments", "%usage%", "/rm convertdatabase <databaseType>"));
			return false;
		}

		final String type = args[1];
		if (!DBType.isDBWithNameExists(type)) {
			sendMessage(sender, RageMode.getLang().get("invalid-database-name"));
			return false;
		}

		String from = plugin.getDatabaseType().name();
		if (from.contentEquals(type.toUpperCase())) {
			sendMessage(sender, RageMode.getLang().get("commands.convertdb.already-set"));
			return false;
		}

		Bukkit.getScheduler().runTaskAsynchronously(plugin, () -> {
			if (plugin.getDatabase().convertDatabase(type)) {
				sendMessage(sender, RageMode.getLang().get("commands.convertdb.done", "%from%", from.toLowerCase(),
						"%to%", type.toLowerCase()));
			} else {
				sendMessage(sender, RageMode.getLang().get("commands.convertdb.error"));
			}
		});

		return true;
	}
}
