package hu.montlikadani.ragemode.commands.list;

import static hu.montlikadani.ragemode.utils.Misc.hasPerm;
import static hu.montlikadani.ragemode.utils.Misc.sendMessage;

import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.config.Configuration;
import hu.montlikadani.ragemode.gameUtils.GameUtils;

public class maxplayers {

	public boolean run(RageMode plugin, CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			sendMessage(sender, RageMode.getLang().get("in-game-only"));
			return false;
		}

		Player p = (Player) sender;
		if (!hasPerm(p, "ragemode.admin.maxplayers")) {
			sendMessage(p, RageMode.getLang().get("no-permission"));
			return false;
		}

		if (args.length < 3) {
			sendMessage(p, RageMode.getLang().get("missing-arguments", "%usage%", "/rm maxplayers <gameName> <maxPlayers>"));
			return false;
		}

		String game = args[1];
		if (!GameUtils.isGameWithNameExists(game)) {
			sendMessage(p, RageMode.getLang().get("invalid-game", "%game%", game));
			return false;
		}

		int x;
		try {
			x = Integer.parseInt(args[2]);
		} catch (NumberFormatException e) {
			sendMessage(p, RageMode.getLang().get("not-a-number", "%wrong-number%", args[2]));
			return false;
		}

		if (x < 2) {
			sendMessage(p, RageMode.getLang().get("setup.at-least-two"));
			return false;
		}

		plugin.getConfiguration().getArenasCfg().set("arenas." + game + ".maxplayers", x);
		Configuration.saveFile(plugin.getConfiguration().getArenasCfg(), plugin.getConfiguration().getArenasFile());

		sendMessage(p, RageMode.getLang().get("commands.maxplayers.changed", "%game%", game, "%value%", x));
		return true;
	}
}
