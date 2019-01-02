package hu.montlikadani.ragemode.commands;

import java.io.IOException;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;

public class SetGlobalMessages extends RmCommand {

	public SetGlobalMessages(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sender.sendMessage(RageMode.getLang().get("in-game-only"));
			return;
		}
		Player p = (Player) sender;
		if (!p.hasPermission("ragemode.admin.setglobalmessages")) {
			p.sendMessage(RageMode.getLang().get("no-permission"));
			return;
		}
		if (args.length >= 3) {
			if (!RageMode.getInstance().getConfiguration().getArenasCfg().isSet("arenas." + args[1])) {
				p.sendMessage(RageMode.getLang().get("setup.not-set-yet", "%usage%", "/rm addgame <gameName> <maxPlayers>"));
				return;
			}
			RageMode.getInstance().getConfiguration().getArenasCfg().set("arenas." + args[1] + ".death-messages", Boolean.parseBoolean(args[2]));
			try {
				RageMode.getInstance().getConfiguration().getArenasCfg().save(RageMode.getInstance().getConfiguration().getArenasFile());
			} catch (IOException e) {
				e.printStackTrace();
				RageMode.getInstance().throwMsg();
			}
			p.sendMessage(RageMode.getLang().get("setup.success"));
		} else
			p.sendMessage(RageMode.getLang().get("missing-arguments", "%usage%", "/rm " + args[0] + " <gameName> <true|false>"));
		return;
	}
}
