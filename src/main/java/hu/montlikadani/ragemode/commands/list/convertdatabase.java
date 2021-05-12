package hu.montlikadani.ragemode.commands.list;

import static hu.montlikadani.ragemode.utils.Misc.sendMessage;

import org.bukkit.command.CommandSender;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.commands.ICommand;
import hu.montlikadani.ragemode.commands.annotations.CommandProcessor;
import hu.montlikadani.ragemode.database.DBType;

@CommandProcessor(
	name = "convertdatabase",
	params = "<databaseType>",
	desc = "Converts the current database to a new specific one",
	permission = "ragemode.admin.convertdatabase")
public final class convertdatabase implements ICommand {

	@Override
	public boolean run(RageMode plugin, CommandSender sender, String[] args) {
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

		String from = plugin.getDatabase().getDatabaseType().name();

		if (from.contentEquals(type.toUpperCase())) {
			sendMessage(sender, RageMode.getLang().get("commands.convertdb.already-set"));
			return false;
		}

		plugin.getDatabase().convertDatabase(type).thenAccept(success -> {
			if (success) {
				sendMessage(sender, RageMode.getLang().get("commands.convertdb.done", "%from%", from.toLowerCase(),
						"%to%", type.toLowerCase()));
			} else {
				sendMessage(sender, RageMode.getLang().get("commands.convertdb.error"));
			}
		}).exceptionally(throwable -> {
			sendMessage(sender, RageMode.getLang().get("commands.convertdb.error"));
			return null;
		});

		return true;
	}
}
