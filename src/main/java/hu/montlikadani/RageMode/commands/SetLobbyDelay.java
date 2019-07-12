package hu.montlikadani.ragemode.commands;

import java.io.IOException;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.Utils;
import hu.montlikadani.ragemode.gameUtils.GameUtils;

public class SetLobbyDelay extends RmCommand {

	@Override
	public boolean run(RageMode plugin, CommandSender sender, Command cmd, String[] args) {
		if (!(sender instanceof Player)) {
			sendMessage(sender, RageMode.getLang().get("in-game-only"));
			return false;
		}
		Player p = (Player) sender;
		if (!hasPerm(p, "ragemode.admin.setlobbydelay")) {
			sendMessage(p, RageMode.getLang().get("no-permission"));
			return false;
		}
		if (args.length >= 3) {
			if (!GameUtils.isGameWithNameExists(args[1])) {
				sendMessage(p, RageMode.getLang().get("invalid-game", "%game%", args[1]));
				return false;
			}

			if (!plugin.getConfiguration().getArenasCfg().isSet("arenas." + args[1])) {
				sendMessage(p, RageMode.getLang().get("setup.not-set-yet", "%usage%", "/rm addgame <gameName> <maxPlayers>"));
				return false;
			}

			if (!plugin.getConfiguration().getArenasCfg().contains("arenas." + args[1] + ".lobby")) {
				sendMessage(p, RageMode.getLang().get("setup.lobby.not-set", "%game%", args[1]));
				return false;
			}
			if (Utils.isInt(args[2])) {
				plugin.getConfiguration().getArenasCfg().set("arenas." + args[1] + ".lobbydelay", Integer.parseInt(args[2]));
				try {
					plugin.getConfiguration().getArenasCfg().save(plugin.getConfiguration().getArenasFile());
				} catch (IOException e) {
					e.printStackTrace();
					plugin.throwMsg();
				}
				sendMessage(p, RageMode.getLang().get("setup.success"));
			} else
				sendMessage(p, RageMode.getLang().get("not-a-number", "%number%", args[2]));
		} else
			sendMessage(p, RageMode.getLang().get("missing-arguments", "%usage%", "/rm lobbydelay <gameName> <seconds>"));
		return false;
	}
}