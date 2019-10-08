package hu.montlikadani.ragemode.commands.list;

import org.bukkit.Location;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;

import hu.montlikadani.ragemode.RageMode;
import hu.montlikadani.ragemode.config.Configuration;
import hu.montlikadani.ragemode.gameLogic.GameSpawnGetter;
import hu.montlikadani.ragemode.gameUtils.GameUtils;

import static hu.montlikadani.ragemode.utils.Message.hasPerm;
import static hu.montlikadani.ragemode.utils.Message.sendMessage;

public class addspawn {

	public boolean run(RageMode plugin, CommandSender sender, String[] args) {
		if (!(sender instanceof Player)) {
			sendMessage(sender, RageMode.getLang().get("in-game-only"));
			return false;
		}

		Player p = (Player) sender;
		if (!hasPerm(p, "ragemode.admin.addspawn")) {
			sendMessage(p, RageMode.getLang().get("no-permission"));
			return false;
		}

		if (args.length < 2) {
			sendMessage(p, RageMode.getLang().get("missing-arguments", "%usage%", "/rm addspawn <gameName>"));
			return false;
		}

		if (!GameUtils.isGameWithNameExists(args[1])) {
			sendMessage(p, RageMode.getLang().get("invalid-game"));
			return false;
		}

		FileConfiguration aFile = plugin.getConfiguration().getArenasCfg();
		int i = 1;
		String path = "arenas." + args[1];
		if (!aFile.isSet(path)) {
			sendMessage(p, RageMode.getLang().get("setup.not-set-yet", "%usage%", "/rm addgame <gameName> <maxPlayers>"));
			return false;
		}

		while (aFile.isSet(path + ".spawns." + i))
			i++;

		Location loc = p.getLocation();
		aFile.set(path + ".spawns." + i + ".world", p.getWorld().getName());
		aFile.set(path + ".spawns." + i + ".x", Double.valueOf(loc.getX()));
		aFile.set(path + ".spawns." + i + ".y", Double.valueOf(loc.getY()));
		aFile.set(path + ".spawns." + i + ".z", Double.valueOf(loc.getZ()));
		aFile.set(path + ".spawns." + i + ".yaw", Double.valueOf(loc.getYaw()));
		aFile.set(path + ".spawns." + i + ".pitch", Double.valueOf(loc.getPitch()));
		Configuration.saveFile(aFile, plugin.getConfiguration().getArenasFile());

		for (GameSpawnGetter spawn : plugin.getSpawns()) {
			if (spawn.getGame().getName().equalsIgnoreCase(args[1])) {
				spawn.getSpawnLocations().add(loc);
				break;
			}
		}

		sendMessage(p, RageMode.getLang().get("setup.spawn-set-success", "%number%", i, "%game%", args[1]));
		return false;
	}
}
