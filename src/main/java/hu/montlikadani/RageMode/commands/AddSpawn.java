package hu.montlikadani.ragemode.commands;

import java.io.IOException;

import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.gameUtils.GetGames;

public class AddSpawn extends RmCommand {

	public AddSpawn(CommandSender sender, Command cmd, String label, String[] args) {
		if (!(sender instanceof Player)) {
			sendMessage(sender, RageMode.getLang().get("in-game-only"));
			return;
		}
		Player p = (Player) sender;
		if (!p.hasPermission("ragemode.admin.addspawn")) {
			sendMessage(p, RageMode.getLang().get("no-permission"));
			return;
		}
		if (args.length < 2) {
			sendMessage(p, RageMode.getLang().get("missing-arguments", "%usage%", "/rm addspawn <gameName>"));
			return;
		}
		if (!GetGames.isGameExistent(args[1])) {
			sendMessage(p, RageMode.getLang().get("invalid-game"));
			return;
		}

		YamlConfiguration aFile = RageMode.getInstance().getConfiguration().getArenasCfg();
		int i = 1;
		String path = "arenas." + args[1];
		if (!aFile.isSet(path)) {
			sendMessage(p, RageMode.getLang().get("setup.not-set-yet", "%usage%", "/rm addgame <gameName> <maxPlayers>"));
			return;
		}

		while (aFile.isSet(path + ".spawns." + i))
			i++;

		Location loc = p.getLocation();
		aFile.set(path + ".spawns." + i + ".world", p.getWorld().getName());
		aFile.set(path + ".spawns." + i + ".x", loc.getX());
		aFile.set(path + ".spawns." + i + ".y", loc.getY());
		aFile.set(path + ".spawns." + i + ".z", loc.getZ());
		aFile.set(path + ".spawns." + i + ".yaw", loc.getYaw());
		aFile.set(path + ".spawns." + i + ".pitch", loc.getPitch());
		try {
			aFile.save(RageMode.getInstance().getConfiguration().getArenasFile());
		} catch (IOException e) {
			e.printStackTrace();
			RageMode.getInstance().throwMsg();
		}
		sendMessage(p, RageMode.getLang().get("setup.spawn-set-success", "%number%", i, "%game%", args[1]));
		return;
	}
}
