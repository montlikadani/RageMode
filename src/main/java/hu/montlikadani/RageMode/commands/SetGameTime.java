package hu.montlikadani.ragemode.commands;

import java.io.IOException;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.Utils;
import hu.montlikadani.ragemode.gameUtils.GetGames;

public class SetGameTime extends RmCommand {

	public SetGameTime(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sendMessage(sender, RageMode.getLang().get("in-game-only"));
			return;
		}
		Player p = (Player) sender;
		if (!p.hasPermission("ragemode.admin.setgametime")) {
			sendMessage(p, RageMode.getLang().get("no-permission"));
			return;
		}
		if (args.length >= 3) {
			if (!GetGames.isGameExistent(args[1])) {
				sendMessage(p, RageMode.getLang().get("invalid-game"));
				return;
			}

			if (!RageMode.getInstance().getConfiguration().getArenasCfg().isSet("arenas." + args[1])) {
				sendMessage(p, RageMode.getLang().get("setup.not-set-yet", "%usage%", "/rm addgame <gameName> <maxPlayers>"));
				return;
			}
			if (Utils.isInt(args[2])) {
				RageMode.getInstance().getConfiguration().getArenasCfg().set("arenas." + args[1] + ".gametime", Integer.parseInt(args[2]));
				try {
					RageMode.getInstance().getConfiguration().getArenasCfg().save(RageMode.getInstance().getConfiguration().getArenasFile());
				} catch (IOException e) {
					e.printStackTrace();
					RageMode.getInstance().throwMsg();
				}
				sendMessage(p, RageMode.getLang().get("setup.success"));
			} else
				sendMessage(p, RageMode.getLang().get("not-a-number", "%number%", args[2]));
		} else
			sendMessage(p, RageMode.getLang().get("missing-arguments", "%usage%", "/rm gametime <gameName> <minutes>"));
		return;
	}
}
