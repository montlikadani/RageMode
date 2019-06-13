package hu.montlikadani.ragemode.commands;

import java.io.IOException;

import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.API.event.GameCreateEvent;
import hu.montlikadani.ragemode.events.BungeeListener;
import hu.montlikadani.ragemode.gameLogic.PlayerList;
import hu.montlikadani.ragemode.gameUtils.GameUtils;

public class AddGame extends RmCommand {

	@Override
	public boolean run(RageMode plugin, CommandSender sender, Command cmd, String[] args) {
		if (!(sender instanceof Player)) {
			sendMessage(sender, RageMode.getLang().get("in-game-only"));
			return false;
		}
		Player p = (Player) sender;
		if (!hasPerm(p, "ragemode.admin.addgame")) {
			sendMessage(p, RageMode.getLang().get("no-permission"));
			return false;
		}
		if (args.length < 3) {
			sendMessage(p, RageMode.getLang().get("missing-arguments", "%usage%", "/rm addgame <gameName> <maxPlayers>"));
			return false;
		}
		int x;
		try {
			x = Integer.parseInt(args[2]);
		} catch (Throwable e) {
			sendMessage(p, RageMode.getLang().get("not-a-number", "%wrong-number%", args[2]));
			return false;
		}

		String game = args[1];
		if (GameUtils.isGameWithNameExists(game)) {
			sendMessage(p, RageMode.getLang().get("setup.already-exists", "%game%", game));
			return false;
		}

		if (x < 2) {
			sendMessage(p, RageMode.getLang().get("setup.at-least-two"));
			return false;
		}

		if (plugin.getConfiguration().getCfg().getBoolean("bungee.enable"))
			plugin.getManager().registerEvents(new BungeeListener(game), plugin);

		new PlayerList();

		GameCreateEvent event = new GameCreateEvent(game);
		Bukkit.getPluginManager().callEvent(event);

		PlayerList.addGameToList(game, x);

		plugin.getConfiguration().getArenasCfg().set("arenas." + game + ".maxplayers", Integer.parseInt(args[2]));
		plugin.getConfiguration().getArenasCfg().set("arenas." + game + ".world", p.getWorld().getName());
		try {
			plugin.getConfiguration().getArenasCfg().save(plugin.getConfiguration().getArenasFile());
		} catch (IOException e) {
			e.printStackTrace();
			plugin.throwMsg();
		}

		sendMessage(p, RageMode.getLang().get("setup.success-added", "%game%", game));
		return false;
	}
}
