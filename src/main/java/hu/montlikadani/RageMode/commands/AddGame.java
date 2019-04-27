package hu.montlikadani.ragemode.commands;

import java.io.IOException;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.gameLogic.PlayerList;

public class AddGame extends RmCommand {

	public AddGame(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sendMessage(sender, RageMode.getLang().get("in-game-only"));
			return;
		}
		Player p = (Player) sender;
		if (!p.hasPermission("ragemode.admin.addgame")) {
			sendMessage(p, RageMode.getLang().get("no-permission"));
			return;
		}
		if (args.length < 3) {
			sendMessage(p, RageMode.getLang().get("missing-arguments", "%usage%", "/rm addgame <gameName> <maxPlayers>"));
			return;
		}
		int x;
		try {
			x = Integer.parseInt(args[2]);
		} catch (Throwable e) {
			sendMessage(p, RageMode.getLang().get("not-a-number", "%wrong-number%", args[2]));
			return;
		}

		if (RageMode.getInstance().getConfiguration().getArenasCfg().isSet("arenas." + args[1])) {
			sendMessage(p, RageMode.getLang().get("setup.already-exists", "%game%", args[1]));
			return;
		}

		if (x < 2) {
			sendMessage(p, RageMode.getLang().get("setup.at-least-two"));
			return;
		}

		PlayerList.addGameToList(args[1], x);

		RageMode.getInstance().getConfiguration().getArenasCfg().set("arenas." + args[1] + ".maxplayers", Integer.parseInt(args[2]));
		RageMode.getInstance().getConfiguration().getArenasCfg().set("arenas." + args[1] + ".world", p.getWorld().getName());
		try {
			RageMode.getInstance().getConfiguration().getArenasCfg().save(RageMode.getInstance().getConfiguration().getArenasFile());
		} catch (IOException e) {
			e.printStackTrace();
			RageMode.getInstance().throwMsg();
		}

		sendMessage(p, RageMode.getLang().get("setup.success-added", "%game%", args[1]));
		return;
	}
}
