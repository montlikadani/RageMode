package hu.montlikadani.ragemode.commands.list;

import static hu.montlikadani.ragemode.utils.Misc.sendMessage;

import org.bukkit.command.CommandSender;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.commands.ICommand;
import hu.montlikadani.ragemode.commands.annotations.CommandProcessor;
import hu.montlikadani.ragemode.config.Configuration;
import hu.montlikadani.ragemode.gameUtils.GameUtils;

@CommandProcessor(
		name = "minplayers",
		permission = "ragemode.admin.setminplayers",
		params = "<gameName> <amount>",
		desc = "Modifies the minplayers value for the game",
		playerOnly = true)
public final class minplayers implements ICommand {

	@Override
	public boolean run(RageMode plugin, CommandSender sender, String[] args) {
		if (args.length < 3) {
			sendMessage(sender,
					RageMode.getLang().get("missing-arguments", "%usage%", "/rm minplayers <gameName> <minPlayers>"));
			return false;
		}

		String game = args[1];
		if (!GameUtils.isGameExist(game)) {
			sendMessage(sender, RageMode.getLang().get("invalid-game", "%game%", game));
			return false;
		}

		int x;
		try {
			x = Integer.parseInt(args[2]);
		} catch (NumberFormatException e) {
			sendMessage(sender, RageMode.getLang().get("not-a-number", "%wrong-number%", args[2]));
			return false;
		}

		if (x < 2) {
			sendMessage(sender, RageMode.getLang().get("setup.at-least-two"));
			return false;
		}

		plugin.getConfiguration().getArenasCfg().set("arenas." + game + ".minplayers", x);
		Configuration.saveFile(plugin.getConfiguration().getArenasCfg(), plugin.getConfiguration().getArenasFile());

		sendMessage(sender, RageMode.getLang().get("commands.minplayers.changed", "%game%", game, "%value%", x));
		return true;
	}
}
